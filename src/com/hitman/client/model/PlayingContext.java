package com.hitman.client.model;

import android.content.Context;
import android.location.Location;
import com.hitman.client.Util;
import com.hitman.client.http.Either;
import com.hitman.client.http.HTTPMethod;
import org.apache.http.HttpResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PlayingContext extends LoggedInContext {

    private GameStorage gameStorage;
    private boolean closed;

    public static PlayingContext readFromStorage(LoggedInContext ctx) throws GameStorage.NoGameException {
        GameStorage gameStorage = GameStorage.read(ctx.getAndroidContext());
        return new PlayingContext(ctx.getAndroidContext(), ctx.getCredentials(), gameStorage);
    }

    public static PlayingContext createFromJoin(LoggedInContext ctx, Game game) {
        GameStorage gameStorage = GameStorage.create(ctx.getAndroidContext(), game);
        return new PlayingContext(ctx.getAndroidContext(), ctx.getCredentials(), gameStorage);
    }

    protected PlayingContext(Context androidContext, LoginCredentials credentials, GameStorage gameStorage) {
        super(androidContext, credentials);
        this.gameStorage = gameStorage;
        closed = false;
    }

    public GameStorage getGameStorage() {
        return gameStorage;
    }

    public Either<Object, HttpResponse> updateLocation(Location loc) {
        checkClosed();
        Map<String,String> params = new HashMap<String,String>();
        params.put("location", String.format("%f,%f", loc.getLatitude(), loc.getLongitude()));
        return Util.collapse(
                execRequest("/games/sensors/location/update", params, HTTPMethod.POST, CONTENT_TYPE_ANY)
                        .bindRight(expectCodes(201)));
    }

    public void leaveGame() {
        checkClosed();
        closed = true;
        gameStorage.clear();
    }

    private void checkClosed() {
        if(closed) {
            throw new IllegalStateException("context is closed");
        }
    }

}
