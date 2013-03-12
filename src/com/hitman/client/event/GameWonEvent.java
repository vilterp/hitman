package com.hitman.client.event;

import org.joda.time.DateTime;

public class GameWonEvent extends GameEvent {


    public GameWonEvent(DateTime dateTime) {
        super(dateTime);
    }

    @Override
    public String getHumanReadableDescr() {
        return "Congratulations, you won!";
    }

}
