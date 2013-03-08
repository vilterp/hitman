package com.hitman.client.event;

import org.joda.time.DateTime;

public class KilledEvent extends GameEvent {

    public KilledEvent(DateTime dateTime) {
        super(dateTime);
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("You were killed. :(");
    }

}
