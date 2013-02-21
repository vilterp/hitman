package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.os.Bundle;
import org.androidsofdeath.client.R;

public class SigningIn extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting);
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");

    }

}
