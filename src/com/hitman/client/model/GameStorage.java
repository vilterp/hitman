package com.hitman.client.model;

import android.content.Context;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.hitman.client.event.*;
import org.joda.time.DateTime;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class GameStorage {

    static class DateTimeTypeConverter implements JsonSerializer<DateTime>,JsonDeserializer<DateTime> {
        public JsonElement serialize(DateTime src, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
        public DateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context)
          throws JsonParseException {
            return new DateTime(json.getAsString());
        }
    }

    private static Gson gson;
    static {
        TypeAdapterFactory factory =
            RuntimeTypeAdapterFactory.of(GameEvent.class)
                .registerSubtype(JoinEvent.class)
                .registerSubtype(YouJoinedEvent.class)
                .registerSubtype(KilledEvent.class)
                .registerSubtype(TargetAssignedEvent.class)
                .registerSubtype(GameStartedEvent.class)
                .registerSubtype(StationaryLocationEvent.class)
                .registerSubtype(MovingLocationEvent.class)
                .registerSubtype(TakePhotoEvent.class)
                .registerSubtype(PhotoReceivedEvent.class)
                .registerSubtype(KillEvent.class)
                .registerSubtype(GameWonEvent.class);
        gson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
                .registerTypeAdapterFactory(factory)
                .create();
    }

    private static final String GAME_FILE_NAME = "game.json";
    private Context context;
    private Game game;
    private boolean cleared;

    private GameStorage(Context context, Game game) {
        this.context = context;
        this.game = game;
    }

    public static GameStorage read(Context context) throws NoGameException {
        if(hasGame(context)) {
            try {
                FileInputStream input = context.openFileInput(GAME_FILE_NAME);
                Game game = gson.fromJson(new InputStreamReader(input), Game.class);
                assert game != null;
                input.close();
                return new GameStorage(context, game);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new NoGameException();
        }
    }

    public static class NoGameException extends StorageException {}

    public static GameStorage create(Context context, Game game) {
        if(hasGame(context)) {
            throw new IllegalStateException("game already present. use clear?");
        } else {
            GameStorage gameStorage = new GameStorage(context, game);
            gameStorage.save();
            return gameStorage;
        }
    }

    public void clear() {
        checkCleared();
        if(hasGame(context)) {
            context.deleteFile(GAME_FILE_NAME);
            cleared = true;
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
        if(evt instanceof JoinEvent) {
            game.getPlayers().add(new Player(((JoinEvent)evt).getUser(), -1));
        }
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

    public void setKillCode(String killCode) {
        getGame().setKillCode(killCode);
        save();
    }

    public Game getGame() {
        checkCleared();
        return game;
    }

    public boolean isActive() {
        return !cleared;
    }

    private void save() {
        checkCleared();
        assert game.getStartDateTime() != null;
        assert game.getEvents() != null;
        assert game.getLocation() != null;
        assert game.getId() >= 0;
        assert game.getName() != null;
        assert game.getPlayers() != null;
        assert game.getEvents() != null;
        try {
            FileOutputStream out = context.openFileOutput(GAME_FILE_NAME, Context.MODE_PRIVATE);
//            gson.toJson(game, new OutputStreamWriter(out));
            String json = gson.toJson(game);
            out.write(json.getBytes("UTF-8"));
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
