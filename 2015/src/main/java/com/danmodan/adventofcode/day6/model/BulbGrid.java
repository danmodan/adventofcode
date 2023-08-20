package com.danmodan.adventofcode.day6.model;

import com.danmodan.adventofcode.common.annotation.Loggable;

public class BulbGrid {

    private final BigBinary[] grid;

    public BulbGrid(int gridSize) {

        this.grid = new BigBinary[gridSize];

        for (int i = 0; i < grid.length; i++) {
            grid[i] = new BigBinary(grid.length);
        }
    }

    public BigBinary createMask(int fromX, int toX) {

        BigBinary mask = new BigBinary(grid.length);

        int fromBlock = fromX / 64;
        int toBlock = toX / 64;

        for (int i = 0; i < mask.bitArray.length; i++) {

            if (fromBlock <= i && i <= toBlock) {

                int fromBlockOffset = i == fromBlock ? fromX % 64 : 0;
                int toBlockOffset = i == toBlock ? toX % 64 : 63;

                mask.bitArray[i] = fromBlockOffset == 0 ? -1L << (63 - toBlockOffset)
                        : (-1L << -fromBlockOffset) ^ (-1L << (63 - toBlockOffset));
            }
        }

        return mask;
    }

    @Loggable(level = "FINE")
    public synchronized void process(Command command) {

        Point point1 = command.point1;
        Point point2 = command.point2;

        int fromX = Math.min(point1.x, point2.x);
        int toX = Math.max(point1.x, point2.x);
        BigBinary mask = createMask(fromX, toX);

        int fromY = Math.min(point1.y, point2.y);
        int toY = Math.max(point1.y, point2.y);

        for (int i = fromY; i <= toY; i++) {

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

    public int countLitBulbs() {

        int counter = 0;

        for (int i = 0; i < grid.length; i++) {

            BigBinary row = grid[i];

            for (int j = row.bitArray.length - 1; j >= 0; j--) {

                long block = row.bitArray[j];

                int permutCounter = 0;
                while (block != 0 && permutCounter < 64) {

                    if ((block & 1L) == 1L) {
                        counter++;
                    }
                    block = block >> 1L;
                    permutCounter++;
                }
            }
        }

        return counter;
    }

    public BigBinary[] getGrid() {
        return grid;
    }
}