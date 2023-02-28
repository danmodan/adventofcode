package com.adventofcode.model;

import java.io.Serializable;

public class Text implements Serializable {

    private String data;

    public Text(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Text [data=" + data + "]";
    }
}
