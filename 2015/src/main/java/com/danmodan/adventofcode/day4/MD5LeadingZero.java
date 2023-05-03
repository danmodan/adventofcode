package com.danmodan.adventofcode.day4;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MD5LeadingZero {

    public static void main(String[] args) throws InterruptedException {

        ExecutorService threadpool = Executors.newCachedThreadPool();
        int maxQueueSize = 10000;
        ThreadSafeQueue<Integer> queue = new ThreadSafeQueue<>(maxQueueSize);
        Semaphore semaphore = new Semaphore(0);

        for (var i = 0; i < 1; i++) {
            threadpool.execute(new ProducerTask(queue));
        }

        int permits = 5;
        for (var i = 0; i < permits; i++) {
            threadpool.execute(new ConsumerTask(queue, semaphore, "000000"));
        }
        threadpool.execute(new AnswerTask(semaphore, permits));

        threadpool.awaitTermination(6, TimeUnit.SECONDS);
    }
}

class AnswerTask implements Runnable {

    private final int permits;
    private final Semaphore semaphore;
    private final Counter counter;

    public AnswerTask(Semaphore semaphore, int permits) {
        this.semaphore = semaphore;
        this.permits = permits;
        this.counter = CounterFactory.getSingleton();
    }

    @Override
    public void run() {

        var start = System.currentTimeMillis();
        try {
            semaphore.acquire(permits);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        var end = System.currentTimeMillis();

        var founds = counter.getFounded();
        founds.sort(Comparator.naturalOrder());

        System.out.println("Response: " + founds.get(0));
        float duration = (end - start)/1000F;
        System.out.println(String.format("Duration: %f.3 seconds.", duration));
    }
}

class ConsumerTask implements Runnable {

    private static final String RAW_MSG = "yzbqklnj";
    private final Counter counter;
    private final ThreadSafeQueue<Integer> queue;
    private final Semaphore semaphore;
    private final String prefix;

    public ConsumerTask(ThreadSafeQueue<Integer> queue, Semaphore semaphore, String prefix) {

        this.counter = CounterFactory.getSingleton();
        this.queue = queue;
        this.semaphore = semaphore;
        this.prefix = prefix;
    }

    @Override
    public void run() {

        var mdOptional = getMessageDigest();

        if (mdOptional.isEmpty()) {
            return;
        }

        MessageDigest md = mdOptional.get();

        while (true) {

            var polled = queue.poll();
            if (polled == null) {
                break;
            }

            var formatedMessage = RAW_MSG + polled;
            md.update(formatedMessage.getBytes());
            byte[] digest = md.digest();
            String hash = HexFormat.of().formatHex(digest);
            // System.out.println(String.format("[%s] %d - %s", Thread.currentThread().getName(), polled, hash));
            if (hash.startsWith(prefix)) {
                counter.addFounded(polled);
            }
        }

        System.out.println(String.format("[%s] Consumer terminated", Thread.currentThread().getName()));
        semaphore.release();
    }

    private Optional<MessageDigest> getMessageDigest() {

        try {
            return Optional.of(MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}

class ProducerTask implements Runnable {

    private final Counter counter;
    private final ThreadSafeQueue<Integer> queue;

    public ProducerTask(ThreadSafeQueue<Integer> queue) {
        this.counter = CounterFactory.getSingleton();
        this.queue = queue;
    }

    @Override
    public void run() {

        while (counter.hasNext()) {

            queue.add(counter.getNext());
        }

        queue.terminate();
        System.out.println("Producer terminated");
    }
}

class ThreadSafeQueue<T> {

    private final Queue<T> queue;
    private final int maxQueueSize;
    private boolean terminated = false;
    private boolean isEmpty = true;

    public ThreadSafeQueue(int maxQueueSize) {
        this.queue = new ArrayDeque<>(maxQueueSize);
        this.maxQueueSize = maxQueueSize;
    }

    public synchronized void add(T msg) {

        while (queue.size() + 1 == maxQueueSize) {
            try {
                // System.out.println("cheio");
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        terminated = false;
        isEmpty = false;

        queue.add(msg);
        notify();
    }

    public synchronized T poll() {

        while (!terminated && isEmpty) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        T polled = null;

        if (queue.size() == 1) {
            isEmpty = true;
        }

        polled = queue.poll();

        if (polled != null) {
            notifyAll();
        }

        return polled;
    }

    public synchronized void terminate() {

        while (!isEmpty) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.terminated = true;
        notifyAll();
    }
}

class Counter {

    private int index = 0;
    private boolean terminated = false;
    private List<Integer> founded = new ArrayList<>();

    public synchronized int getNext() {
        return ++index;
    }

    public boolean hasNext() {
        return !terminated;
    }

    public void addFounded(int found) {

        synchronized (founded) {
            founded.add(found);
        }

        terminated = true;
    }

    public List<Integer> getFounded() {
        return founded;
    }
}

class CounterFactory {

    private static Counter counter;

    private CounterFactory() {
    }

    public static Counter getSingleton() {
        if (counter != null) {
            return counter;
        }
        synchronized (CounterFactory.class) {
            if (counter == null) {
                counter = new Counter();
            }
            return counter;
        }
    }
}