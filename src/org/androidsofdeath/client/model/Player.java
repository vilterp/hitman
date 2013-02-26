package org.androidsofdeath.client.model;

public class Player {

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
