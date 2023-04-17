package com.danmodan.adventofcode.common.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

class InputDataReader {

    final DataUnitReader dataUnitReader;
    final ReadDataDestiny readDataDestiny;

    InputDataReader(DataUnitReader dataUnitReader, ReadDataDestiny readDataDestiny) {
        this.dataUnitReader = dataUnitReader;
        this.readDataDestiny = readDataDestiny;
    }

    void read(InputStream is) throws IOException {

        try (Reader reader = new InputStreamReader(is);
                BufferedReader bReader = new BufferedReader(reader);
                readDataDestiny) {

            do {

                DataUnit data = dataUnitReader.read(bReader);

                if (data.isEnd()) {
                    break;
                }

                readDataDestiny.send(data);
            } while (true);
        }
    }
}