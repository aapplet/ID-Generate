package io.github.aapplet.segment.core;

import io.github.aapplet.segment.loader.SegmentData;
import io.github.aapplet.segment.loader.SegmentLoader;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

public final class IdProduction implements IdGenerator {

    private final SegmentLoader segmentLoader;
    private final Map<String, IdSegmentBuffer> cache;

    public IdProduction(SegmentLoader segmentLoader) {
        this.segmentLoader = segmentLoader;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public long nextId(String key) {
        IdSegmentBuffer idSegmentBuffer = cache.get(key);
        if (idSegmentBuffer == null) {
            idSegmentBuffer = this.initializeSegment(key);
        }
        // 加载超时时间戳,未初始化状态
        long loadTimeoutTimestamp = -1;
        final Lock readLock = idSegmentBuffer.getReadLock();
        final Lock writeLock = idSegmentBuffer.getWriteLock();
        while (true) {
            if (readLock.tryLock()) {
                // 加载下一号段
                this.loadNextSegment(idSegmentBuffer);
                try {
                    IdSegment segment = idSegmentBuffer.currentSegment();
                    long value = segment.incrementAndGet();
                    if (value <= segment.getMaxId()) {
                        return value;
                    }
                } finally {
                    readLock.unlock();
                }
            }
            if (writeLock.tryLock()) {
                try {
                    IdSegment segment = idSegmentBuffer.currentSegment();
                    long value = segment.incrementAndGet();
                    if (value <= segment.getMaxId()) {
                        return value;
                    }
                    // 下一段准备就绪，则切换
                    if (idSegmentBuffer.nextSegmentReady()) {
                        idSegmentBuffer.switchNextSegment();
                    }
                    // 下一段没有准备好，则启动计时,初始化状态
                    else if (loadTimeoutTimestamp < 0) {
                        loadTimeoutTimestamp = System.currentTimeMillis();
                    }
                    // 下一段在5秒后还没有准备好，则抛出异常
                    else if (System.currentTimeMillis() - loadTimeoutTimestamp > 5 * 1000) {
                        throw new RuntimeException("segment load timeout");
                    }
                } finally {
                    writeLock.unlock();
                }
            } else {
                // 让其他线程先运行
                Thread.yield();
            }
        }
    }

    /**
     * 初始化号段
     */
    private synchronized IdSegmentBuffer initializeSegment(String key) {
        if (!this.cache.containsKey(key)) {
            SegmentData segmentData = segmentLoader.initSegment(key);
            this.cache.put(key, new IdSegmentBuffer(key, segmentData));
        }
        return this.cache.get(key);
    }

    /**
     * 加载下一号段
     */
    private void loadNextSegment(IdSegmentBuffer idSegmentBuffer) {
        // 下一段未就绪，线程未执行，则异步加载下一段
        if (!idSegmentBuffer.nextSegmentReady() && idSegmentBuffer.loadThreadAvailable()) {
            // 异步加载下一段
            CompletableFuture.runAsync(() -> {
                // 防止多次加载下一段
                if (idSegmentBuffer.nextSegmentReady()) return;
                try {
                    // 当前时间戳
                    final long currentTimestamp = System.currentTimeMillis();
                    // 距上次更新间隔时间
                    final long intervalTimestamp = currentTimestamp - idSegmentBuffer.getLastUpdateTimestamp();
                    // 间隔3分钟以内，步长翻倍，且步长不受限制
                    if (intervalTimestamp < 1000 * 60 * 3) {
                        segmentLoader.updateStepIncrement(idSegmentBuffer.getKey());
                    }
                    // 间隔15分钟以内，步长翻倍，步长最多为100_000
                    else if (intervalTimestamp < 1000 * 60 * 15 && idSegmentBuffer.currentSegment().getStep() < 100_000) {
                        segmentLoader.updateStepIncrement(idSegmentBuffer.getKey());
                    }
                    // 间隔30分钟以外，步长减半，步长最少为10
                    else if (intervalTimestamp > 1000 * 60 * 30 && idSegmentBuffer.currentSegment().getStep() > 10) {
                        segmentLoader.updateStepDecrement(idSegmentBuffer.getKey());
                    }
                    // 装载下一段
                    idSegmentBuffer.loadNextSegment(segmentLoader.getSegment(idSegmentBuffer.getKey()));
                    // 设置最后更新时间戳
                    idSegmentBuffer.setLastUpdateTimestamp(currentTimestamp);
                } finally {
                    // 加载线程就绪
                    idSegmentBuffer.loadThreadReady();
                }
            });
        }
    }

}
