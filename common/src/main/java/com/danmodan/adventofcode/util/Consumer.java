package com.danmodan.adventofcode.util;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer {

    private static final Logger log = Logger.getLogger(Consumer.class.getName());

    private final SynchronizedQueue queue;
    private final java.util.function.Consumer consumerFunc;
    private final long delayMilliseconds;

    private Consumer(
            SynchronizedQueue queue,
            java.util.function.Consumer<?> consumerFunc,
            long delayMilliseconds) {

        this.queue = queue;
        this.consumerFunc = consumerFunc;
        this.delayMilliseconds = delayMilliseconds;
    }

    public void run() throws TimeElapsedException {

        try {
            while (true) {

                Object msg = getMessage();

                log.log(Level.INFO, "cosuming msg: " + msg);

                consumerFunc.accept(msg);
            }
        } catch (InterruptedException e) {
            log.log(Level.SEVERE, null, e);
        }
    }

    private Object getMessage() throws InterruptedException {

        Object msg = null;

        if (delayMilliseconds > 0) {
            Thread.sleep(delayMilliseconds);
        }

        try {

            queue.getLock().lock();

            while ((msg = queue.poll()) == null) {

                boolean elapsed = queue.getCondition().await(500, TimeUnit.MILLISECONDS);
                if (!elapsed) {
                    throw new TimeElapsedException("Time elapsed");
                }
            }

        } finally {
            queue.getCondition().signalAll();
            queue.getLock().unlock();
        }

        return msg;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private SynchronizedQueue queue;
        private java.util.function.Consumer<?> consumerFunc;
        private long delayMilliseconds = 0;

        public Builder queue(SynchronizedQueue queue) {
            this.queue = queue;
            return this;
        }

        public Builder consumerFunc(java.util.function.Consumer<?> consumerFunc) {
            this.consumerFunc = consumerFunc;
            return this;
        }

        public Builder delayMilliseconds(long delayMilliseconds) {
            this.delayMilliseconds = delayMilliseconds;
            return this;
        }

        public Consumer build() {

            Objects.requireNonNull(queue);
            Objects.requireNonNull(consumerFunc);
            Objects.requireNonNull(delayMilliseconds);

            return new Consumer(
                    queue,
                    consumerFunc,
                    delayMilliseconds);
        }
    }

    public static class TimeElapsedException extends RuntimeException {
        public  TimeElapsedException(String msg) {
            super(msg);
        }
    }
}