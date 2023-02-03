package util;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedQueue {

    private final Queue queue;
    private final ReentrantLock lock;
    private final Condition condition;

    public SynchronizedQueue(int queueMaxSize) {
        this(new ArrayDeque<>(queueMaxSize));
    }

    public SynchronizedQueue(Queue queue) {
        this.queue = queue;
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    public boolean offer(Object e) {
        return queue.offer(e);
    }

    public Object poll() {
        return queue.poll();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public Condition getCondition() {
        return condition;
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
