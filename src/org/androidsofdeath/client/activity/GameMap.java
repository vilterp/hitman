package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.os.Bundle;
import org.androidsofdeath.client.R;

public class GameMap extends Activity {

    private static final String TAG = "GameMap";


    private GameSession session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        session = (GameSession) getIntent().getSerializableExtra("session");
    }

}
