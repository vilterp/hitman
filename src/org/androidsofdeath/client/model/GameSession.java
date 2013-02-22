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

    private LoginCredentials credentials;

    public GameSession(LoginCredentials credentials) {
        this.credentials = credentials;
    }

    public static GameSession doSignup(LoginCredentials credentials) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user", credentials.getUsername());
        params.put("password", credentials.getPassword());
        params.put("gcm_regid", credentials.getGcmId());
        String path = "/users/signup";
        // TODO: flow when signup doesn't work (e.g. username already taken)
        execAppPost(path, params);
        return new GameSession(credentials);
    }

    public static GameSession doLogin(LoginCredentials credentials) {
        // TODO: not implemented on the server yet
        Map<String, String> params = new HashMap<String, String>();
        params.put("gcm_regid", credentials.getGcmId());
        params.put("user", credentials.getUsername());
        params.put("password", credentials.getPassword());
        HttpResponse response = execAppPost("/users/login", params);
        if(response.getStatusLine().getStatusCode() == 200) {
            return new GameSession(credentials);
        } else {
            return null;
        }
    }

    public void updateLocation(Location location) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("gcm_regid", credentials.getGcmId());
        params.put("lat", Double.toString(location.getLatitude()));
        params.put("lon", Double.toString(location.getLongitude()));
        execAppPost("/locations/update", params);
    }

    public Set<Game> getGameList() throws IOException, JSONException {
        JSONArray gamesJson = getJSONArray("/games/");
        Set<Game> games = new HashSet<Game>();
        for (int i = 0; i < gamesJson.length(); i++) {
            JSONObject gameObjJson = gamesJson.getJSONObject(i);
            Location loc = new Location(HITMAN_API_PROVIDER);
            loc.setLatitude(gameObjJson.getDouble("location_lat"));
            loc.setLongitude(gameObjJson.getDouble("location_long"));
            DateTime startDate = DateTime.parse(gameObjJson.getString("start_time"));
            Game game = new Game(
                gameObjJson.getInt("id"),
                gameObjJson.getString("name"),
                loc,
                gameObjJson.getInt("num_players"),
                startDate
            );
            games.add(game);
        }
        return games;
    }

    private JSONArray getJSONArray(String path) throws IOException, JSONException {
        return new JSONArray(getJSONString(path));
    }

    private String getJSONString(String path) throws IOException {
        assert path.charAt(0) == '/';
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = String.format("http://%s:%d%s?format=json", SERVER_HOST, SERVER_PORT, path);
        HttpGet httpGet = new HttpGet(url);
        // http://stackoverflow.com/questions/9605913/how-to-parse-json-in-android
        HttpResponse response = httpClient.execute(httpGet);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        return reader.readLine();
    }

    /**
     * Post to the server....
     *
     * @param path should start with a /
     * @param params POST params
     * @return Http response object
     */
    private static HttpResponse execAppPost(String path, Map<String, String> params) {
        assert path.charAt(0) == '/';
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String postUrl = String.format("http://%s:%d%s/", SERVER_HOST, SERVER_PORT, path);
        HttpPost httpPost = new HttpPost(postUrl);

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

}
