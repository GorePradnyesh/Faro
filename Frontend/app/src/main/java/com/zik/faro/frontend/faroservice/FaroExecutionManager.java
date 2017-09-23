package com.zik.faro.frontend.faroservice;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by gaurav on 6/18/17.
 */

public class FaroExecutionManager {
    // TODO : Determine the appropriate number of threads
    private static final int NUM_THREADS = 5;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

    public static <T> Future<T> execute(Callable<T> job) {
        return threadPool.submit(job);
    }

    public static void execute(Runnable job) {
        threadPool.execute(job);
    }

}
