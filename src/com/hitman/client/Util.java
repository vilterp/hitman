package com.hitman.client;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.common.base.Function;
import com.hitman.client.http.Either;
import com.hitman.client.http.Left;
import com.hitman.client.http.Right;
import com.hitman.client.http.WrongSideException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Util {

    public static Function<String,Either<JSONException,JSONObject>> parseJsonObject =
        new Function<String, Either<JSONException, JSONObject>>() {
            public Either<JSONException, JSONObject> apply(String s) {
                try {
                    return new Right<JSONException, JSONObject>(new JSONObject(s));
                } catch (JSONException e) {
                    return new Left<JSONException, JSONObject>(e);
                }
            }
        };

    public static Function<String,Either<JSONException,JSONArray>> parseJsonArray =
            new Function<String, Either<JSONException, JSONArray>>() {
                public Either<JSONException, JSONArray> apply(String s) {
                    try {
                        return new Right<JSONException, JSONArray>(new JSONArray(s));
                    } catch (JSONException e) {
                        return new Left<JSONException, JSONArray>(e);
                    }
                }
            };

    public static void handleError(Context ctx, Object e) {
        Log.e(String.format("HITMAN-%s", ctx.getClass().getSimpleName()), e.toString());
        Toast.makeText(ctx, "An error occured. Try again?", Toast.LENGTH_LONG).show();
    }

}
