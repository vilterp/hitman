package org.androidsofdeath.client.model;

import android.content.Context;
import android.content.Intent;
import com.google.common.base.Function;
import org.androidsofdeath.client.http.*;
import org.androidsofdeath.client.service.LocationService;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class LoggedInContext extends HitmanContext {

    public static final String AUTH_HEADER = "X-GCMID";

    private List<Header> headers;
    private LoginCredentials credentials;

    public LoggedInContext(LoginCredentials credentials) {
        this.credentials = credentials;
        this.headers = new ArrayList<Header>(1);
        headers.add(new BasicHeader(AUTH_HEADER, credentials.getGcmId()));
    }

    public LoginCredentials getCredentials() {
        return credentials;
    }

    @Override
    public List<Header> getHeaders() {
        return headers;
    }

    private static final Function<JSONObject, Either<JSONException,Game>> gameFromJsonObject =
        new Function<JSONObject, Either<JSONException, Game>>() {
            public Either<JSONException, Game> apply(JSONObject obj) {
                try {
                    LatLng loc = LatLng.parseCommaSep(obj.getString("location"));
                    DateTime startDate = DateTime.parse(obj.getString("start_time"));
                    Set<Player> players = new HashSet<Player>();
                    if(obj.has("players")) {
                        JSONArray playersJson = obj.getJSONArray("players");
                        for (int j = 0; j < playersJson.length(); j++) {
                            JSONObject p = playersJson.getJSONObject(j);
                            players.add(new Player(p.getString("username"), p.getInt("id")));
                        }
                    }
                    Game game = new Game(
                        obj.getInt("id"),
                        obj.getString("name"),
                        loc,
                        players,
                        startDate,
                        true
                    );
                    return new Right<JSONException, Game>(game);
                } catch(JSONException e) {
                    return new Left<JSONException, Game>(e);
                }
            }
        };

    public Either<Object,Game> getGame(int id) {
        return collapse(
                 getJsonObjectExpectCodes(String.format("/games/%d", id), null, HTTPMethod.GET, 200)
               .bindRight(gameFromJsonObject));
    }
    
    public Either<Object,Set<Game>> getGameList() {
        return getJsonArrayExpectCodes("/games", null, HTTPMethod.GET, 200).bindRight(new Function<JSONArray, Set<Game>>() {
            public Set<Game> apply(JSONArray jsonArray) {
                Set<Game> games = new HashSet<Game>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        games.add(gameFromJsonObject.apply(obj).getRight());
                    } catch(JSONException e) {
                        throw new RuntimeException(e);
                    } catch(WrongSideException e) {
                        throw new RuntimeException(e);
                    }
                }
                return games;
            }
        });
    }

    public Either<Object,Game> createGame(Game game) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", game.getName());
        params.put("start_time", game.getStartDate().toString(ISODateTimeFormat.dateTime()));
        params.put("location", game.getLocation().formatCommaSep());
        return collapse(
          getJsonObjectExpectCodes("/games/create", params, HTTPMethod.POST, 201)
        .bindRight(gameFromJsonObject));
    }

    public Either<Object,Either<AlreadyInGameException,PlayingContext>> joinGame(final SessionStorage storage, final Game game) {
        return getJsonObjectExpectCodes(String.format("/games/%d/join", game.getId()), null, HTTPMethod.PUT, 200, 403)
                .bindRight(new Function<JSONObject, Either<AlreadyInGameException, PlayingContext>>() {
                    public Either<AlreadyInGameException, PlayingContext> apply(JSONObject jsonObject) {
                        try {
                            if(jsonObject.getBoolean("success")) {
                                storage.saveGameId(game.getId());
                                return new Right<AlreadyInGameException, PlayingContext>(
                                        new PlayingContext(game, credentials));
                            } else {
                                return new Left<AlreadyInGameException, PlayingContext>(new AlreadyInGameException());
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    public class AlreadyInGameException extends Exception {
    }

}
