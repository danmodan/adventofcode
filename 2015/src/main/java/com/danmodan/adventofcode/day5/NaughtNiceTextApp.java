package com.danmodan.adventofcode.day5;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.danmodan.adventofcode.day5.model.Text;
import com.danmodan.adventofcode.day5.repository.JedisQueueRepository;
import com.danmodan.adventofcode.day5.repository.JedisSetRepository;
import com.danmodan.adventofcode.day5.repository.QueueRepository;
import com.danmodan.adventofcode.day5.repository.SetRepository;
import com.danmodan.adventofcode.day5.service.BranchingSetsConsumer;
import com.danmodan.adventofcode.day5.service.DispatcherBulkInputConsumer;
import com.danmodan.adventofcode.day5.service.InputReaderService;
import com.danmodan.adventofcode.day5.service.QueuePopWorkerService;

public class NaughtNiceTextApp {

    private static final int BULK_SIZE = 10;
    private static final String INPUT_TXT = "day5/input.txt";
    private static final Pattern VOWEL_PATTERN = Pattern.compile("\\b(?=(.*[aeiou]){3,}).*\\b");
    private static final Pattern REPETITION_FORBIDDEN_PATTERN = Pattern.compile("\\b(?=.*(.)\\1).*\\b");
    private static final Pattern NEGATE_PAIR_PATTERN = Pattern.compile("\\b(?!.*(ab|cd|pq|xy)).*\\b");
    private static final Pattern LETTER_PAIR_PATTERN = Pattern.compile("\\b(?=.*((..).*\\2)).*\\b");
    private static final Pattern REPETITION_PATTERN = Pattern.compile("\\b(?=.*((.).\\2)).*\\b");

    private static final Predicate<Text> VOWEL_PATTERN_PREDICATE = text -> VOWEL_PATTERN
            .matcher(text.getData()).matches();
    private static final Predicate<Text> REPETITION_FORBIDDEN_PATTERN_PREDICATE = text -> REPETITION_FORBIDDEN_PATTERN
            .matcher(text.getData()).matches();
    private static final Predicate<Text> NEGATE_PAIR_PATTERN_PREDICATE = text -> NEGATE_PAIR_PATTERN
            .matcher(text.getData()).matches();
    private static final Predicate<Text> LETTER_PAIR_PATTERN_PREDICATE = text -> LETTER_PAIR_PATTERN
            .matcher(text.getData()).matches();
    private static final Predicate<Text> REPETITION_PATTERN_PREDICATE = text -> REPETITION_PATTERN
            .matcher(text.getData()).matches();

    // P1: \b(?=(.*[aeiou]){3,})(?=.*(.)\2)(?!.*(ab|cd|pq|xy)).*\b
    // P2: \b(?=.*((..).*\2))(?=.*((.).\4)).*\b

    public static final void main(String... args) throws Exception {

        long start = System.currentTimeMillis();

        QueueRepository<Text> naughtNiceTextListRepository = new JedisQueueRepository<>("NaughtNiceTexts");

        SetRepository<Text> vowelNiceTexts = new JedisSetRepository<>("VowelNiceTexts");
        SetRepository<Text> repetitionForbiddenNiceTexts = new JedisSetRepository<>("RepetitionForbiddenNiceTexts");
        SetRepository<Text> negateNiceTexts = new JedisSetRepository<>("NegateNiceTexts");
        SetRepository<Text> letterPairNiceTexts = new JedisSetRepository<>("LetterPairNiceTexts");
        SetRepository<Text> repetitionNiceTexts = new JedisSetRepository<>("RepetitionNiceTexts");

        Consumer<Text> poppedElementConsumer = new BranchingSetsConsumer<>(
                Set.of(
                        Map.entry(VOWEL_PATTERN_PREDICATE, vowelNiceTexts),
                        Map.entry(REPETITION_FORBIDDEN_PATTERN_PREDICATE, repetitionForbiddenNiceTexts),
                        Map.entry(NEGATE_PAIR_PATTERN_PREDICATE, negateNiceTexts),
                        Map.entry(LETTER_PAIR_PATTERN_PREDICATE, letterPairNiceTexts),
                        Map.entry(REPETITION_PATTERN_PREDICATE, repetitionNiceTexts)
                ));

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
                CompletableFuture.runAsync(() -> queuePopWorkerService.process(2))
        )
        .thenCompose(prev -> {
                return CompletableFuture
                        .supplyAsync(() -> vowelNiceTexts.intersect(repetitionForbiddenNiceTexts, negateNiceTexts))
                        .thenAcceptBoth(CompletableFuture.supplyAsync(() -> letterPairNiceTexts.intersect(repetitionNiceTexts)), 
                        (prev1, prev2) -> {
                                System.out.println("parte 1: " + prev1.size());
                                System.out.println("parte 2: " + prev2.size());
                        });
        })
        .thenAccept(prev -> {
                long stop = System.currentTimeMillis();
                System.out.println("fim: " + (stop - start));
        })
        .join();
    }
}
