package com.adventofcode.service;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.adventofcode.model.Text;
import com.adventofcode.repository.QueueRepository;

public class DispatcherBulkInputConsumer implements Consumer<Collection<String>> {

    private final QueueRepository<Text> queueRepository;

    public DispatcherBulkInputConsumer(QueueRepository<Text> queueRepository) {
        this.queueRepository = queueRepository;
    }

    @Override
    public void accept(Collection<String> message) {
        List<Text> texts = message
            .stream()
            .map(Text::new)
            .collect(Collectors.toList());

        queueRepository.addAll(texts);
    }
}