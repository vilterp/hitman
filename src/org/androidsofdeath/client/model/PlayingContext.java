package org.androidsofdeath.client.model;

import android.content.Context;
import android.location.Location;
import org.androidsofdeath.client.activity.ShowGame;
import org.apache.http.Header;

import java.io.Serializable;
import java.util.List;

public class PlayingContext extends LoggedInContext implements Serializable {

    private Game game;

    public PlayingContext(Game game, LoginCredentials credentials) {
        super(credentials);
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void updateLocation(Location loc) {
        // TODO ...
    }

    public void leaveGame(Context context) {
        getPrefs(context).edit().remove(PREF_CURRENT_GAME_ID).commit();
    }

}
