package org.androidsofdeath.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Startup extends Activity {

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
                Intent intent = new Intent(Startup.this, GameMap.class);
                intent.putExtra("name", nameField.getText().toString());
                startActivity(intent);
            }
        });
    }

}
