package com.hitman.client.event;

import org.joda.time.DateTime;

public class TargetAssignedEvent extends GameEvent {

    // TODO: refactor to Player?
    private String newTarget;

    public TargetAssignedEvent(DateTime dateTime, String newTarget) {
        super(dateTime);
        this.newTarget = newTarget;
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("You were assigned \"%s\" as a target", newTarget);
    }

    public String getNewTarget() {
        return newTarget;
    }

}
