package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.androidsofdeath.client.R;

public class SignUp extends Activity {

    private EditText username;
    private EditText password;
    private Button submitButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        username = (EditText) findViewById(R.id.signup_password);
        password = (EditText) findViewById(R.id.signup_password);
        submitButton = (Button) findViewById(R.id.signup_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

}
