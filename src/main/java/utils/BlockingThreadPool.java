package utils;

import Constants.InternalConstants;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A fixed-capacity queue which blocks threads which attempt to submit while it is at max capacity.
 * @param <E>   The type of object which the queue will hold
 */
class LimitedQueue<E> extends LinkedBlockingQueue<E> {
    public LimitedQueue(int maxSize) {
        super(maxSize);
    }
    @Override
    public boolean offer(E e) {
        // turn offer() and add() into a blocking calls (unless interrupted)
        try {
            put(e);
            return true;
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}

/**
 * A ThreadPoolExecutor using a fixed number of threads to handle the job queue. This class's job queue has a size
 * limit. When it is full, any further job submissions will block the submitting thread until there is again space in
 * the queue, whereupon the submission will complete.
 */
public class BlockingThreadPool extends ThreadPoolExecutor {
    /**
     * @param num_threads       The number of threads used to execute jobs in the job queue
     * @param queue_capacity    The maximum capacity of the job queue
     */
    public BlockingThreadPool(int num_threads, int queue_capacity) {
        // For this class to function correctly, the minimum and maximum number of threads must be equal (as they are
        // below).
        // See https://stackoverflow.com/questions/4521983/java-executorservice-that-blocks-on-submission-after-a-certain-queue-size
        // for more details
        super(
                num_threads, num_threads,
                InternalConstants.THREAD_TIMEOUT_MILLIS, InternalConstants.THREAD_TIMEOUT_UNITS,
                new LimitedQueue<>(queue_capacity)
        );
    }
}
