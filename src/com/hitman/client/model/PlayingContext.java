package com.hitman.client.model;

import android.location.Location;
import com.hitman.client.Util;
import com.hitman.client.http.Either;
import com.hitman.client.http.HTTPMethod;
import org.apache.http.HttpResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PlayingContext extends LoggedInContext implements Serializable {

    private Game game;

    public PlayingContext(Game game, LoginCredentials credentials) {
        super(credentials);
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public Either<Object, HttpResponse> updateLocation(Location loc) {
        Map<String,String> params = new HashMap<String,String>();
        params.put("location", String.format("%f,%f", loc.getLatitude(), loc.getLongitude()));
        return Util.collapse(
                execRequest("/games/sensors/location/update", params, HTTPMethod.POST, CONTENT_TYPE_ANY)
                        .bindRight(expectCodes(201)));
    }

}
