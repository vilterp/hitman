package com.hitman.client.event;

import org.joda.time.DateTime;

import java.io.Serializable;

public abstract class GameEvent implements Serializable {

    private DateTime dateTime;

    protected GameEvent(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public abstract String getHumanReadableDescr();

    public DateTime getDateTime() {
        return dateTime;
    }

}
