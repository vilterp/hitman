package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.androidsofdeath.client.R;

public class Login extends Activity {

    private static final int SIGNING_IN_REQ_CODE = 1;
    private Button submitButton;
    private EditText nameField;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        submitButton = (Button) findViewById(R.id.startupSubmitButton);
        nameField = (EditText) findViewById(R.id.nameField);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, LoggingIn.class);
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
            Log.d(LoggingIn.TAG, session.toString());
            // launch GameMap with session...
            Intent intent = new Intent(this, GameMap.class);
            intent.putExtra("session", session);
            startActivity(intent);
        }
    }

}
