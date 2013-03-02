package com.hitman.client.model;

import java.io.Serializable;

public class Player implements Serializable {

    private String nickname;
    private int id;

    public Player(String nickname, int id) {
        this.nickname = nickname;
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public int getId() {
        return id;
    }

}
