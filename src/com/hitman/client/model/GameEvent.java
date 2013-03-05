package com.hitman.client.model;

import org.joda.time.DateTime;

public abstract class GameEvent {

    private DateTime dateTime;

    protected GameEvent(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public abstract String getHumanReadableDescr();

    public DateTime getDateTime() {
        return dateTime;
    }

}
