package com.danmodan.adventofcode.day5.repository;

import java.util.List;
import java.util.Optional;

public interface QueueRepository<T> {

    void add(T message);

    void addAll(List<T> messages);

    Optional<T> pop();
}