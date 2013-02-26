package org.androidsofdeath.client.model;

import android.location.Location;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
    public static final String AUTH_HEADER = "GCMID";

    private LoginCredentials credentials;
    private Game currentGame;

    public GameSession(LoginCredentials credentials, Game game) {
        this.credentials = credentials;
        this.currentGame = game;
    }

    public static GameSession doSignup(LoginCredentials credentials) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user", credentials.getUsername());
        params.put("password", credentials.getPassword());
        params.put("gcm_regid", credentials.getGcmId());
        String path = "/users/signup";
        // TODO: flow when signup doesn't work (e.g. username already taken)
        execNoAuthPost(path, params);
        return new GameSession(credentials, null);
    }

    public static GameSession doLogin(LoginCredentials credentials) {
        // TODO: not implemented on the server yet
        Map<String, String> params = new HashMap<String, String>();
        params.put("gcm_regid", credentials.getGcmId());
        params.put("username", credentials.getUsername());
        params.put("password", credentials.getPassword());
        HttpResponse response = execNoAuthPost("/users/login", params);
        if(response.getStatusLine().getStatusCode() == 200) {
            return new GameSession(credentials, null);
        } else {
            return null;
        }
    }

    public void updateLocation(Location location) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("gcm_regid", credentials.getGcmId());
        params.put("lat", Double.toString(location.getLatitude()));
        params.put("lon", Double.toString(location.getLongitude()));
        execAuthdPost("/locations/update", params);
    }

    public Set<Game> getGameList() throws IOException, JSONException {
        JSONArray gamesJson = getJSONArray("/games/");
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
        params.put("location_lat", Double.toString(game.getLocation().getLatitude()));
        params.put("location_long", Double.toString(game.getLocation().getLongitude()));
        HttpResponse resp = execAuthdPost("/games/create/", params);
        if(resp.getStatusLine().getStatusCode() == 201) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
            String respContents = reader.readLine();
            return gameFromJsonObject(new JSONObject(respContents));
        } else {
            throw new ApiException(resp.getStatusLine().getStatusCode());
        }
    }

    public void joinGame(Game game) throws ApiException {
        int respCode = execAuthdPost(String.format("/games/%d/join", game.getId()), new HashMap<String, String>())
                .getStatusLine().getStatusCode();
        if(respCode != 200) {
            throw new ApiException(respCode);
        }
    }

    public Game getGame(int id) throws IOException, JSONException {
        return gameFromJsonObject(getJSONObject(String.format("/games/%d/", id)));
    }
    
    private static Game gameFromJsonObject(JSONObject obj) throws JSONException {
        Location loc = new Location(HITMAN_API_PROVIDER);
        loc.setLatitude(obj.getDouble("location_lat"));
        loc.setLongitude(obj.getDouble("location_long"));
        DateTime startDate = DateTime.parse(obj.getString("start_time"));
        Set<Player> players = new HashSet<Player>();
        JSONArray playersJson = obj.getJSONArray("players");
        for (int j = 0; j < playersJson.length(); j++) {
            JSONObject p = playersJson.getJSONObject(j);
            players.add(new Player(p.getString("username"), p.getInt("id")));
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

    private JSONObject getJSONObject(String path) throws IOException, JSONException {
        return new JSONObject(getJSONString(path));
    }
    
    private JSONArray getJSONArray(String path) throws IOException, JSONException {
        return new JSONArray(getJSONString(path));
    }

    private String getJSONString(String path) throws IOException {
        assert path.charAt(0) == '/';
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = String.format("http://%s:%d%s?format=json", SERVER_HOST, SERVER_PORT, path);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(AUTH_HEADER, credentials.getGcmId());
        // http://stackoverflow.com/questions/9605913/how-to-parse-json-in-android
        HttpResponse response = httpClient.execute(httpGet);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        return reader.readLine();
    }

    private HttpResponse execAuthdPost(String path, Map<String, String> params) {
        return doPost(path, params, this);
    }

    /**
     * Post to the server....
     *
     * @param path should start with a /
     * @param params POST params
     * @return Http body object
     */
    private static HttpResponse execNoAuthPost(String path, Map<String, String> params) {
        return doPost(path, params, null);
    }

    private static HttpResponse doPost(String path, Map<String, String> params, GameSession session) {
        assert path.charAt(0) == '/';
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String postUrl = String.format("http://%s:%d%s/", SERVER_HOST, SERVER_PORT, path);
        HttpPost httpPost = new HttpPost(postUrl);

        if(session != null) {
            httpPost.setHeader(AUTH_HEADER, session.credentials.getGcmId());
        }

        List<NameValuePair> args = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> param : params.entrySet()) {
            args.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(args));
            HttpResponse response = httpClient.execute(httpPost);
            return response;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null; // TODO: probably not the best way to handle errors
        }
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

    public class ApiException extends Exception {

        private int status;

        public ApiException(int status) {
            super(String.format("Unexpected status code: %d", status));
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

    }

}
