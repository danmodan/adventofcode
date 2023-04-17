package com.danmodan.adventofcode.day6;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.danmodan.adventofcode.day6.model.*;

public class LitBulb {

    private static final String REGEX = "^(toggle|turn off|turn on)|(\\d{0,3},\\d{0,3}(?= through))|((?<=through )\\d{0,3},\\d{0,3})";
    private static final String INPUT_FILE_PATH = "day6/input.txt";

    public static void main(String[] args) throws Exception {

        // List<Command> commands = getCommandList(INPUT_FILE_PATH);

        // BulbGrid bulbGrid = new BulbGrid(1000);

        // commands.forEach(bulbGrid::process);

        // try (
        //     Writer writer = new FileWriter("day6/output.txt");
        //     BufferedWriter bw = new BufferedWriter(writer);
        // ) {

        //     for(int i = bulbGrid.grid.length - 1 ; i >= 0; i--) {
        //         BigBinary row = bulbGrid.grid[i];
        //         String rowTxt = row.toString()
        //                 .substring(0, bulbGrid.grid.length)
        //                 .replace('1', '#')
        //                 .replace('0', '.');
        //         bw.write(rowTxt + System.lineSeparator());
        //     }
        // }

        // System.out.println("lit bulb counter = " + bulbGrid.countLitBulbs());
    }

    private static List<Command> getCommandList(String inputFilePath) throws IOException {

        try (
                InputStream is = LitBulb.class.getClassLoader().getResourceAsStream(inputFilePath);
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