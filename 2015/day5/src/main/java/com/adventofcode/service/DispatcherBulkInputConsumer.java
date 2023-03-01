package com.adventofcode.service;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.adventofcode.repository.QueueRepository;

public class DispatcherBulkInputConsumer<Q> implements Consumer<Collection<String>> {

    private final QueueRepository<Q> queueRepository;
    private final Function<String, Q> instantiationFuncMap;

    public DispatcherBulkInputConsumer(
            QueueRepository<Q> queueRepository,
            Function<String, Q> instantiationFuncMap) {
        this.queueRepository = queueRepository;
        this.instantiationFuncMap = instantiationFuncMap;
    }

    @Override
    public void accept(Collection<String> rawMessages) {
        List<Q> elementsInstances = rawMessages
            .stream()
            .map(instantiationFuncMap)
            .collect(Collectors.toList());

        queueRepository.addAll(elementsInstances);
    }
}