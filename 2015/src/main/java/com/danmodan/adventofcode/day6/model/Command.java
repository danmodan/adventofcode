package com.danmodan.adventofcode.day6.model;

public class Command {

    public final Action action;
    public final Point point1;
    public final Point point2;

    public Command(Action action, Point point1, Point point2) {
        this.action = action;
        this.point1 = point1;
        this.point2 = point2;
    }
}
