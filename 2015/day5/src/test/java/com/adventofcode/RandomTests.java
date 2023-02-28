package com.adventofcode;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.Test;

import com.adventofcode.model.Text;

import redis.clients.jedis.search.querybuilder.Value;

public class RandomTests {

    @Test
    public void doTest() {

        CompletableFuture
            .supplyAsync(() -> {

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String value = "wrggegukhhatygfi";
                return new Text(value.toString());
            })
            .thenApply(previous -> {

                return Stream.of(
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Test vagais");
                        return textMatch("\\b(?=(.*[aeiou]){3,}).*\\b", previous.getData());
                    }),
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Test repeticao");
                        return textMatch("\\b(?=.*(.)\\1).*\\b", previous.getData());
                    }),
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Test negacao");
                        return textMatch("\\b(?!.*(ab|cd|pq|xy)).*\\b", previous.getData());
                    })
                )
                .map(CompletableFuture::join);
            })
            .thenAccept(stream -> {
                stream.forEach(System.out::println);
            });

        try {
            System.out.println("entrou sleep:");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("fim:");
    }

    private static boolean textMatch(String pattern, String text) {

        return Pattern.matches(pattern, text);
    }
}
