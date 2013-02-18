package org.androidsofdeath.client;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Util {

    public static final String TAG = "Util";

    public static final String SENDER_ID = "791109992959";
    private static final String SERVER_HOST = "vm1.kevinzhang.org";
    private static final int SERVER_PORT = 9000;

    public static HttpResponse sendHTTPPost(Map<String, String> params, String action, CookieStore cookieStore) {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        String postUrl = String.format("http://%s:%d/%s/", SERVER_HOST, SERVER_PORT, action);
        HttpPost httpPost = new HttpPost(postUrl);

        List<NameValuePair> args = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> param : params.entrySet()) {
            args.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        // TODO: this should probably just throw errors
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(args));
            return httpClient.execute(httpPost, context);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "exception", e);
        } catch (ClientProtocolException e) {
            Log.e(TAG, "exception", e);
        } catch (IOException e) {
            Log.e(TAG, "exception", e);
        } finally {
//            httpClient.getConnectionManager().shutdown();
        }
        return null;
    }

}
