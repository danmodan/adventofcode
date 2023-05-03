package com.danmodan.adventofcode.day2;

import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.danmodan.adventofcode.common.config.LoggerConfig;
import com.danmodan.adventofcode.common.stream.DataSourceSession;

public class WrappingPaperCalculator {

    private static final int MAX_CONSUMER_THREADS = 10;
    private static final int MAX_QUEUE_SIZE = 100;
    private static final String PATH = "2015/build/resources/main/day2/input.txt";

    public static void main(String[] args) {

        var logConfig = new LoggerConfig(Level.FINEST);
        logConfig.createRootLogger();
        var log = Logger.getLogger(WrappingPaperCalculator.class.getName());

        var session = DataSourceSession
                .readStream()
                .fromFile(Paths.get(PATH).toUri())
                .dataUnit().asLine(line -> {
                    String[] split = line.split("x");
                    String lenght = split[0];
                    String width = split[1];
                    String height = split[2];
                    return new Dimension(lenght, width, height);
                })
                .readDataDestiny().toBlockingQueue(MAX_QUEUE_SIZE)
                .load();

        Calculator calculator = new Calculator();

        CompletableFuture
                .allOf(
                        Stream
                                .concat(
                                        Stream.of(CompletableFuture.runAsync(session::connect)),
                                        Stream.generate(() -> CompletableFuture
                                                .runAsync(() -> session.performOnData(calculator::addDimensions)))
                                                .limit(MAX_CONSUMER_THREADS))
                                .toArray(CompletableFuture[]::new))
                .thenRun(() -> log.info("" + calculator.getTotalWrappingArea()))
                .join();
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