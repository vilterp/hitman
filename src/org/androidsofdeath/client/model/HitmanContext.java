package org.androidsofdeath.client.model;

import android.content.Context;
import android.content.SharedPreferences;
import org.androidsofdeath.client.http.AuthContext;

import java.io.Serializable;

public abstract class HitmanContext extends AuthContext implements Serializable {

    public static final String PREFS_NAME = "HitmanPrefs";

    private Context androidContext;
    private SharedPreferences prefs;

    protected HitmanContext(Context androidContext) {
        this.androidContext = androidContext;
        this.prefs = androidContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public Context getAndroidContext() {
        return androidContext;
    }

    public SharedPreferences getPrefs() {
        return prefs;
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
