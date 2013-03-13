package com.hitman.client.event;

import org.joda.time.DateTime;

public class GameCanceledEvent extends GameEvent {

    public GameCanceledEvent(DateTime now) {
        super(now);
    }

    @Override
    public String getHumanReadableDescr() {
        return "The game was canceled because you were the only player";
    }

}
