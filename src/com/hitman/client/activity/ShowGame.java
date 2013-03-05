package com.hitman.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hitman.client.R;
import com.hitman.client.model.*;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

public class ShowGame extends Activity {

    private static final String TAG = "HITMAN-ShowGame";
    private PlayingContext context;

    private State state;

    private TextView runningInd;
    private LinearLayout countdownContainer;
    private TextView countdownTimer;
    private TextView waitingInd;
    
    public enum State {
        COUNTDOWN,
        WAITING_FOR_TARGET,
        RUNNING
        // OVER?
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_game);
        try {
            context = PlayingContext.readFromStorage(LoggedInContext.readFromStorage(new LoggedOutContext(this)));
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
        // view refs
        countdownContainer = (LinearLayout) findViewById(R.id.show_game_countdown_container);
        countdownTimer = (TextView) findViewById(R.id.show_game_countdown_timer);
        waitingInd = (TextView) findViewById(R.id.show_game_countdown_waiting_ind);
        runningInd = (TextView) findViewById(R.id.show_game_running_ind);
        // set up initial state
        if(DateTime.now().isBefore(context.getGameStorage().getGame().getStartDateTime())) {
            // countdown
            enterState(State.COUNTDOWN);
            countdownContainer.setVisibility(View.VISIBLE);
            startTicker();
        } else {
            enterState(State.RUNNING);
            countdownContainer.setVisibility(View.GONE);
        }
    }

    private void startTicker() {
        Thread ticker = new Thread(new Runnable() {
                    public void run() {
            DateTime countdownTo = context.getGameStorage().getGame().getStartDateTime();
            while(true) {
                DateTime now = DateTime.now();
                if(now.isBefore(countdownTo)) {
                    final Duration diff = new Interval(now.toInstant(), countdownTo.toInstant()).toDuration();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            countdownTimer.setText(String.format("%dd, %dh, %dm, %ds",
                                    diff.getStandardDays(), diff.getStandardHours(),
                                    diff.getStandardMinutes(), diff.getStandardSeconds()));
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            countdownTimer.setVisibility(View.GONE);
                            waitingInd.setVisibility(View.VISIBLE);
                            state = State.WAITING_FOR_TARGET;
                            enterState(State.WAITING_FOR_TARGET);
                        }
                    });
                    return;
                }
            }
            }
        });
        ticker.start();
    }

    private void enterState(State newState) {
        Log.i(TAG, "entered state " + newState.toString());
        state = newState;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_game_leave:
                context.leaveGame();
                Log.i(TAG, "leave game");
                finish();
                break;
        }
        return true;
    }

}
