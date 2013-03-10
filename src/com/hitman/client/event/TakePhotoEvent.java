package com.hitman.client.event;

import org.joda.time.DateTime;

public class TakePhotoEvent extends GameEvent {

    private int photoSetId;

    public TakePhotoEvent(DateTime now, int photoSetId) {
        super(now);
        this.photoSetId = photoSetId;
    }

    public int getPhotoSetId() {
        return photoSetId;
    }

    @Override
    public String getHumanReadableDescr() {
        return "You were prompted to take photos of someone else's target.";
    }

}
