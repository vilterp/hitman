package org.androidsofdeath.client.model;

import android.content.Context;
import android.location.Location;
import org.androidsofdeath.client.activity.ShowGame;
import org.androidsofdeath.client.http.Either;
import org.androidsofdeath.client.http.HTTPMethod;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
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
        return collapse(
            execRequest("/games/sensors/location/update", params, HTTPMethod.POST, CONTENT_TYPE_ANY)
        .bindRight(expectCodes(201)));
    }

}
