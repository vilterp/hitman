package org.androidsofdeath.client;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Util {

    public static void handleError(Context ctx, Object e) {
        Log.e(String.format("HITMAN-%s", ctx.getClass().getSimpleName()), e.toString());
        Toast.makeText(ctx, "An error occured. Try again?", Toast.LENGTH_LONG).show();
    }

}
