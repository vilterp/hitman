package com.hitman.client.event;

import org.joda.time.DateTime;

public class GameStartedEvent extends TargetAssignedEvent {

    public GameStartedEvent(DateTime dateTime, String newTarget) {
        super(dateTime, newTarget);
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("Game Started! Your initial target is \"%s\"", getNewTarget());
    }

}
