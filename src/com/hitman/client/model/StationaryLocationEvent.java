package com.hitman.client.model;

import com.hitman.client.gcm.LocationStationaryMessage;
import org.joda.time.DateTime;

public class StationaryLocationEvent extends GameEvent {

    private String placeName;

    public StationaryLocationEvent(DateTime dateTime, LocationStationaryMessage event) {
        super(dateTime);
        placeName = event.getPlaceName();
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("Your target was seen at %s", placeName);
    }

}
