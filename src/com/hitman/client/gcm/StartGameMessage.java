package com.hitman.client.gcm;

public class StartGameMessage extends GCMMessage {

    private String target;

    public StartGameMessage(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

}
