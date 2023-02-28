package com.adventofcode;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.adventofcode.model.Text;
import com.adventofcode.repository.JedisQueueRepository;
import com.adventofcode.repository.QueueRepository;
import com.adventofcode.service.DispatcherBulkInputConsumer;
import com.adventofcode.service.InputReaderService;
import com.adventofcode.service.TextPopService;

import redis.clients.jedis.JedisPool;

public class NaughtNiceTextApp {

    private static final int BULK_SIZE = 100;
    private static final String INPUT_TXT = "input.txt";
    private static final String LIST_NAME = "NaughtNiceTextList";

    // P1: \b(?=(.*[aeiou]){3,})(?=.*(.)\2)(?!.*(ab|cd|pq|xy)).*\b
    // P2: \b(?=.*((..).*\2))(?=.*((.).\4)).*\b

    public static final void main(String... args) throws Exception {

        JedisPool pool = new JedisPool();
        QueueRepository<Text> queueRepository = new JedisQueueRepository<>(pool, LIST_NAME);
        Consumer<Collection<String>> inputConsumer = new DispatcherBulkInputConsumer(queueRepository);
        InputReaderService inputReaderService = new InputReaderService(inputConsumer, BULK_SIZE);

        TextPopService textPopService = new TextPopService(queueRepository, pool);

        textPopService.process();
        textPopService.process();
        textPopService.process();
        inputReaderService.read(INPUT_TXT);



        Thread.sleep(30_000);
        shutdowExecutors(textPopService.threadPool);
    }

    private static void shutdowExecutors(ExecutorService executors) {

        executors.shutdown();
        try {
            if (!executors.awaitTermination(25, TimeUnit.SECONDS)) {
                executors.shutdownNow();
            }
        } catch (InterruptedException e) {
            executors.shutdownNow();
        }
    }
}
