package com.hitman.client.model;

import android.content.Context;
import com.google.gson.Gson;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class GameStorage {

    private static Gson gson = new Gson();

    private static final String GAME_FILE_NAME = "game.json";
    private Context context;
    private Game game;
    private boolean cleared;

    private GameStorage(Context context, Game game) {
        this.context = context;
        this.game = game;
    }

    public static GameStorage read(Context context) {
        if(hasGame(context)) {
            try {
                FileInputStream input = context.openFileInput(GAME_FILE_NAME);
                Game game = gson.fromJson(new InputStreamReader(input), Game.class);
                input.close();
                return new GameStorage(context, game);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException("no game. use create");
        }
    }

    public static GameStorage create(Context context, Game game) {
        if(hasGame(context)) {
            throw new IllegalStateException("game already present. use clear?");
        } else {
            GameStorage gameStorage = new GameStorage(context, game);
            gameStorage.save();
            return gameStorage;
        }
    }

    public void clear(Context context) {
        checkCleared();
        if(hasGame(context)) {
            context.deleteFile(GAME_FILE_NAME);
        } else {
            throw new IllegalStateException("no game to clear");
        }
    }

    private static boolean hasGame(Context context) {
        String[] files = context.fileList();
        return Arrays.asList(files).contains(GAME_FILE_NAME);
    }

    public void addEvent(GameEvent evt) {
        game.getEvents().add(evt);
        save();
    }

    public void addPlayers(List<Player> players) {
        game.getPlayers().addAll(players);
        save();
    }

    public void updateBasicInfo(Game updated) {
        game.setName(updated.getName());
        game.setLocation(updated.getLocation());
        game.setStartDate(updated.getStartDateTime());
        save();
    }

    public Game getGame() {
        return game;
    }

    private void save() {
        checkCleared();
        assert game.getStartDateTime() != null;
        assert game.getEvents() != null;
        assert game.getLocation() != null;
        assert game.getId() >= 0;
        assert game.getName() != null;
        assert game.getPlayers() != null;
        try {
            FileOutputStream out = context.openFileOutput(GAME_FILE_NAME, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            gson.toJson(game, new OutputStreamWriter(out));
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkCleared() {
        if(cleared) {
            throw new IllegalStateException("GameStorage cleared");
        }
    }

}
