package org.androidsofdeath.client.model;

import android.location.Location;

import java.io.Serializable;

public class PlayingContext implements Serializable {

    private Game game;
    private LoginCredentials credentials;

    public PlayingContext(Game game, LoginCredentials credentials) {
        this.game = game;
        this.credentials = credentials;
    }

    public Game getGame() {
        return game;
    }

    public LoginCredentials getCredentials() {
        return credentials;
    }

    public void updateLocation(Location loc) {
        // TODO ...
    }

}
