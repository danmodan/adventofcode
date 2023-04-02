package com.danmodan.adventofcode.util;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.danmodan.adventofcode.log.LoggerFactory;

public class Producer {

    private static final Logger log = LoggerFactory.getLogger(Producer.class);

    private final Path path;
    private final SynchronizedQueue queue;
    private final int maxQueueSize;
    private final Reader reader;

    private Producer(
            Path path,
            Reader reader,
            SynchronizedQueue queue,
            int maxQueueSize) {

        this.path = path;
        this.reader = reader;
        this.queue = queue;
        this.maxQueueSize = maxQueueSize;
    }

    public void run() {

        try (BufferedReader br = Files.newBufferedReader(path)) {

            reader.read(br, this);

        } catch (Exception e) {
            log.log(Level.SEVERE, null, e);
        }
    }

    public void enqueueMessage(Object msg) throws InterruptedException {

        try {

            queue.getLock().lock();

            while (queue.size() > maxQueueSize) {
                log.info("fila encheu");
                queue.getCondition().await();
            }

            queue.offer(msg);

            log.log(Level.INFO, "producing msg: " + msg);

        } finally {
            queue.getCondition().signalAll();
            queue.getLock().unlock();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Path path;
        private SynchronizedQueue queue;
        private int maxQueueSize;
        private Reader reader;

        public Builder path(Path path) {
            this.path = path;
            return this;
        }

        public Builder queue(SynchronizedQueue queue) {
            this.queue = queue;
            return this;
        }

        public Builder maxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
            return this;
        }

        public Builder reader(Reader reader) {
            this.reader = reader;
            return this;
        }

        public Producer build() {

            Objects.requireNonNull(path);
            Objects.requireNonNull(queue);
            Objects.requireNonNull(maxQueueSize);
            Objects.requireNonNull(reader);

            return new Producer(
                    path,
                    reader,
                    queue,
                    maxQueueSize);
        }
    }
}