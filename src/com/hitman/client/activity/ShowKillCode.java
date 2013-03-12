package com.hitman.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.hitman.client.R;

public class ShowKillCode extends Activity {

    private String killCode;
    private TextView killCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_kill_code);
        killCode = getIntent().getStringExtra("kill_code");
        // view ref
        killCodeView = (TextView) findViewById(R.id.kill_code_ind);
        killCodeView.setText(killCode);
    }

}
