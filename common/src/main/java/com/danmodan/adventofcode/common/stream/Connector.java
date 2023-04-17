package com.danmodan.adventofcode.common.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;

interface Connector {

    void connect() throws IOException;
}

class SocketConnector implements Connector {

    final String host;
    final int port;
    final InputDataReader inputDataReader;

    SocketConnector(String host, int port, InputDataReader inputDataReader) {
        this.host = host;
        this.port = port;
        this.inputDataReader = inputDataReader;
    }

    @Override
    public void connect() throws IOException {

        try (
                Socket socket = new Socket(host, port);
                InputStream is = socket.getInputStream()) {

            inputDataReader.read(is);
        }
    }
}

class FileConnector implements Connector {

    final URI uri;
    final InputDataReader inputDataReader;

    FileConnector(URI uri, InputDataReader inputDataReader) {
        this.uri = uri;
        this.inputDataReader = inputDataReader;
    }

    @Override
    public void connect() throws IOException {

        File file = new File(uri);
        FileInputStream fis = new FileInputStream(file);
        inputDataReader.read(fis);
    }
}