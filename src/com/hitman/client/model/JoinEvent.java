package com.hitman.client.model;

import org.joda.time.DateTime;

public class JoinEvent extends GameEvent {

    private String user;

    public JoinEvent(DateTime dateTime, String user) {
        super(dateTime);
        this.user = user;
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("User %s joined.", user);
    }

}
