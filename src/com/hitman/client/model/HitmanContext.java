package com.hitman.client.model;

import com.hitman.client.http.AuthContext;

import java.io.Serializable;

public abstract class HitmanContext extends AuthContext implements Serializable {

    @Override
    public String getDomain() {
        return "hitman.kevinzhang.org";
    }

    @Override
    public int getPort() {
        return 80;
    }

}
