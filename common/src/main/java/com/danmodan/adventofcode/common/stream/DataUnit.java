package com.danmodan.adventofcode.common.stream;

interface DataUnit<T> {

    boolean isEnd();

    T getRawData();
}

class CharDataUnit implements DataUnit<Character> {

    final int rawData;

    CharDataUnit(int rawData) {
        this.rawData = rawData;
    }

    @Override
    public boolean isEnd() {
        return rawData == -1;
    }

    @Override
    public Character getRawData() {
        return (char) rawData;
    }
}

class LineDataUnit implements DataUnit<String> {

    final String rawData;

    LineDataUnit(String rawData) {
        this.rawData = rawData;
    }

    @Override
    public boolean isEnd() {
        return rawData == null;
    }

    @Override
    public String getRawData() {
        return rawData;
    }
}