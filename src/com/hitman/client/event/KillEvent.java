package com.hitman.client.event;

import org.joda.time.DateTime;

public class KillEvent extends GameEvent {

    private String victim;

    public KillEvent(DateTime dateTime, String victim) {
        super(dateTime);
        this.victim = victim;
    }

    public String getVictim() {
        return victim;
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("\"%s\" was killed.", victim);
    }

}
