package com.danmodan.adventofcode.day6.model;

import java.util.NoSuchElementException;

public enum Action {

    TOGGLE ("toggle"),
    TURN_OFF ("turn off"),
    TURN_ON ("turn on");

    final String code;

    private Action(String code) {
        this.code = code;
    }

    public static Action getByCode(String code) {

        for(Action action : values()) {
            if(action.code.equals(code)) {
                return action;
            }
        }

        throw new NoSuchElementException("No Action with " + code);
    }
}