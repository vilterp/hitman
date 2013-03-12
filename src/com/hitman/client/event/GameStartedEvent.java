package com.hitman.client.event;

import org.joda.time.DateTime;

public class GameStartedEvent extends TargetAssignedEvent {

    private final String killCode;

    public GameStartedEvent(DateTime dateTime, String newTarget, String killCode) {
        super(dateTime, newTarget);
        this.killCode = killCode;
    }

    public String getKillCode() {
        return killCode;
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("Game Started! Your initial target is \"%s\"", getNewTarget());
    }

}
