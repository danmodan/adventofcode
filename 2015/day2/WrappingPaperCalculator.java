package day2;

import java.nio.file.Paths;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import log.LoggerFactory;
import util.Consumer;
import util.Consumer.TimeElapsedException;
import util.Producer;
import util.Reader;
import util.SynchronizedQueue;

public class WrappingPaperCalculator {

    private static final int MAX_CONSUMER_THREADS = 10;
    private static final int MAX_QUEUE_SIZE = 100;
    private static final String PATH = "day2/input.txt";

    public static void main(String[] args) throws InterruptedException {

        SynchronizedQueue queue = new SynchronizedQueue(MAX_QUEUE_SIZE);
        Calculator calculator = new Calculator();
        Semaphore semaphore = new Semaphore(0);

        Producer producer = Producer
                .builder()
                .path(Paths.get(PATH))
                .reader(new Reader.LineReader(line -> {
                    String[] split = line.split("x");
                    String lenght = split[0];
                    String width = split[1];
                    String height = split[2];
                    return new Dimension(lenght, width, height);
                }))
                .queue(queue)
                .maxQueueSize(MAX_QUEUE_SIZE)
                .build();

        Consumer consumer = Consumer
                .builder()
                .consumerFunc((Dimension dimension) -> calculator.addDimensions(dimension))
                .queue(queue)
                .delayMilliseconds(10)
                .build();

        Thread[] threads = new Thread[MAX_CONSUMER_THREADS + 1 + 1];

        // producer
        threads[0] = createProducerThread(semaphore, producer);

        // answer
        threads[1] = createAnswerThread(semaphore, calculator, threads.length - 1);

        // consumer
        for (int i = 2; i < threads.length; i++) {
            threads[i] = createConsumerThread(semaphore, consumer);
        }

        LoggerFactory.setRootLoggerLevel(Level.SEVERE);
        AnswerCaptureRunnable.log.setLevel(Level.INFO);

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join(5_000);
        }
    }

    private static Thread createProducerThread(Semaphore semaphore, Producer producer) {

        var t = new Thread(new ProducerRunnable(semaphore, producer));
        t.setDaemon(true);
        return t;
    }

    private static Thread createConsumerThread(Semaphore semaphore, Consumer consumer) {

        var t = new Thread(new ConsumerRunnable(semaphore, consumer));
        t.setDaemon(true);
        return t;
    }

    private static Thread createAnswerThread(Semaphore semaphore, Calculator calculator, int amountWorkerThreads) {

        var t = new Thread(new AnswerCaptureRunnable(semaphore, calculator, amountWorkerThreads));
        t.setDaemon(true);
        return t;
    }
}

class ProducerRunnable implements Runnable {

    private final Semaphore semaphore;
    private final Producer producer;

    public ProducerRunnable(Semaphore semaphore, Producer producer) {
        this.semaphore = semaphore;
        this.producer = producer;
    }

    @Override
    public void run() {
        producer.run();
        semaphore.release();
    }
}

class ConsumerRunnable implements Runnable {

    private final Semaphore semaphore;
    private final Consumer consumer;

    public ConsumerRunnable(Semaphore semaphore, Consumer consumer) {
        this.semaphore = semaphore;
        this.consumer = consumer;
    }

    @Override
    public void run() {

        try {
            consumer.run();
        } catch (TimeElapsedException e) {
        } finally {
            semaphore.release();
        }
    }
}

class AnswerCaptureRunnable implements Runnable {

    public static final Logger log = LoggerFactory.getLogger(AnswerCaptureRunnable.class);

    private final Semaphore semaphore;
    private final Calculator calculator;
    private final int semaphorePermits;

    public AnswerCaptureRunnable(Semaphore semaphore, Calculator calculator, int semaphorePermits) {
        this.semaphore = semaphore;
        this.calculator = calculator;
        this.semaphorePermits = semaphorePermits;
    }

    @Override
    public void run() {
        lock();
        log.info(String.format("Wrapping area: %.2f - Ribbon feet: %.2f", calculator.getTotalWrappingArea(),
                calculator.getTotalRibbonFeet()));
    }

    private void lock() {
        try {
            semaphore.acquire(semaphorePermits);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }
}

class Dimension {

    final double lenght;
    final double width;
    final double height;

    public Dimension(String lenght, String width, String height) {
        this(
                Double.parseDouble(lenght),
                Double.parseDouble(width),
                Double.parseDouble(height));
    }

    public Dimension(double lenght, double width, double height) {

        this.lenght = lenght;
        this.width = width;
        this.height = height;
    }

    public double getWrappingArea() {

        double areaA = lenght * width;
        double areaB = lenght * height;
        double areaC = width * height;
        double smallestArea = getSmallestDimension(areaA, areaB, areaC);
        double slack = smallestArea;
        return 2 * (areaA + areaB + areaC) + slack;
    }

    public double getRibbonFeet() {

        double bow = lenght * width * height;
        double perimeterA = lenght + width;
        double perimeterB = lenght + height;
        double perimeterC = width + height;
        double smallestPerimeter = 2 * getSmallestDimension(perimeterA, perimeterB, perimeterC);
        return smallestPerimeter + bow;
    }

    private double getSmallestDimension(double a, double b, double c) {

        double smallest = a;

        if (b <= smallest)
            smallest = b;

        if (c <= smallest)
            smallest = c;

        return smallest;
    }

    @Override
    public String toString() {

        return String.format("%.2f x %.2f x %.2f", lenght, width, height);
    }
}

class Calculator {

    private volatile double wrappingArea;
    private volatile double ribbonFeet;

    synchronized void addDimensions(Dimension dimension) {
        sumWrappingArea(dimension.getWrappingArea());
        sumRibbonFeet(dimension.getRibbonFeet());
    }

    synchronized void sumWrappingArea(double area) {

        wrappingArea += area;
    }

    synchronized void sumRibbonFeet(double feet) {

        ribbonFeet += feet;
    }

    public double getTotalRibbonFeet() {
        return ribbonFeet;
    }

    public double getTotalWrappingArea() {
        return wrappingArea;
    }
}