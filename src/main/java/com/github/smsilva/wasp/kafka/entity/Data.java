package com.github.smsilva.wasp.kafka.entity;

import java.util.Random;

public class Data {

    private String id;
    private String name;

    public Data() {
        int randomId = new Random().nextInt(1000);
        this.setId(String.valueOf(randomId));
        this.setName("Name #" + randomId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
