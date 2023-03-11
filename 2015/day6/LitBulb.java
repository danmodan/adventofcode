package day6;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LitBulb {

    
    private static final BulbGrid BULB_GRID = new BulbGrid();

    public static void main(String[] args) throws Exception {

        URL url = new URL("file:day6/input2.txt");
        URLConnection conn = url.openConnection();

        try (
                InputStream is = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);) {

            Pattern pattern = Pattern.compile("^(toggle|turn off|turn on)|(\\d{0,3},\\d{0,3}(?= through))|((?<=through )\\d{0,3},\\d{0,3})");

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

            BulbGrid bulbGrid = new BulbGrid();

            commands.forEach(bulbGrid::process);

            for(int i = bulbGrid.grid.length - 1 ; i >= 0; i--) {
                System.out.println(
                    String.format("%1$" + bulbGrid.grid.length + "s", Integer.toBinaryString(bulbGrid.grid[i])).replace(' ', '0')
                );
            }
        }
    }
}

class BulbGrid {

    final int[] grid = new int[10];

    BulbGrid() {
        for(int i = 0; i < grid.length; i++) {
            grid[i] = 0;
        }
    }

    private int createMask(int fromX, int toX, Function<Boolean, Character> getBitFunction) {

        StringBuilder sb = new StringBuilder();
        for(int j = 0; j < grid.length; j++) {
            sb.append(getBitFunction.apply(fromX <= j && j <= toX));
        }
        return Integer.parseInt(sb.toString(), 2);
    }

    private int createAndMask(int fromX, int toX) {

        return createMask(fromX, toX, bool -> bool ? '0' : '1');
    }

    private int createOrMask(int fromX, int toX) {

        return createMask(fromX, toX, bool -> bool ? '1' : '0');
    }

    synchronized void process(Command command) {

        Point point1 = command.point1;
        Point point2 = command.point2;

        int fromX = Math.min(point1.x, point2.x);
        int toX = Math.max(point1.x, point2.x);
        int andMask = createAndMask(fromX, toX);
        int orMask = createOrMask(fromX, toX);
        
        int fromY = Math.min(point1.y, point2.y);
        int toY = Math.max(point1.y, point2.y);
        for(int i = fromY; i <= toY; i++) {

            int strip = grid[i];

            switch (command.action) {
                case TOGGLE: {
                    grid[i] = strip ^ orMask;
                    break;
                }
                case TURN_OFF: {
                    grid[i] = strip & andMask;
                    break;
                }
                case TURN_ON: {
                    grid[i] = strip | orMask;
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