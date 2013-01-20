package org.androidsofdeath.client;

import java.io.Serializable;

public class GameSession implements Serializable {

    private String regId;
    private String userName;
    private String sessionId;

    public GameSession(String regId, String userName, String sessionId) {
        this.regId = regId;
        this.userName = userName;
        this.sessionId = sessionId;
    }

    public String getRegId() {
        return regId;
    }

    public String getUserName() {
        return userName;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return "GameSession{" +
                "regId='" + regId + '\'' +
                ", userName='" + userName + '\'' +
                ", sessionId=" + sessionId +
                '}';
    }

}
