package com.danmodan.adventofcode.day6.model;

import java.util.function.LongBinaryOperator;

public class BigBinary {

    public final long[] bitArray;

    public BigBinary(int binaryLength) {
        this.bitArray = new long[binaryLength / 64 + 1];
    }

    public void bitOp(LongBinaryOperator bitOpFunc, BigBinary mask) {

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