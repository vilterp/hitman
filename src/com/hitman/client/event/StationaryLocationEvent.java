package com.hitman.client.event;

import org.joda.time.DateTime;

public class StationaryLocationEvent extends GameEvent {

    private String placeName;

    public StationaryLocationEvent(DateTime dateTime, String placeName) {
        super(dateTime);
        this.placeName = placeName;
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("Your target was seen at %s", placeName);
    }

    public String getPlaceName() {
        return placeName;
    }

}
