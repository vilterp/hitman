package org.androidsofdeath.client;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gcm.GCMBaseIntentService;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class GCMIntentService extends GCMBaseIntentService {

    private static final String SENDER_ID = "791109992959";
    private static final String SERVER_HOST = "vm1.kevinzhang.org";
    private static final int SERVER_PORT = 9000;
    public static final String REGISTER_TAG = "REGISTER";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onError(Context context, String errorId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onRegistered(Context context, final String registrationId) {
        Toast.makeText(context, String.format("onRegistered: %s", registrationId), 100).show();
        new Thread(new Runnable() {
            public void run() {
                Log.d(REGISTER_TAG, "sending registration");
                HttpClient httpClient = new DefaultHttpClient();
                String postUrl = String.format("http://%s:%d/update_gcmid/", SERVER_HOST, SERVER_PORT);
                HttpPost httpPost = new HttpPost(postUrl);

                // TODO: actually send username
                List<NameValuePair> args = new ArrayList<NameValuePair>();
                args.add(new BasicNameValuePair("user", "admin"));
                args.add(new BasicNameValuePair("regid", registrationId));
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(args));
                    HttpResponse response = httpClient.execute(httpPost);
                    Toast.makeText(GCMIntentService.this, "HTTP response should have been sent", 100).show();
                } catch (UnsupportedEncodingException e) {
                    Log.e(REGISTER_TAG, "exception", e);
                } catch (ClientProtocolException e) {
                    Log.e(REGISTER_TAG, "exception", e);
                } catch (IOException e) {
                    Log.e(REGISTER_TAG, "exception", e);
                }

            }
        }).run();
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.d(REGISTER_TAG, "unregister");
    }

}
