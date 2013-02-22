package org.androidsofdeath.client.model;

import java.io.Serializable;

public class LoginCredentials implements Serializable {

    private String gcmId;
    private String username;
    private String password;

    public LoginCredentials(String regId, String username, String password) {
        this.gcmId = regId;
        this.username = username;
        this.password = password;
    }

    public String getGcmId() {
        return gcmId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}