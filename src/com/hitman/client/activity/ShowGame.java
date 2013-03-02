package com.hitman.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.hitman.client.R;
import com.hitman.client.model.Game;
import com.hitman.client.model.LoginCredentials;
import com.hitman.client.model.PlayingContext;
import com.hitman.client.model.SessionStorage;

public class ShowGame extends Activity {

    private static final String TAG = "HITMAN-ShowGame";
    private PlayingContext context;
    private SessionStorage storage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_game);
        storage = new SessionStorage(this);
        context = new PlayingContext((Game) getIntent().getSerializableExtra("game"),
                        (LoginCredentials) getIntent().getSerializableExtra("credentials"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_game_leave:
                storage.clearGameId();
                Log.i(TAG, "leave game");
                finish();
                break;
        }
        return true;
    }

}