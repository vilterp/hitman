package com.hitman.client.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.google.common.base.Function;
import com.hitman.client.Util;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
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
                    StringBuilder builder = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    while(true) {
                        String read = reader.readLine();
                        if(read == null) break;
                        builder.append(read);
                    }
                    return new Right<IOException, String>(builder.toString());
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
                     execNormalRequest(path, params, method, CONTENT_TYPE_JSON)
                   .bindRight(expectCodes(codes)))
                 .bindRight(getBody))
               .bindRight(Util.parseJsonObject));
    }

    public Either<Object,JSONArray> getJsonArrayExpectCodes(String path, Map<String,String> params,
                                                             HTTPMethod method, int... codes) {
        return Util.collapse(
                Util.collapse(
                        Util.collapse(
                                execNormalRequest(path, params, method, CONTENT_TYPE_JSON)
                                        .bindRight(expectCodes(codes)))
                                .bindRight(getBody))
                        .bindRight(Util.parseJsonArray));
    }

    public static Function<HttpResponse,Either<IOException,InputStream>> getResultInputStream =
            new Function<HttpResponse, Either<IOException, InputStream>>() {
                public Either<IOException, InputStream> apply(HttpResponse resp) {
                    try {
                        return new Right<IOException, InputStream>(resp.getEntity().getContent());
                    } catch (IOException e) {
                        return new Left<IOException, InputStream>(e);
                    }
                }
            };

    public static Function<InputStream,Bitmap> decodeBitmap = new Function<InputStream, Bitmap>() {
        public Bitmap apply(InputStream inputStream) {
            return BitmapFactory.decodeStream(inputStream);
        }
    };

    public Either<Object,Bitmap> getBitmap(String path) {
        return Util.collapse(
                 Util.collapse(
                   execNormalRequest(path, null, HTTPMethod.GET, CONTENT_TYPE_ANY)
                 .bindRight(expectCodes(200)))
               .bindRight(getResultInputStream))
               .bindRight(decodeBitmap);
    }

    public Either<IOException,HttpResponse> execNormalRequest(String path, Map<String, String> params,
                                                              HTTPMethod method, String acceptType) {
        String url = getUriForPath(path);
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
            MultipartEntity entity = new MultipartEntity();

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

        return execRequest(httpReq, acceptType);
    }

    private String getUriForPath(String path) {
        assert path.charAt(0) == '/';
        return String.format("http://%s:%d%s", getDomain(), getPort(), path);
    }

    public Either<IOException, HttpResponse> execUploadRequest(String path, List<FormBodyPart> parts,
                                                               HTTPMethod method, String acceptType) {
        HttpEntityEnclosingRequestBase httpReq = null;
        String url = getUriForPath(path);
        if(method.equals(HTTPMethod.POST)) {
            httpReq = new HttpPost(url);
        } else if(method.equals(HTTPMethod.PUT)) {
            httpReq = new HttpPut(url);
        } else {
            throw new IllegalArgumentException("gotta be this or that: post or put");
        }
        MultipartEntity entity = new MultipartEntity();
        for (FormBodyPart part : parts) {
            entity.addPart(part);
        }
        httpReq.setEntity(entity);

        return execRequest(httpReq, acceptType);
    }

    public Either<IOException, HttpResponse> execRequest(HttpUriRequest httpReq, String acceptType) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpReq.setHeader("accept", acceptType);
        for (Header header : getHeaders()) {
            httpReq.setHeader(header);
        }

        try {
            HttpResponse resp = httpClient.execute(httpReq);
            Log.i(TAG, String.format("%s %s %s => %d",
                    httpReq.getRequestLine().getMethod(), httpReq.getRequestLine().getUri(),
                    httpReq.getParams(), resp.getStatusLine().getStatusCode()));
            return new Right<IOException, HttpResponse>(resp);
        } catch (IOException e) {
            return new Left<IOException, HttpResponse>(e);
        }
    }

}
