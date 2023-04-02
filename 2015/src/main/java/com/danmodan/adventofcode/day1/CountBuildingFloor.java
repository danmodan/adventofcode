package com.danmodan.adventofcode.day1;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

public class CountBuildingFloor {

    private static final String PATH = "day1/input.txt";

    public static final void main(String... args) throws Exception {

        Path path = Paths.get(PATH);
        Queue<Character> queue = new LinkedList<>();
        SantaPosition santaPosition = new SantaPosition();

        Thread log = new LogSantaPosition(santaPosition);
        log.setDaemon(true);

        Thread producer = new ProduceInstructions(path, queue);
        producer.setDaemon(true);

        Thread consumer1 = new ConsumeInstructions(queue, santaPosition);
        consumer1.setDaemon(true);

        Thread consumer2 = new ConsumeInstructions(queue, santaPosition);
        consumer2.setDaemon(true);

        log.start();
        consumer1.start();
        consumer2.start();
        producer.start();
        log.join(10_000);
        System.out.println("first Basement Achive Instruction: " + santaPosition.firstBasementAchiveInstruction);

    }
}

class LogSantaPosition extends Thread {

    private final SantaPosition santaPosition;

    public LogSantaPosition(SantaPosition santaPosition) {
        this.santaPosition = santaPosition;
    }

    @Override
    public void run() {

        while (true) {

            System.out.println("Floor: " + santaPosition.floor);
            try {
                sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class ConsumeInstructions extends Thread {

    private final Queue<Character> queue;
    private final SantaPosition santaPosition;

    public ConsumeInstructions(Queue<Character> queue, SantaPosition santaPosition) {
        this.queue = queue;
        this.santaPosition = santaPosition;
    }

    @Override
    public void run() {

        try {
            while (true) {
                sleep(2);

                synchronized (queue) {
                    while (queue.isEmpty()) {
                        queue.wait();
                    }

                    if(queue.size() == 1) {
                        queue.notifyAll();
                    }

                    switch (queue.poll()) {
                        case '(':
                            santaPosition.goUp();
                            break;
                        case ')':
                            santaPosition.goDown();
                            break;
                    }

                    if(santaPosition.firstBasementAchiveInstruction == null && santaPosition.isBasementFloor()) {
                        santaPosition.firstBasementAchiveInstruction = santaPosition.readInstructions;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ProduceInstructions extends Thread {

    private static final int MAX_QUEUE_SIZE = 50;
    private final Path path;
    private final Queue<Character> queue;

    ProduceInstructions(Path path, Queue<Character> queue) {
        this.path = path;
        this.queue = queue;
    }

    @Override
    public void run() {

        readData();
    }

    private void readCharacter(char character) throws InterruptedException {

        synchronized (queue) {

            while (queue.size() > MAX_QUEUE_SIZE) {
                queue.wait();
            }

            queue.add(character);
            queue.notifyAll();
        }
    }

    private void readData() {
        try (BufferedReader reader = Files.newBufferedReader(path)) {

            char character = 0;
            char endOfFile = (char) -1;
            char newLine = '\n';

            while ((character = (char) reader.read()) != endOfFile) {

                if (character == newLine) {
                    continue;
                }

                readCharacter(character);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class SantaPosition {

    int floor = 0;
    int readInstructions = 0;
    Integer firstBasementAchiveInstruction;

    void goUp() {
        synchronized (this) {

            ++readInstructions;
            ++floor;
        }
    }

    void goDown() {
        synchronized (this) {

            ++readInstructions;
            --floor;
        }
    }

    boolean isBasementFloor() {

        return floor == -1;
    }
}