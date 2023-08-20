package com.danmodan.adventofcode.day6.model;

public class BrightnessGrid {

    private final int bulbGridSize;
    private final int[][] grid;

    public BrightnessGrid(int bulbGridSize) {

        this.bulbGridSize = bulbGridSize;
        this.grid = new int[bulbGridSize][bulbGridSize];

        for(int i = 0; i < bulbGridSize; i++) {
            for(int j = 0; j < bulbGridSize; j++) {
                grid[i][j] = 0;
            }
        }
    }

    public synchronized void process(Command command) {

        Point point1 = command.point1;
        Point point2 = command.point2;

        int fromX = Math.min(point1.x, point2.x);
        int toX = Math.max(point1.x, point2.x);

        int fromY = Math.min(point1.y, point2.y);
        int toY = Math.max(point1.y, point2.y);

        for(int i = fromY; i <= toY; i++) {
            for(int j = fromX; j <= toX; j++) {
                switch (command.action) {
                    case TOGGLE:
                        grid[i][j] += 2;
                        break;
                    case TURN_ON:
                        grid[i][j]++;
                        break;
                    case TURN_OFF:
                        grid[i][j] = Math.max(grid[i][j] - 1, 0);
                        break;
                }
            }
        }
    }

    public int countBulbsBrightness() {

        int totalBrightness = 0;

        for(int i = 0; i < bulbGridSize; i++) {
            for(int j = 0; j < bulbGridSize; j++) {
                totalBrightness += grid[i][j];
            }
        }

        return totalBrightness;
    }
}
