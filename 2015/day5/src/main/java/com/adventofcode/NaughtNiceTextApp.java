package com.adventofcode;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.adventofcode.model.Text;
import com.adventofcode.repository.JedisQueueRepository;
import com.adventofcode.repository.JedisSetRepository;
import com.adventofcode.repository.QueueRepository;
import com.adventofcode.service.BranchingSetsConsumer;
import com.adventofcode.service.DispatcherBulkInputConsumer;
import com.adventofcode.service.InputReaderService;
import com.adventofcode.service.QueuePopWorkerService;

public class NaughtNiceTextApp {

    private static final int BULK_SIZE = 100;
    private static final String INPUT_TXT = "input.txt";
    private static final Pattern VOWEL_PATTERN = Pattern.compile("\\b(?=(.*[aeiou]){3,}).*\\b");
    private static final Pattern REPETITION_PATTERN = Pattern.compile("\\b(?=.*(.)\\1).*\\b");
    private static final Pattern NEGATE_PAIR_PATTERN = Pattern.compile("\\b(?!.*(ab|cd|pq|xy)).*\\b");

    private static final Predicate<Text> VOWEL_PATTERN_PREDICATE = text -> VOWEL_PATTERN
            .matcher(text.getData()).matches();
    private static final Predicate<Text> REPETITION_PATTERN_PREDICATE = text -> REPETITION_PATTERN
            .matcher(text.getData()).matches();
    private static final Predicate<Text> NEGATE_PAIR_PATTERN_PREDICATE = text -> NEGATE_PAIR_PATTERN
            .matcher(text.getData()).matches();

    // P1: \b(?=(.*[aeiou]){3,})(?=.*(.)\2)(?!.*(ab|cd|pq|xy)).*\b
    // P2: \b(?=.*((..).*\2))(?=.*((.).\4)).*\b

    public static final void main(String... args) throws Exception {

        long start = System.currentTimeMillis();

        QueueRepository<Text> naughtNiceTextListRepository = new JedisQueueRepository<>("NaughtNiceTexts");

        Consumer<Text> poppedElementConsumer = new BranchingSetsConsumer<>(
                Set.of(
                        Map.entry(VOWEL_PATTERN_PREDICATE, new JedisSetRepository<>("VowelNiceTexts")),
                        Map.entry(REPETITION_PATTERN_PREDICATE, new JedisSetRepository<>("RepetitionNiceTexts")),
                        Map.entry(NEGATE_PAIR_PATTERN_PREDICATE, new JedisSetRepository<>("NegateNiceTexts"))));

        QueuePopWorkerService<Text> queuePopWorkerService = new QueuePopWorkerService<>(
                naughtNiceTextListRepository,
                poppedElementConsumer);

        Consumer<Collection<String>> inputConsumer = new DispatcherBulkInputConsumer<>(naughtNiceTextListRepository,
                Text::new);

        InputReaderService inputReaderService = new InputReaderService(inputConsumer, BULK_SIZE);

        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> inputReaderService.read(INPUT_TXT)),
                CompletableFuture.runAsync(() -> queuePopWorkerService.process(2)),
                CompletableFuture.runAsync(() -> queuePopWorkerService.process(2)),
                CompletableFuture.runAsync(() -> queuePopWorkerService.process(2)),
                CompletableFuture.runAsync(() -> queuePopWorkerService.process(2)))
                .orTimeout(20, TimeUnit.SECONDS)
                .thenAccept(prev -> {
                    long stop = System.currentTimeMillis();
                    System.out.println("fim: " + (stop - start));
                })
                .join();
    }
}
