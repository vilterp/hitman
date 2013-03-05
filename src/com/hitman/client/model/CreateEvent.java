package com.hitman.client.model;

import org.joda.time.DateTime;

public class CreateEvent extends GameEvent {

    private DateTime dateTime;
    private String creator;

    public CreateEvent(DateTime dateTime, String creator) {
        super(dateTime);
        this.creator = creator;
    }

    @Override
    public String getHumanReadableDescr() {
        // TODO: if it's you
        return String.format("%s created this game.", creator);
    }

}
