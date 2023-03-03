package com.adventofcode.repository;

import java.util.Set;

public interface SetRepository<T> {

    void add(T message);

    Set<T> intersect(SetRepository<T>... otherRepos);

    Set<T> intersect(String... otherSets);

    Set<T> intersect(byte[]... otherSets);

    String getKeyName();
}
