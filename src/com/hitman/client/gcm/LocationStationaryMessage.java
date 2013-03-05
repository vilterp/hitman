package com.hitman.client.gcm;

public class LocationStationaryMessage extends GCMMessage {

    private String target;
    private String placeName;

    public LocationStationaryMessage(String target, String location) {
        this.target = target;
        this.placeName = location;
    }

    public String getTarget() {
        return target;
    }

    public String getPlaceName() {
        return placeName;
    }

}
