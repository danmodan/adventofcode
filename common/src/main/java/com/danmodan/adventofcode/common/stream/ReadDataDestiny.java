package com.danmodan.adventofcode.common.stream;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

interface ReadDataDestiny extends Closeable {

    void send(DataUnit data);
}

class BlockingQueueReadDataDestiny implements ReadDataDestiny {

    final int limit;
    final Queue<DataUnit> queue;
    boolean finished;

    BlockingQueueReadDataDestiny(int limit) {
        this.queue = new ArrayDeque<>(limit);
        this.limit = limit;
    }

    @Override
    public synchronized void close() throws IOException {

        finished = true;
        notify();
    }

    @Override
    public synchronized void send(DataUnit data) {

        offer(data);
    }

    synchronized void offer(DataUnit data) {

        try {

            while (queue.size() + 1 > limit) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            queue.offer(data);
        } finally {
            finished = false;
            notify();
        }
    }

    synchronized DataUnit poll() {

        try {

            while (!finished && queue.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            return queue.poll();
        } finally {
            notifyAll();
        }
    }
}
