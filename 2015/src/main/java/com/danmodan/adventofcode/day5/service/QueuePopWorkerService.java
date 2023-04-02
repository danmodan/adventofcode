package com.danmodan.adventofcode.day5.service;

import java.util.Optional;
import java.util.function.Consumer;

import com.danmodan.adventofcode.day5.repository.QueueRepository;

public class QueuePopWorkerService<Q> {

    private final QueueRepository<Q> queueRepository;
    private final Consumer<Q> poppedElementConsumer;

    public QueuePopWorkerService(
            QueueRepository<Q> queueRepository,
            Consumer<Q> poppedElementConsumer) {
        this.queueRepository = queueRepository;
        this.poppedElementConsumer = poppedElementConsumer;
    }

    public void process() {

        process(5);
    }

    public void process(int maxAttempts) {

        int attempt = maxAttempts;
        while (attempt > 0) {

            Optional<Q> popped = queueRepository.pop();

            if (popped.isEmpty()) {
                attempt--;
                continue;
            }

            Q element = popped.get();

            poppedElementConsumer.accept(element);

            attempt = maxAttempts;
        }
    }
}