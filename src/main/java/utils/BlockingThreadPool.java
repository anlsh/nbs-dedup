package utils;

import jdk.nashorn.internal.ir.Block;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// See https://stackoverflow.com/questions/4521983/java-executorservice-that-blocks-on-submission-after-a-certain-queue-size
// for very important information about this class and its functionality

class LimitedQueue<E> extends LinkedBlockingQueue<E>
{
    public LimitedQueue(int maxSize)
    {
        super(maxSize);
    }

    @Override
    public boolean offer(E e)
    {
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

public class BlockingThreadPool extends ThreadPoolExecutor {
    public BlockingThreadPool(int num_threads, int queue_capacity) {
        super(num_threads, num_threads, 2000, TimeUnit.MILLISECONDS, new LimitedQueue<>(queue_capacity));
    }
}
