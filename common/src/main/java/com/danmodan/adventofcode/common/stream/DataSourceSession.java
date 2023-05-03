package com.danmodan.adventofcode.common.stream;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;

public class DataSourceSession {

    private final DataUnitReader dataUnitReader;
    private final BlockingQueueReadDataDestiny readDataDestiny;
    private final Connector connector;
    private Function transfromFunc;

    private DataSourceSession(
            DataUnitReader dataUnitReader,
            BlockingQueueReadDataDestiny readDataDestiny,
            Connector connector,
            Function transfromFunc) {
        this.dataUnitReader = dataUnitReader;
        this.readDataDestiny = readDataDestiny;
        this.connector = connector;
        this.transfromFunc = transfromFunc;
    }

    public DataSourceSession connect() {

        try {
            connector.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public <R> R poll() {
        DataUnit polled = readDataDestiny.poll();
        if(polled == null) {
            return null;
        }
        return (R) transfromFunc.apply(polled.getRawData());
    }

    public <R> void performOnData(Consumer<R> consumerFunc) {

        R r = null;
        while ((r = poll()) != null) {
            consumerFunc.accept(r);
        }
    }

    public static ConnectorBuilder readStream() {

        return new Builder().connector();
    }

    public static class DataUnitReaderBuilder {

        private final Builder builder;

        private DataUnitReaderBuilder(Builder builder) {
            this.builder = builder;
        }

        public Builder asChar(boolean ignoreNL, Function<Character, ?> transfromFunc) {
            builder.dataUnitReader = new CharDataUnitReader(ignoreNL);
            builder.transfromFunc = transfromFunc;
            return builder;
        }

        public Builder asLine(Function<String, ?> transfromFunc) {
            builder.dataUnitReader = new LineDataUnitReader();
            builder.transfromFunc = transfromFunc;
            return builder;
        }

        public Builder asChar(boolean ignoreNL) {
            return asChar(ignoreNL, Function.identity());
        }

        public Builder asLine() {
            return asLine(Function.identity());
        }
    }

    public static class ReadDataDestinyBuilder {

        private final Builder builder;

        private ReadDataDestinyBuilder(Builder builder) {
            this.builder = builder;
        }

        public Builder toBlockingQueue(int limit) {
            builder.readDataDestiny = new BlockingQueueReadDataDestiny(limit);
            return builder;
        }
    }

    public static class ConnectorBuilder {

        private final Builder builder;

        private ConnectorBuilder(Builder builder) {
            this.builder = builder;
        }

        public Builder fromFile(URI uri) {
            builder.createConnector = i -> new FileConnector(uri, i);
            return builder;
        }

        public Builder fromSocket(String host, int port) {
            builder.createConnector = i -> new SocketConnector(host, port, i);
            return builder;
        }
    }

    public static class Builder {

        private DataUnitReader dataUnitReader = new CharDataUnitReader();
        private BlockingQueueReadDataDestiny readDataDestiny = new BlockingQueueReadDataDestiny(16);
        private Function<InputDataReader, Connector> createConnector;
        private Function<?, ?> transfromFunc = Function.identity();

        public DataUnitReaderBuilder dataUnit() {
            return new DataUnitReaderBuilder(this);
        }

        public ReadDataDestinyBuilder readDataDestiny() {
            return new ReadDataDestinyBuilder(this);
        }

        public ConnectorBuilder connector() {
            return new ConnectorBuilder(this);
        }

        public DataSourceSession load() {
            var connector = createConnector.apply(new InputDataReader(dataUnitReader, readDataDestiny));
            return new DataSourceSession(dataUnitReader, readDataDestiny, connector, transfromFunc);
        }
    }
}