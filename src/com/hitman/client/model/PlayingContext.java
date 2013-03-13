package com.hitman.client.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import com.google.common.base.Function;
import com.hitman.client.event.KillEvent;
import com.hitman.client.http.Either;
import com.hitman.client.http.HTTPMethod;
import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.joda.time.DateTime;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayingContext extends LoggedInContext {

    private static final String PHOTO_TEMP_FILE = "photo.tmp.png";
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

    /**
     * caution -- this is blocking
     * @return newly updated GameStorage object
     */
    public GameStorage reloadGameStorage() {
        try {
            gameStorage = GameStorage.read(getAndroidContext());
            return gameStorage;
        } catch (GameStorage.NoGameException e) {
            throw new RuntimeException(e);
        }
    }

    public Either<Object, HttpResponse> updateLocation(Location loc) {
        checkClosed();
        Map<String,String> params = new HashMap<String,String>();
        params.put("location", String.format("%f,%f", loc.getLatitude(), loc.getLongitude()));
        return Either.collapse(
                execNormalRequest("/games/sensors/location/create/",
                        params, HTTPMethod.POST, CONTENT_TYPE_ANY)
                        .bindRight(expectCodes(201)));
    }

    public void leaveGame() {
        checkClosed();
        closed = true;
        gameStorage.clear();
        getSessionStorage().clearTarget();
    }

    private void checkClosed() {
        if(closed) {
            throw new IllegalStateException("context is closed");
        }
    }

    /**
     * caution -- blocking
     * @param image
     * @param photosetId
     * @return resp
     */
    public Either<Object,HttpResponse> uploadImage(Bitmap image, int photosetId) {
        File file = new File(getAndroidContext().getFilesDir(), PHOTO_TEMP_FILE);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        image.compress(Bitmap.CompressFormat.PNG, 100, out);
        try {
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<FormBodyPart> formBodyParts = new ArrayList<FormBodyPart>();
        try {
            formBodyParts.add(new FormBodyPart("photoset", new StringBody(Integer.toString(photosetId))));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        formBodyParts.add(new FormBodyPart("photo", new FileBody(file)));
        Either<Object,HttpResponse> resp =
                Either.collapse(
                        execUploadRequest("/games/sensors/camera/upload/", formBodyParts, HTTPMethod.POST, CONTENT_TYPE_ANY)
                                .bindRight(expectCodes(201)));
        getAndroidContext().deleteFile(PHOTO_TEMP_FILE);
        return resp;
    }

    public Either<Object, Boolean> sendKillCode(String code) {
        Map<String,String> params = new HashMap<String, String>();
        params.put("kill_code", code);
        return Either.collapse(
                execNormalRequest("/games/kill/", params, HTTPMethod.POST, CONTENT_TYPE_ANY)
                        .bindRight(expectCodes(200, 403)))
               .bindRight(new Function<HttpResponse, Boolean>() {
                   public Boolean apply(HttpResponse response) {
                       return response.getStatusLine().getStatusCode() == 200;
                   }
               });
    }
}
