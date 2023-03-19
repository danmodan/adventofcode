package day6;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.LongBinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LitBulb {

    private static final String REGEX = "^(toggle|turn off|turn on)|(\\d{0,3},\\d{0,3}(?= through))|((?<=through )\\d{0,3},\\d{0,3})";
    private static final String SHORT_INPUT_FILE_PATH = "file:day6/short-input.txt";
    private static final String MEDIUM_INPUT_FILE_PATH = "file:day6/medium-input.txt";
    private static final String INPUT_FILE_PATH = "file:day6/input.txt";

    public static void main(String[] args) throws Exception {

        System.out.println("short grid");
        runWithShortInput(SHORT_INPUT_FILE_PATH, 10);

        System.out.println("medium grid");
        runWithMediumInput(MEDIUM_INPUT_FILE_PATH, 70);

        System.out.println("big grid");
        runWithBigInput(INPUT_FILE_PATH, 1000);
    }

    private static void runWithBigInput(
        String filePath,
        int gridSize
    ) throws IOException {

        List<Command> commands = getCommandList(filePath);

        BulbGrid bulbGrid = new BulbGrid(gridSize);

        commands.forEach(bulbGrid::process);

        try (
            Writer writer = new FileWriter("day6/output.txt");
            BufferedWriter bw = new BufferedWriter(writer);
        ) {

            for(int i = bulbGrid.grid.length - 1 ; i >= 0; i--) {
                BigBinary row = bulbGrid.grid[i];
                String rowTxt = row.toString()
                        .substring(0, bulbGrid.grid.length)
                        .replace('1', '#')
                        .replace('0', '.');
                bw.write(rowTxt + System.lineSeparator());
            }
        }

        System.out.println("lit bulb counter = " + bulbGrid.countLitBulbs());
    }

    private static void runWithMediumInput(
        String filePath,
        int gridSize
    ) throws IOException {

        List<Command> commands = getCommandList(filePath);

        BulbGrid bulbGrid = new BulbGrid(gridSize);

        commands.forEach(bulbGrid::process);

        for(int i = bulbGrid.grid.length - 1 ; i >= 0; i--) {
            BigBinary row = bulbGrid.grid[i];
            System.out.println(
                row.toString()
                    .substring(0, bulbGrid.grid.length)
                    .replace('1', '#')
                    .replace('0', '.')
            );
        }
    }

    private static void runWithShortInput(
        String filePath,
        int gridSize
    ) throws IOException {

        List<Command> commands = getCommandList(filePath);

        ShortBulbGrid shortBulbGrid = new ShortBulbGrid(gridSize);

        commands.forEach(shortBulbGrid::process);

        for(int i = shortBulbGrid.grid.length - 1 ; i >= 0; i--) {
            int row = shortBulbGrid.grid[i];
            System.out.println(
                String
                    .format("%1$32s", Integer.toBinaryString(row))
                    .replace(' ', '0')
                    .substring(0, shortBulbGrid.grid.length)
                    .replace('1', '#')
                    .replace('0', '.')
            );
        }
    }

    private static List<Command> getCommandList(String inputFilePath) throws IOException {
        URL url = new URL(inputFilePath);
        URLConnection conn = url.openConnection();

        try (
                InputStream is = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);) {

            Pattern pattern = Pattern.compile(REGEX);

            List<Command> commands = new ArrayList<>();

            while(true) {
                String line = br.readLine();
                if(line == null || line.isBlank()) {
                    break;
                }
                Matcher matcher = pattern.matcher(line);
                matcher.find();
                Action action = Action.getByCode(matcher.group());
                matcher.find();
                String[] xy1 = matcher.group().split(",");
                matcher.find();
                String[] xy2 = matcher.group().split(",");
                Point point1 = new Point(Integer.parseInt(xy1[0]), Integer.parseInt(xy1[1]));
                Point point2 = new Point(Integer.parseInt(xy2[0]), Integer.parseInt(xy2[1]));
                commands.add(new Command(action, point1, point2));
            }

            return commands;
        }
    }
}

class ShortBulbGrid {

    final int[] grid;

    ShortBulbGrid(int gridSize) {
        this.grid = new int[gridSize];
    }

    private int createMask(int fromX, int toX) {

        return fromX == 0 ? 
            -1 << (31 - toX) : 
            (-1 << -fromX) ^ (-1 << (31 - toX));
    }

