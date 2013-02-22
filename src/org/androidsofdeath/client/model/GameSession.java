package org.androidsofdeath.client.model;

import android.location.Location;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The static methods in here are blocking. Call them
 * within an AsyncTask.
 */
public class GameSession implements Serializable {

    public static final String TAG = "HITMAN_GameSession";

    public static final String SENDER_ID = "791109992959";
    private static final String SERVER_HOST = "hitman.kevinzhang.org";
    private static final int SERVER_PORT = 80;

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

    private static HttpResponse execAppPost(String path, Map<String, String> params) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String postUrl = String.format("http://%s:%d/%s/", SERVER_HOST, SERVER_PORT, path);
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
