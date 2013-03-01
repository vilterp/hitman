package org.androidsofdeath.client.model;

import android.content.Context;
import android.content.SharedPreferences;
import org.androidsofdeath.client.http.AuthContext;

import java.io.Serializable;

public abstract class HitmanContext extends AuthContext implements Serializable {

    public static final String PREFS_NAME = "HitmanPrefs";

    private SharedPreferences prefs;

    public SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public String getDomain() {
        return "hitman.kevinzhang.org";
    }

    @Override
    public int getPort() {
        return 80;
    }

}
