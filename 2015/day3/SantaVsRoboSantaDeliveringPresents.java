package day3;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import log.LoggerFactory;

public class SantaVsRoboSantaDeliveringPresents {

    private static final String PATH = "day3/input.txt";
    private static final int MAX_QUEUE_SIZE = 100;
    private static final int CONSUMER_THREADS_AMOUNT = 2;

    public static final void main(String... args) {

        ThreadSafeQueue santaQueue = new ThreadSafeQueue(MAX_QUEUE_SIZE);
        ThreadSafeQueue roboQueue = new ThreadSafeQueue(MAX_QUEUE_SIZE);
        Semaphore semaphore = new Semaphore(0);
        SantaTrack santaTrack = new SantaTrack();
        SantaTrack roboTrack = new SantaTrack();

        Thread[] threads = new Thread[CONSUMER_THREADS_AMOUNT + 3];

        // producer
        threads[0] = createProducerThread(santaQueue, semaphore, Paths.get(PATH), true);
        threads[1] = createProducerThread(roboQueue, semaphore, Paths.get(PATH), false);

        // answer
        threads[2] = createAnswerThread(santaTrack, roboTrack, semaphore, threads.length - 1);

        // consumer

        for (int i = 3; i < threads.length; i++) {
            if(i % 2 != 0) {

                threads[i] = createConsumerThread(santaQueue, santaTrack, semaphore);
            } else {
                
                threads[i] = createConsumerThread(roboQueue, roboTrack, semaphore);
            }
        }

        LoggerFactory.setRootLoggerLevel(Level.SEVERE);
        AnswerCollector.log.setLevel(Level.INFO);
        StepConsumer.log.setLevel(Level.INFO);

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join(5_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Thread createProducerThread(
            ThreadSafeQueue queue,
            Semaphore semaphore,
            Path path,
            boolean oddSearch) {
        // Thread t = new StepReaderProducer(queue, path, semaphore, oddSearch);
        var t = new StepReaderProducer(queue, path, semaphore, oddSearch);
        t.setDaemon(true);
        return t;
    }

    private static Thread createAnswerThread(
        SantaTrack santaTrack,
        SantaTrack roboTrack,
            Semaphore semaphore,
            int permits) {
        Thread t = new AnswerCollector(semaphore, permits, santaTrack, roboTrack);
        t.setDaemon(true);
        return t;
    }

    private static Thread createConsumerThread(
            ThreadSafeQueue queue,
            SantaTrack santaTrack,
            Semaphore semaphore) {
        Thread t = new StepConsumer(queue, santaTrack, semaphore);
        t.setDaemon(true);
        return t;
    }
}

class AnswerCollector extends Thread {

    public static final Logger log = LoggerFactory.getLogger(AnswerCollector.class);

    private final Semaphore semaphore;
    private final int permits;
    private final SantaTrack santaTrack;
    private final SantaTrack roboTrack;

    AnswerCollector(
            Semaphore semaphore,
            int permits,
            SantaTrack santaTrack, 
            SantaTrack roboTrack) {
        this.semaphore = semaphore;
        this.permits = permits;
        this.santaTrack = santaTrack;
        this.roboTrack = roboTrack;
    }

    @Override
    public void run() {

        try {
            acquireLock();
            Set<Location> allLocations = new HashSet<>();
            allLocations.addAll(santaTrack.getAllDistinctLocations());
            allLocations.addAll(roboTrack.getAllDistinctLocations());
            log.info(String.format("Total delivered: %d", allLocations.size()));
        } catch (Exception e) {
            log.log(Level.SEVERE, null, e);
        }
    }

    private void acquireLock() {

        try {
            semaphore.acquire(permits);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class StepConsumer extends Thread {

    public static final Logger log = LoggerFactory.getLogger(StepConsumer.class);

    private final ThreadSafeQueue queue;
    private final SantaTrack santaTrack;
    private final Semaphore semaphore;

    StepConsumer(
            ThreadSafeQueue queue,
            SantaTrack santaTrack,
            Semaphore semaphore) {
        this.queue = queue;
        this.santaTrack = santaTrack;
        this.semaphore = semaphore;
        setName("C-" + getName());
    }

    @Override
    public void run() {

        try {
            while (true) {

                Direction direction = queue.poll();

                if (direction == null) {
                    break;
                }

                processMessage(direction);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, null, e);
        } finally {
            semaphore.release();
        }
    }

    private void processMessage(Direction direction) {

        var newLocation = santaTrack.goToLocation(direction);
        log.log(Level.INFO, newLocation.toString());
    }
}

class StepReaderProducer extends Thread {

    private static final Logger log = LoggerFactory.getLogger(StepReaderProducer.class);

    private static final char NEW_LINE = '\n';
    private static final char EOF = (char) -1;
    private final ThreadSafeQueue queue;
    private final Path path;
    private final Semaphore semaphore;
    private final Function<BufferedReader, Character> searchFunc;

    StepReaderProducer(
            ThreadSafeQueue queue,
            Path path,
            Semaphore semaphore,
            boolean oddSearch) {
        this.queue = queue;
        this.path = path;
        this.semaphore = semaphore;
        setName("P-" + getName());

        if(oddSearch) {

            searchFunc = this::oddSearch;
        } else {

            searchFunc = this::evenSearch;
        }
    }

    @Override
    public void run() {

        try (BufferedReader br = Files.newBufferedReader(path)) {

            do {

                char read = searchFunc.apply(br);

                if (read == NEW_LINE || read == EOF) {
                    queue.terminate();
                    break;
                }

                enqueueMessage(read);

            } while (true);

        } catch (Exception e) {
            log.log(Level.SEVERE, null, e);
        } finally {
            semaphore.release();
        }
    }

    private Character oddSearch(BufferedReader br) {

        try {
            char read = (char) br.read();
            br.skip(1);
            return read;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Character evenSearch(BufferedReader br) {

        try {
            br.skip(1);
            return (char) br.read();    
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void enqueueMessage(char read) {

        Direction direction = Direction.getByCode(read);
        log.log(Level.INFO, direction.name());
        queue.offer(direction);
    }
}

class ThreadSafeQueue {

    private final Queue<Direction> queue;
    private final int maxQueueSize;
    private boolean terminated = false;
    private boolean isEmpty = true;

    ThreadSafeQueue(int maxQueueSize) {
        this.queue = new LinkedList<>();
        this.maxQueueSize = maxQueueSize;
    }

    synchronized void offer(Direction direction) {

        while (queue.size() == maxQueueSize) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        queue.offer(direction);
        terminated = false;
        isEmpty = false;
        notify();
    }

    synchronized Direction poll() {

        while (isEmpty && !terminated) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (queue.size() == 1) {
            isEmpty = true;
        }

        Direction direction = queue.poll();

        if (direction != null) {
            notifyAll();
        }

        return direction;
    }

    synchronized void terminate() {

        terminated = true;
        notifyAll();
    }
}

/**
 * _._._._._._._._._.
 * _._._._._._._._._.
 * _._._._._._._._._.
 * _._._._._ _._._._.
 * _._._._ _._ _._._.
 * _._._._._ _._._._.
 * _._._._._._._._._.
 * _._._._._._._._._.
 */
enum Direction {
    NORTH('^'),
    SOUTH('v'),
    EAST('>'),
    WEST('<');

    final char code;

    private Direction(char code) {
        this.code = code;
    }

    static Direction getByCode(char code) {

        for (Direction value : values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new NoSuchElementException(String.format("No %s with code %s", Direction.class.getName(), code));
    }
}

class Location {

    private final int x;
    private final int y;
    private Location previousLocation;

    Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    public Location getPreviousLocation() {
        return previousLocation;
    }

    public void setPreviousLocation(Location previousLocation) {
        this.previousLocation = previousLocation;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Location other = (Location) obj;
        return x == other.x &&
                y == other.y;
    }

    @Override
    public String toString() {
        return "Location [x=" + x + ", y=" + y + "]";
    }
}

class SantaTrack {

    private final Stack<Location> stack;

    public SantaTrack() {
        this.stack = new Stack<>();
        goToLocation(new Location(0, 0));
    }

    synchronized Location goToLocation(Direction direction) {
        Location currentLocation = getCurrentLocation();
        var nextLocation = NextLocationConstructor.getNextLocation(currentLocation, direction);
        nextLocation.setPreviousLocation(currentLocation);
        stack.add(nextLocation);
        return nextLocation;
    }

    synchronized Location goToLocation(Location location) {

        Location currentLocation = getCurrentLocation();
        location.setPreviousLocation(currentLocation);
        stack.add(location);
        return location;
    }

    Location getCurrentLocation() {

        if(stack.isEmpty()) {
            return null;
        }

        return stack.peek();
    }

    Set<Location> getAllDistinctLocations() {
        return new HashSet<>(stack);
    }
}

class NextLocationConstructor {

    private NextLocationConstructor() {
    }

    static Location getNextLocation(Location location, Direction direction) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(direction);

        int nextX = location.getX();
        int nextY = location.getY();

        switch (direction) {
            case NORTH:
                nextY++;
                break;
            case SOUTH:
                nextY--;
                break;
            case EAST:
                nextX++;
                break;
            case WEST:
                nextX--;
                break;
        }

        return new Location(nextX, nextY);
    }
}