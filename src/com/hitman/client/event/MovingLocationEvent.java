package com.hitman.client.event;

import org.joda.time.DateTime;

public class MovingLocationEvent extends GameEvent {

    private String placeName1;
    private String placeName2;

    public MovingLocationEvent(DateTime dateTime, String placeName1, String placeName2) {
        super(dateTime);
        this.placeName1 = placeName1;
        this.placeName2 = placeName2;
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("Target moved from %s to %s", placeName1, placeName2);
    }

}
