package day6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LitBulb {

    private static final String REGEX = "^(toggle|turn off|turn on)|(\\d{0,3},\\d{0,3}(?= through))|((?<=through )\\d{0,3},\\d{0,3})";
    private static final String SHORT_INPUT_FILE_PATH = "file:day6/short-input.txt";
    private static final String MEDIUM_INPUT_FILE_PATH = "file:day6/medium-input.txt";
    private static final String INPUT_FILE_PATH = "file:day6/input.txt";

    public static void main(String[] args) throws Exception {

        runWithShortInput();
    }

    private static void runWithShortInput() throws IOException {

        List<Command> commands = getCommandList(SHORT_INPUT_FILE_PATH);

        ShortBulbGrid shortBulbGrid = new ShortBulbGrid();

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

    final int[] grid = new int[10];

    private int createMask(int fromX, int toX) {

        return fromX == 0 ? 
            -1 << (31 - toX) : 
            (-1 << -fromX) ^ (-1 << (31 - toX));
    }

    synchronized void process(Command command) {

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