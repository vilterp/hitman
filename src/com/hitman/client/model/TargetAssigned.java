package com.hitman.client.model;

import org.joda.time.DateTime;

public class TargetAssigned extends GameEvent {

    private String newTarget;

    public TargetAssigned(DateTime dateTime, String newTarget) {
        super(dateTime);
        this.newTarget = newTarget;
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("You were assigned %s as a target", newTarget);
    }

}
