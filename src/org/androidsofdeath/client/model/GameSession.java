package org.androidsofdeath.client.model;

import android.location.Location;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;

/**
 * The static methods in here are blocking. Call them
 * within an AsyncTask.
 */
public class GameSession implements Serializable {

    public static final String TAG = "HITMAN_GameSession";

    public static final String SENDER_ID = "791109992959";
    private static final String SERVER_HOST = "hitman.kevinzhang.org";
    private static final int SERVER_PORT = 80;
    public static final String HITMAN_API_PROVIDER = "HITMAN_API_PROVIDER";
    public static final String AUTH_HEADER = "X-GCMID";

    private LoginCredentials credentials;
    private Game currentGame;

    public GameSession(LoginCredentials credentials, Game game) {
        this.credentials = credentials;
        this.currentGame = game;
    }

    public static GameSession doSignup(LoginCredentials credentials) throws IOException, ApiException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user", credentials.getUsername());
        params.put("password", credentials.getPassword());
        params.put("gcm_regid", credentials.getGcmId());
        String path = "/users/signup";
        expectCodes(execNoAuthReq(path, params, HTTPMethod.POST), 200);
        return new GameSession(credentials, null);
    }

    public static GameSession doLogin(LoginCredentials credentials) throws IOException, ApiException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("gcm_regid", credentials.getGcmId());
        params.put("username", credentials.getUsername());
        params.put("password", credentials.getPassword());
        expectCodes(execNoAuthReq("/users/login", params, HTTPMethod.POST), 200);
        return new GameSession(credentials, null);
    }

    public void updateLocation(Location location) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("gcm_regid", credentials.getGcmId());
        params.put("lat", Double.toString(location.getLatitude()));
        params.put("lon", Double.toString(location.getLongitude()));
        execAuthdReq("/locations/update", params, HTTPMethod.POST);
    }

    public Set<Game> getGameList() throws IOException, JSONException, ApiException {
        JSONArray gamesJson = new JSONArray(getBody(expectCodes(execAuthdReq("/games/", null, HTTPMethod.GET), 200)));
        Set<Game> games = new HashSet<Game>();
        for (int i = 0; i < gamesJson.length(); i++) {
            games.add(gameFromJsonObject(gamesJson.getJSONObject(i)));
        }
        return games;
    }

    public Game createGame(Game game) throws IOException, JSONException, ApiException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", game.getName());
        params.put("start_time", game.getStartDate().toString(ISODateTimeFormat.dateTime()));
        params.put("location", game.getLocation().formatCommaSep());
        return gameFromJsonObject(new JSONObject(
                getBody(expectCodes(execAuthdReq("/games/create/", params, HTTPMethod.POST), 201))));
    }

    public void joinGame(Game game) throws ApiException, IOException {
        expectCodes(execAuthdReq(String.format("/games/%d/join", game.getId()),
                new HashMap<String, String>(), HTTPMethod.PUT), 200);
    }

    public Game getGame(int id) throws IOException, JSONException, ApiException {
        return gameFromJsonObject(new JSONObject(getBody(expectCodes(execAuthdReq(
                String.format("/games/%d/", id), null, HTTPMethod.GET), 200))));
    }
    
    private static Game gameFromJsonObject(JSONObject obj) throws JSONException {
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
        return new Game(
            obj.getInt("id"),
            obj.getString("name"),
            loc,
            players,
            startDate,
            true
        );
    }

    private static String getBody(HttpResponse resp) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
        return reader.readLine();
    }

    private static HttpResponse expectCodes(HttpResponse resp, int... codes) throws ApiException {
        int respCode = resp.getStatusLine().getStatusCode();
        for (int code : codes) {
            if(code == respCode) {
                return resp;
            }
        }
        throw new ApiException(resp, codes);
    }

    private HttpResponse execAuthdReq(String path, Map<String, String> params, HTTPMethod method) throws IOException {
        return doReq(path, params, this, method);
    }

    /**
     * Post to the server....
     *
     * @param path should start with a /
     * @param params POST params
     * @return Http body object
     */
    private static HttpResponse execNoAuthReq(String path, Map<String, String> params, HTTPMethod method)
            throws IOException {
        return doReq(path, params, null, method);
    }

    private static HttpResponse doReq(String path, Map<String, String> params, GameSession session, HTTPMethod method)
            throws IOException {
        assert path.charAt(0) == '/';
        Log.i(TAG, String.format("%s %s %s", method, path, params));
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = String.format("http://%s:%d%s/", SERVER_HOST, SERVER_PORT, path);
        if(params == null) {
            params = new HashMap<String, String>();
        }
        HttpUriRequest httpReq = null;
        if(method.equals(HTTPMethod.GET)) {
            HttpGet get = new HttpGet(url);
            for (Map.Entry<String, String> param: params.entrySet()) {
                get.getParams().setParameter(param.getKey(), param.getValue());
            }
            httpReq = get;
        } else {
            HttpEntityEnclosingRequestBase postOrPut = null;
            if(method.equals(HTTPMethod.PUT)) {
                postOrPut = new HttpPut(url);
            } else {
                postOrPut = new HttpPost(url);
            }
            List<NameValuePair> args = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> param : params.entrySet()) {
                args.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }
            postOrPut.setEntity(new UrlEncodedFormEntity(args));
            httpReq = postOrPut;
        }

        httpReq.setHeader("accept", "application/json");
        if(session != null) {
            httpReq.setHeader(AUTH_HEADER, session.credentials.getGcmId());
        }

        return httpClient.execute(httpReq);
    }

    public LoginCredentials getCredentials() {
        return credentials;
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public GameSession joinedGame(Game res) {
        return new GameSession(credentials, res);
    }

    enum HTTPMethod {
        GET,
        POST,
        PUT
    }

}
