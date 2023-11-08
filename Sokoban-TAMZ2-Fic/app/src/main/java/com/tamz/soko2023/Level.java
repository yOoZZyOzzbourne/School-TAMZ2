package com.tamz.soko2023;

import java.util.ArrayList;
import java.util.List;

public class Level {
    private String name;
    private String data;

    // Constructor, getters and setters
    public Level(String name, String data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }
}
