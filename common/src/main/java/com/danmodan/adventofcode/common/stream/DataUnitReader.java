package com.danmodan.adventofcode.common.stream;

import java.io.BufferedReader;
import java.io.IOException;

interface DataUnitReader {

    DataUnit read(BufferedReader bReader) throws IOException;
}

class CharDataUnitReader implements DataUnitReader {

    final boolean ignoreNL;

    CharDataUnitReader(boolean ignoreNL) {
        this.ignoreNL = ignoreNL;
    }

    CharDataUnitReader() {
        this(false);
    }

    @Override
    public DataUnit read(BufferedReader bReader) throws IOException {

        int read = -1;

        do {
            read = bReader.read();
        } while(ignoreNL && read == '\n');

        return new CharDataUnit(read);
    }
}

class LineDataUnitReader implements DataUnitReader {

    @Override
    public DataUnit read(BufferedReader bReader) throws IOException {

        return new LineDataUnit(bReader.readLine());
    }
}