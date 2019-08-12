package com.example.artem.chatapp;

/**
 * Created by Artem on 29.03.2018.
 */

public class Conv {

    public boolean seen;

    public Long timestamp;

    public Conv(){}

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
