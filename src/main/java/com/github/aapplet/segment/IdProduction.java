package com.github.aapplet.segment;

import com.github.aapplet.segment.loader.SegmentData;
import com.github.aapplet.segment.loader.SegmentLoader;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

public class IdProduction implements IdGenerator {

    private final SegmentLoader segmentLoader;
    private final Map<String, IdSegmentBuffer> cache = new ConcurrentHashMap<>();

    public IdProduction(SegmentLoader segmentLoader) {
        this.segmentLoader = segmentLoader;
    }

    @Override
    public long get(String key) {
        IdSegmentBuffer idSegmentBuffer = cache.get(key);
        if (idSegmentBuffer == null) {
            idSegmentBuffer = this.initializeSegment(key);
        }
        // 预留计时器
        long reservedTimer = Integer.MIN_VALUE;
        final Lock readLock = idSegmentBuffer.getReadWriteLock().readLock();
        final Lock writeLock = idSegmentBuffer.getReadWriteLock().writeLock();
        while (true) {
            // 获取读锁,读写互斥,写写互斥,读读共享
            if (readLock.tryLock()) {
                try {
                    // 加载下一段
                    this.loadNextSegment(idSegmentBuffer);
                    // 获取号段生成ID
                    IdSegment segment = idSegmentBuffer.currentSegment();
                    long value = segment.incrementAndGet();
                    if (value <= segment.getMaxId()) {
                        return value;
                    }
                } finally {
                    // 释放读锁
                    readLock.unlock();
                }
            }
            // 获取写锁,读写互斥,写写互斥,读读共享
            if (writeLock.tryLock()) {
                try {
                    // 从新获取号段校验是否需要切换,防止并发时多次切换号段
                    IdSegment segment = idSegmentBuffer.currentSegment();
                    long value = segment.incrementAndGet();
                    if (value <= segment.getMaxId()) {
                        return value;
                    }
                    // 下一号段准备就绪则切换
                    if (idSegmentBuffer.nextChainIsReady()) {
                        idSegmentBuffer.switchNextSegment();
                    }
                    // 下一号段未就绪则开始计时
                    else if (reservedTimer < 0) {
                        reservedTimer = System.currentTimeMillis();
                    }
                    // 下一号段5秒后还未就绪则抛出异常
                    else if (System.currentTimeMillis() - reservedTimer > 5 * 1000) {
                        throw new RuntimeException("请求超时:SegmentChain中的Segment均未从DB中装载");
                    }
                } finally {
                    // 释放写锁
                    writeLock.unlock();
                }
            } else {
                // 线程让步
                Thread.yield();
            }
        }
    }

    /**
     * 初始化段
     */
    private synchronized IdSegmentBuffer initializeSegment(String key) {
        // 防止并发初始化
        if (!this.cache.containsKey(key)) {
            // 初始化号段
            SegmentData segmentData = segmentLoader.initSegment(key);
            IdSegmentChain idSegmentChain = new IdSegmentChain(segmentData);
            IdSegmentBuffer idSegmentBuffer = new IdSegmentBuffer(key, idSegmentChain);
            // 初始化cache
            this.cache.put(key, idSegmentBuffer);
        }
        return this.cache.get(key);
    }

    /**
     * 加载下一段
     */
    private void loadNextSegment(IdSegmentBuffer idSegmentBuffer) {
        // 下一号段未就绪,线程未执行,则异步加载下一段
        if (!idSegmentBuffer.nextChainIsReady() && idSegmentBuffer.getLoadThreadState().compareAndSet(true, false)) {
            // 异步更新号段
            CompletableFuture.runAsync(() -> {
                if (idSegmentBuffer.nextChainIsReady()) return;
                try {
                    // 当前更新时间
                    final long currentUpdateTimestamp = System.currentTimeMillis();
                    // 更新间隔时间
                    final long intervalTimestamp = currentUpdateTimestamp - idSegmentBuffer.getLastUpdateTimestamp();
                    // 1分钟内再次获取则步长翻倍,不限制步长
                    if (intervalTimestamp < 1000 * 60) {
                        segmentLoader.updateStepIncrement(idSegmentBuffer.getKey());
                    }
                    // 15分钟内再次获取则步长翻倍,步长限制10万
                    else if (intervalTimestamp < 1000 * 60 * 15 && idSegmentBuffer.currentSegment().getStep() < 100_000) {
                        segmentLoader.updateStepIncrement(idSegmentBuffer.getKey());
                    }
                    // 30分钟外再次获取则步长减半,步长最少为10
                    else if (intervalTimestamp > 1000 * 60 * 30 && idSegmentBuffer.currentSegment().getStep() > 10) {
                        segmentLoader.updateStepDecrement(idSegmentBuffer.getKey());
                    }
                    // 获取下一号段
                    SegmentData segmentData = segmentLoader.getSegment(idSegmentBuffer.getKey());
                    // 装载下一号段
                    idSegmentBuffer.getSegmentChain().setNextChain(new IdSegmentChain(segmentData));
                    // 设置最后更新时间
                    idSegmentBuffer.setLastUpdateTimestamp(currentUpdateTimestamp);
                } finally {
                    // 线程准备就绪
                    idSegmentBuffer.getLoadThreadState().set(true);
                }
            });
        }
    }

}
