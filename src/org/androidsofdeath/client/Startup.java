package org.androidsofdeath.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Startup extends Activity {

    private static final int SIGNING_IN_REQ_CODE = 1;
    private Button submitButton;
    private EditText nameField;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);
        submitButton = (Button) findViewById(R.id.startupSubmitButton);
        nameField = (EditText) findViewById(R.id.nameField);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Startup.this, SigningIn.class);
                intent.putExtra("name", nameField.getText().toString());
                startActivityForResult(intent, SIGNING_IN_REQ_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if(reqCode != SIGNING_IN_REQ_CODE) {
            throw new RuntimeException("wrong req code");
        } else {
            GameSession session = (GameSession) data.getExtras().get("session");
            Log.d(SigningIn.TAG, session.toString());
            // launch GameMap with session...
            Intent intent = new Intent(this, GameMap.class);
            intent.putExtra("session", session);
            startActivity(intent);
        }
    }

}
