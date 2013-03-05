package com.hitman.client.http;

import android.util.Log;
import com.google.common.base.Function;
import com.hitman.client.Util;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AuthContext {

    private static final String TAG = "HITMAN-AuthContext";
    public static final List<Header> NO_HEADERS = new ArrayList<Header>(0);
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_ANY = "*/*";

    public abstract String getDomain();
    public abstract int getPort();
    public abstract List<Header> getHeaders();

    public static Function<HttpResponse,Either<IOException,String>> getBody =
        new Function<HttpResponse, Either<IOException, String>>() {
            public Either<IOException, String> apply(HttpResponse response) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    return new Right<IOException, String>(reader.readLine());
                } catch (IOException e) {
                    return new Left<IOException, String>(e);
                }
            }
        };

    public static Function<HttpResponse,Either<UnexpectedResponseStatusException,HttpResponse>> expectCodes(final int... codes) {
        return new Function<HttpResponse, Either<UnexpectedResponseStatusException, HttpResponse>>() {
            public Either<UnexpectedResponseStatusException, HttpResponse> apply(HttpResponse response) {
                int respCode = response.getStatusLine().getStatusCode();
                for(int i = 0; i < codes.length; i++) {
                    if(respCode == codes[i]) {
                        return new Right<UnexpectedResponseStatusException, HttpResponse>(response);
                    }
                }
                return new Left<UnexpectedResponseStatusException, HttpResponse>(
                                new UnexpectedResponseStatusException(response, codes));
            }
        };
    }

    public Either<Object,JSONObject> getJsonObjectExpectCodes(String path, Map<String,String> params,
                                                              HTTPMethod method, int... codes) {
        return Util.collapse(
                 Util.collapse(
                   Util.collapse(
                     execRequest(path, params, method, CONTENT_TYPE_JSON)
                   .bindRight(expectCodes(codes)))
                 .bindRight(getBody))
               .bindRight(Util.parseJsonObject));
    }

    public Either<Object,JSONArray> getJsonArrayExpectCodes(String path, Map<String,String> params,
                                                             HTTPMethod method, int... codes) {
        return Util.collapse(
                 Util.collapse(
                   Util.collapse(
                     execRequest(path, params, method, CONTENT_TYPE_JSON)
                   .bindRight(expectCodes(codes)))
                 .bindRight(getBody))
               .bindRight(Util.parseJsonArray));
    }

    public Either<IOException,HttpResponse> execRequest(String path, Map<String, String> params,
                                                         HTTPMethod method, String acceptType) {
        assert path.charAt(0) == '/';
        Log.i(TAG, String.format("%s %s %s", method, path, params));
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = String.format("http://%s:%d%s/", getDomain(), getPort(), path);
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
            try {
                postOrPut.setEntity(new UrlEncodedFormEntity(args));
            } catch (UnsupportedEncodingException e) {
                return new Left<IOException,HttpResponse>(e);
            }
            httpReq = postOrPut;
        }

        httpReq.setHeader("accept", acceptType);
        for (Header header : getHeaders()) {
            httpReq.setHeader(header);
        }

        try {
            return new Right<IOException, HttpResponse>(httpClient.execute(httpReq));
        } catch (IOException e) {
            return new Left<IOException, HttpResponse>(e);
        }
    }

}
