package io.github.aapplet.segment;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

public class IdGenerator {

    private final IdSegmentLoader idSegmentLoader;
    private final Map<String, IdSegmentBuffer> cache = new ConcurrentHashMap<>();

    public IdGenerator(IdSegmentLoader idSegmentLoader) {
        this.idSegmentLoader = idSegmentLoader;
    }

    /**
     * @param key service key
     * @return id
     */
    public long nextId(String key) {
        IdSegmentBuffer idSegmentBuffer = this.cache.get(key);
        if (idSegmentBuffer == null) {
            idSegmentBuffer = this.initializeBuffer(key);
        }
        long loadTimeoutTimestamp = -1L;
        final Lock readLock = idSegmentBuffer.getReadLock();
        final Lock writeLock = idSegmentBuffer.getWriteLock();
        while (true) {
            if (readLock.tryLock()) {
                this.loadNextSegment(idSegmentBuffer);
                try {
                    IdSegment idSegment = idSegmentBuffer.getSegment();
                    long value = idSegment.getAndIncrement();
                    if (value < idSegment.getMaxId()) {
                        return value;
                    }
                } finally {
                    readLock.unlock();
                }
            }
            if (writeLock.tryLock()) {
                try {
                    IdSegment idSegment = idSegmentBuffer.getSegment();
                    long value = idSegment.getAndIncrement();
                    if (value < idSegment.getMaxId()) {
                        return value;
                    }
                    // 下一段准备就绪，则切换
                    if (idSegmentBuffer.isNextSegmentReady()) {
                        idSegmentBuffer.switchSegment();
                    }
                    // 下一段没有准备好，则启动计时
                    else if (loadTimeoutTimestamp < 0) {
                        loadTimeoutTimestamp = System.currentTimeMillis();
                    }
                    // 下一段在5秒后还没有准备好，则抛出异常
                    else if (System.currentTimeMillis() - loadTimeoutTimestamp > 5 * 1000) {
                        throw new RuntimeException("请求超时:SegmentBuffer中的两个Segment均未从DB中装载");
                    }
                } finally {
                    writeLock.unlock();
                }
            } else {
                Thread.yield();
            }
        }
    }

    /**
     * 初始化号段
     */
    private synchronized IdSegmentBuffer initializeBuffer(String key) {
        if (!this.cache.containsKey(key)) {
            IdSegment idSegment = idSegmentLoader.initSegment(key);
            IdSegmentBuffer idSegmentBuffer = new IdSegmentBuffer(key);
            idSegmentBuffer.getSegment().setValue(idSegment);
            this.cache.put(key, idSegmentBuffer);
            return idSegmentBuffer;
        }
        return this.cache.get(key);
    }

    /**
     * 加载下一号段
     */
    private void loadNextSegment(IdSegmentBuffer buffer) {
        if (!buffer.isNextSegmentReady() && buffer.loadThreadAvailable()) {
            CompletableFuture.runAsync(() -> {
                try {
                    // 当前时间戳
                    final long currentTimestamp = System.currentTimeMillis();
                    // 距上次更新间隔时间
                    final long intervalTimestamp = currentTimestamp - buffer.getLastUpdateTimestamp();
                    // 间隔3分钟以内，步长翻倍，且步长不受限制
                    if (intervalTimestamp < 1000 * 60 * 3) {
                        idSegmentLoader.updateStepIncrement(buffer.getKey());
                    }
                    // 间隔15分钟以内，步长翻倍，步长最多为100_000
                    else if (intervalTimestamp < 1000 * 60 * 15 && buffer.getSegment().getStep() < 100_000) {
                        idSegmentLoader.updateStepIncrement(buffer.getKey());
                    }
                    // 间隔30分钟以外，步长减半，步长最少为10
                    else if (intervalTimestamp > 1000 * 60 * 30 && buffer.getSegment().getStep() > 10) {
                        idSegmentLoader.updateStepDecrement(buffer.getKey());
                    }
                    // 更新下一段
                    buffer.nextSegment().setValue(idSegmentLoader.getSegment(buffer.getKey()));
                    // 更新最后更新时间戳
                    buffer.setLastUpdateTimestamp(currentTimestamp);
                    // 下一号段准备就绪
                    buffer.nextSegmentReady();
                } finally {
                    // 加载线程准备就绪
                    buffer.loadThreadReady();
                }
            });
        }
    }

}