package com.hitman.client.event;

import org.joda.time.DateTime;

public class YouJoinedEvent extends JoinEvent {

    public YouJoinedEvent(DateTime dateTime) {
        super(dateTime, null);
    }

    @Override
    public String getHumanReadableDescr() {
        return String.format("You joined this game");
    }

}
