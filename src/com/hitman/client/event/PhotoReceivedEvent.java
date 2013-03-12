package com.hitman.client.event;

import org.joda.time.DateTime;

public class PhotoReceivedEvent extends GameEvent {

    private final String path;

    public PhotoReceivedEvent(DateTime dateTime, String path) {
        super(dateTime);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String getHumanReadableDescr() {
        return "You received a photo of your target. You can only view it once!";
    }

}