    void process(Command command) {

        Point point1 = command.point1;
        Point point2 = command.point2;

        int fromX = Math.min(point1.x, point2.x);
        int toX = Math.max(point1.x, point2.x);
        int mask = createMask(fromX, toX);
        
        int fromY = Math.min(point1.y, point2.y);
        int toY = Math.max(point1.y, point2.y);
        for(int i = fromY; i <= toY; i++) {

            switch (command.action) {
                case TOGGLE: {
                    grid[i] ^= mask;
                    break;
                }
                case TURN_OFF: {
                    grid[i] &= (~mask);
                    break;
                }
                case TURN_ON: {
                    grid[i] |= mask;
                    break;
                }
            }
        }
    }
}

class BulbGrid {

    final BigBinary[] grid;

    BulbGrid(int gridSize) {

        this.grid = new BigBinary[gridSize];

        for(int i = 0; i < grid.length; i++) {
            grid[i] = new BigBinary(grid.length);
        }
    }

    BigBinary createMask(int fromX, int toX) {

        BigBinary mask = new BigBinary(grid.length);

        int fromBlock = fromX / 64;
        int toBlock = toX / 64;

        for(int i = 0; i < mask.bitArray.length; i++) {

            if(fromBlock <= i && i <= toBlock) {

                int fromBlockOffset = i == fromBlock ? fromX % 64 : 0;
                int toBlockOffset = i == toBlock ? toX % 64 : 63;

                mask.bitArray[i] = fromBlockOffset == 0 ? 
                    -1L << (63 - toBlockOffset) : 
                    (-1L << -fromBlockOffset) ^ (-1L << (63 - toBlockOffset));
            }
        }

        return mask;
    }

    void process(Command command) {

        Point point1 = command.point1;
        Point point2 = command.point2;

        int fromX = Math.min(point1.x, point2.x);
        int toX = Math.max(point1.x, point2.x);
        BigBinary mask = createMask(fromX, toX);

        int fromY = Math.min(point1.y, point2.y);
        int toY = Math.max(point1.y, point2.y);

        for(int i = fromY; i <= toY; i++) {

            switch (command.action) {
                case TOGGLE: {
                    grid[i].bitOp((l1, l2) -> l1 ^ l2, mask);
                    break;
                }
                case TURN_OFF: {
                    grid[i].bitOp((l1, l2) -> l1 & (~l2), mask);
                    break;
                }
                case TURN_ON: {
                    grid[i].bitOp((l1, l2) -> l1 | l2, mask);
                    break;
                }
            }
        }
    }

    int countLitBulbs() {

        int counter = 0;

        for (int i = 0; i < grid.length; i++) {
            
            BigBinary row = grid[i];

            for (int j = row.bitArray.length - 1; j >= 0; j--) {
                
                long block = row.bitArray[j];

                int permutCounter = 0;
                while(block != 0 && permutCounter < 64) {

                    if((block & 1L) == 1L) {
                        counter++;
                    }
                    block = block >> 1L;
                    permutCounter++;
                }
            }
        }

        return counter;
    }
}

class Command {

    final Action action;
    final Point point1;
    final Point point2;

    Command(Action action, Point point1, Point point2) {
        this.action = action;
        this.point1 = point1;
        this.point2 = point2;
    }
}

enum Action {

    TOGGLE ("toggle"),
    TURN_OFF ("turn off"),
    TURN_ON ("turn on");

    final String code;

    private Action(String code) {
        this.code = code;
    }

    static Action getByCode(String code) {

        for(Action action : values()) {
            if(action.code.equals(code)) {
                return action;
            }
        }

        throw new NoSuchElementException("No Action with " + code);
    }
}

class Point {

    final int x;
    final int y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class BigBinary {

    final long[] bitArray;

    BigBinary(int binaryLength) {
        this.bitArray = new long[binaryLength / 64 + 1];
    }

    void bitOp(LongBinaryOperator bitOpFunc, BigBinary mask) {

        for (int i = 0; i < bitArray.length; i++) {

            bitArray[i] = bitOpFunc.applyAsLong(bitArray[i], mask.bitArray[i]);
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        for(long block : bitArray) {

            sb.append(
                String
                    .format("%1$64s", Long.toBinaryString(block))
                    .replace(' ', '0')
            );
        }

        return sb.toString();
    }
}