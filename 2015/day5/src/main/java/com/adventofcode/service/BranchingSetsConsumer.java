package com.adventofcode.service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.adventofcode.repository.SetRepository;

public class BranchingSetsConsumer<Q> implements Consumer<Q> {

    private final Collection<Map.Entry<Predicate<Q>, SetRepository<Q>>> entries;

    public BranchingSetsConsumer(Collection<Map.Entry<Predicate<Q>, SetRepository<Q>>> entries) {

        this.entries = entries;
    }

    @Override
    public void accept(Q element) {

        entries
                .stream()
                .map(entry -> createFuture(entry, element))
                .forEach(CompletableFuture::join);
    }

    private CompletableFuture<Void> createFuture(Map.Entry<Predicate<Q>, SetRepository<Q>> entry, Q element) {

        return CompletableFuture
                .runAsync(() -> {

                    Predicate<Q> predicate = entry.getKey();
                    SetRepository<Q> repository = entry.getValue();

                    if (!predicate.test(element)) {
                        return;
                    }

                    repository.add(element);
                });
    }
}
