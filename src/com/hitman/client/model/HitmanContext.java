package com.hitman.client.model;

import android.content.Context;
import com.hitman.client.http.AuthContext;

import java.io.Serializable;

public abstract class HitmanContext extends AuthContext {

    private Context androidContext;
    private SessionStorage storage;

    public HitmanContext(Context androidContext) {
        this.androidContext = androidContext;
        this.storage = new SessionStorage(androidContext);
    }

    public SessionStorage getSessionStorage() {
        return storage;
    }

    public Context getAndroidContext() {
        return androidContext;
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
