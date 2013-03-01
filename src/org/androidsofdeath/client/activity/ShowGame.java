package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.androidsofdeath.client.R;
import org.androidsofdeath.client.model.Game;
import org.androidsofdeath.client.model.LoginCredentials;
import org.androidsofdeath.client.model.PlayingContext;

public class ShowGame extends Activity {

    private static final String TAG = "HITMAN-ShowGame";
    private PlayingContext context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_game);
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
                context.leaveGame(this);
                Log.i(TAG, "leave game");
                finish();
                break;
        }
        return true;
    }

}
