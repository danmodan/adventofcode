package com.adventofcode.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.adventofcode.model.Text;
import com.adventofcode.repository.JedisSetRepository;
import com.adventofcode.repository.QueueRepository;
import com.adventofcode.repository.SetRepository;

import redis.clients.jedis.JedisPool;

public class TextPopService {

    private static final Pattern VOWEL_PATTERN = Pattern.compile("\\b(?=(.*[aeiou]){3,}).*\\b");
    private static final Pattern REPETITION_PATTERN = Pattern.compile("\\b(?=.*(.)\\1).*\\b");
    private static final Pattern NEGATE_PAIR_PATTERN = Pattern.compile("\\b(?!.*(ab|cd|pq|xy)).*\\b");

    private final QueueRepository<Text> queueRepository;
    private final Map<String, SetRepository<Text>> setRepoByName;
    public final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public TextPopService(
        QueueRepository<Text> queueRepository,
        JedisPool poolConnections
    ) {
        this.queueRepository = queueRepository;
        this.setRepoByName = Map.of(
            "VOWEL_PATTERN", new JedisSetRepository<Text>(poolConnections, "VOWEL_PATTERN"),
            "REPETITION_PATTERN",  new JedisSetRepository<Text>(poolConnections, "REPETITION_PATTERN"),
            "NEGATE_PAIR_PATTERN",  new JedisSetRepository<Text>(poolConnections, "NEGATE_PAIR_PATTERN")
        );
    }

    public void process() {

        threadPool.execute(() -> {

            int maxEmptyCycle = 0;
            while(maxEmptyCycle < 5) {

                Optional<Text> popped = queueRepository.pop();

                if(popped.isEmpty()) {
                    maxEmptyCycle++;
                    continue;
                }

                Text text = popped.get();

                Stream.of(
                    createRegexMatcherFuture(text, VOWEL_PATTERN, "VOWEL_PATTERN"),
                    createRegexMatcherFuture(text, REPETITION_PATTERN, "REPETITION_PATTERN"),
                    createRegexMatcherFuture(text, NEGATE_PAIR_PATTERN, "NEGATE_PAIR_PATTERN")
                ).map(CompletableFuture::join);

                maxEmptyCycle = 0;
            }
        });
    }

    private CompletableFuture<Void> createRegexMatcherFuture(Text text, Pattern pattern, String setName) {

        return CompletableFuture
            .runAsync(() -> {

                boolean matches = pattern.matcher(text.getData()).matches();

                if(!matches) {
                    return;
                }
                setRepoByName.get(setName).add(text);
            });
    }
}