package com.hitman.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.hitman.client.R;
import com.hitman.client.model.LoginCredentials;

public class Login extends Activity {

    private static final int SIGNING_IN_REQ_CODE = 1;

    private EditText nameField;
    private EditText passwordField;
    private Button submitButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        final String gcmId = getIntent().getStringExtra("gcmId");
        submitButton = (Button) findViewById(R.id.login_submitButton);
        nameField = (EditText) findViewById(R.id.login_nameField);
        passwordField = (EditText) findViewById(R.id.login_passwordField);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent data = new Intent();
                LoginCredentials credentials = new LoginCredentials(
                        gcmId,
                        nameField.getText().toString(),
                        passwordField.getText().toString());
                data.putExtra("credentials", credentials);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

}
