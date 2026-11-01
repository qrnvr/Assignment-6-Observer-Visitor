package com.arnur.transport.concurrent;

import com.arnur.transport.observer.source.VehicleArrivalSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public final class ConcurrentArrivalSimulation {
    private final VehicleArrivalSource source;
    private final int threadCount;

    public ConcurrentArrivalSimulation(VehicleArrivalSource source, int threadCount) {
        if (threadCount < 1) throw new IllegalArgumentException("threadCount must be positive");
        this.source = source;
        this.threadCount = threadCount;
    }

    public int run(int producerCount, int eventsPerProducer) throws InterruptedException {
        if (producerCount < 3) throw new IllegalArgumentException("At least 3 producer threads are required");
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int producer = 0; producer < producerCount; producer++) {
            int producerId = producer;
            futures.add(executor.submit(() -> {
                start.await();
                for (int i = 0; i < eventsPerProducer; i++) {
                    source.arrival("SIM-" + producerId + "-" + i, "R1", "S1", 0, 1);
                }
                return null;
            }));
        }
        start.countDown();
        executor.shutdown();
        try {
            for (Future<?> future : futures) future.get();
        } catch (ExecutionException e) {
            executor.shutdownNow();
            throw new IllegalStateException("Producer failed", e.getCause());
        }
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Executor did not terminate cleanly");
            }
        }
        return producerCount * eventsPerProducer;
    }
}
