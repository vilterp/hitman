package com.hitman.client.event;

import org.joda.time.DateTime;

public class KillEvent extends GameEvent {

    private String target;

    public KillEvent(DateTime dateTime, String target) {
        super(dateTime);
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("You killed \"%s\".", target);
    }

}
