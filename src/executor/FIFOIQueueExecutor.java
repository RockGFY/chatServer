package executor;

import handler.IHandlerRouter;
import queue.BundleQueue;
import queue.IExecutor;
import util.SystemLogger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FIFOIQueueExecutor
        implements IExecutor {

    private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            10,
            200,
            500,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(10),
            new ThreadPoolExecutor.CallerRunsPolicy());

    private IHandlerRouter handler;

    public FIFOIQueueExecutor(IHandlerRouter handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        execute();
    }

    @Override
    public void execute() {
        SystemLogger.info("Starting executor...");
        while (!Thread.interrupted()) {
            if (!BundleQueue.isEmpty()) {
                SystemLogger.info("Processing a session...");
                var bundle = BundleQueue.poll();
                assert bundle != null;
                SystemLogger.print("Session {0} contains: {1}.", bundle, bundle.getRawData());
                threadPool.execute(new Process(handler, bundle));
            }
        }
    }
}
