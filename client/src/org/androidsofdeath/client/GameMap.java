package org.androidsofdeath.client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;

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
