package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        UnsafeSequence unsafeSequence = new UnsafeSequence();
        parallelize(unsafeSequence::getNext);
        // most likely will be different all the time
        System.out.println(unsafeSequence.getNext());

        Sequence safeSequence = new Sequence();
        // should be 10000 always
        parallelize(safeSequence::getNext);
        System.out.println(safeSequence.getNext());
    }

    private static void parallelize(Runnable runnable) {
        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < 10000; i++) {
            threadPool.submit(runnable);
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
