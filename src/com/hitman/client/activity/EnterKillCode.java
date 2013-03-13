package com.hitman.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.hitman.client.R;
import com.hitman.client.Util;
import com.hitman.client.http.Either;
import com.hitman.client.http.WrongSideException;
import com.hitman.client.model.*;

public class EnterKillCode extends Activity {

    private PlayingContext context;

    private EditText killCodeField;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_kill_code);
        // get context
        try {
            context = PlayingContext.readFromStorage(LoggedInContext.readFromStorage(new LoggedOutContext(this)));
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
        // view refs
        killCodeField = (EditText) findViewById(R.id.enter_kill_code_kill_code_field);
        submitButton = (Button) findViewById(R.id.enter_kill_code_register_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AsyncTask<String, Void, Either<Object, Boolean>>() {
                    @Override
                    protected void onPreExecute() {
                        submitButton.setEnabled(false);
                    }
                    @Override
                    protected Either<Object, Boolean> doInBackground(String... params) {
                        assert params.length == 1;
                        return context.sendKillCode(params[0]);
                    }
                    @Override
                    protected void onPostExecute(Either<Object, Boolean> res) {
                        try {
                            boolean rightCode = res.getRight();
                            if(rightCode) {
                                Toast.makeText(EnterKillCode.this,
                                    "Kill confirmed. You will be assigned a new target shortly.",
                                    Toast.LENGTH_LONG).show();
                                setResult(RESULT_OK, new Intent());
                                finish();
                            } else {
                                Toast.makeText(EnterKillCode.this,
                                        "Wrong code. Either you mistyped it or you're lying! Try again.",
                                        Toast.LENGTH_LONG).show();
                                submitButton.setEnabled(true);
                            }
                        } catch (WrongSideException e) {
                            Util.handleError(EnterKillCode.this, e);
                            submitButton.setEnabled(true);
                        }
                    }
                }.execute(killCodeField.getText().toString());
            }
        });
    }

}
