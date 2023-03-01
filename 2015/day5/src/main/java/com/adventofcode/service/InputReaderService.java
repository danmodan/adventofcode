package com.adventofcode.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;

public class InputReaderService {

    private final Consumer<Collection<String>> inputConsumer;
    private final int bulkSize;

    public InputReaderService(
            Consumer<Collection<String>> inputConsumer,
            int bulkSize) {
        this.inputConsumer = inputConsumer;
        this.bulkSize = bulkSize;
    }

    public void read(String filepath) {

        try {
            
            List<String> allLines = getAllFileLines(filepath);

            ReadFileTask task = new ReadFileTask(allLines);
            ForkJoinPool
                    .commonPool()
                    .execute(task);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private List<String> getAllFileLines(String filepath) throws IOException, URISyntaxException {
        return Files.readAllLines(Paths.get(getClass().getClassLoader().getResource(filepath).toURI()));
    }

    class ReadFileTask extends RecursiveAction {

        private final List<String> workload;

        public ReadFileTask(List<String> workload) {
            this.workload = workload;
        }

        @Override
        protected void compute() {

            if (workload.size() > bulkSize) {
                invokeAll(divideJob());
            } else {
                doJob();
            }
        }

        private void doJob() {

            inputConsumer.accept(workload);
        }

        private Collection<ReadFileTask> divideJob() {

            int half = workload.size() / 2;

            List<String> firstPart = workload.subList(0, half);
            List<String> secondPart = workload.subList(half, workload.size());

            List<ReadFileTask> splited = new ArrayList<>();
            splited.add(new ReadFileTask(firstPart));
            splited.add(new ReadFileTask(secondPart));
            return splited;
        }
    }
}