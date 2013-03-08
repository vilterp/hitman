package com.hitman.client.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.hitman.client.GCMIntentService;
import com.hitman.client.R;
import com.hitman.client.event.GameEvent;
import com.hitman.client.model.*;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowGame extends Activity {

    private static final String TAG = "HITMAN-ShowGame";
    private PlayingContext context;

    private State state;

    private LinearLayout countdownContainer;
    private TextView countdownTimer;

    private TextView waitingInd;

    private LinearLayout gameRunningInfoContainer;
    private TextView gameNameInd;
    private TextView playersLeftLabel;
    private TextView playersLeftInd;
    private TextView targetInd;

    private ListView gameEventsList;

    private BroadcastReceiver receiver;
    
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
        // VIEW REFS
        // countdown
        countdownContainer = (LinearLayout) findViewById(R.id.show_game_countdown_container);
        countdownTimer = (TextView) findViewById(R.id.show_game_countdown_timer);
        // waiting
        waitingInd = (TextView) findViewById(R.id.show_game_countdown_waiting_ind);
        // info
        gameNameInd = (TextView) findViewById(R.id.show_game_game_name);
        playersLeftLabel = (TextView) findViewById(R.id.show_game_num_players_label);
        playersLeftInd = (TextView) findViewById(R.id.show_game_num_players_ind);
        // running
        gameRunningInfoContainer = (LinearLayout) findViewById(R.id.show_game_running_info_container);
        targetInd = (TextView) findViewById(R.id.show_game_target_ind);
        // events list
        gameEventsList = (ListView) findViewById(R.id.show_game_events_list);
        // set up initial state
        if(DateTime.now().isBefore(context.getGameStorage().getGame().getStartDateTime())) {
            // countdown
            enterCountdownState();
        } else {
            enterRunningState();
        }
        // update list when new game events come
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reloadGameFromStorage();
            }
        };
        this.registerReceiver(receiver, new IntentFilter(GCMIntentService.GAME_EVENT_ACTION));
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadGameFromStorage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void reloadGameFromStorage() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                context.reloadGameStorage().getGame();
                return null;
            }
            @Override
            protected void onPostExecute(Void bla) {
                updateEventsList();
                setBasicInfo();
            }
        }.execute();
    }

    private void updateEventsList() {
        String[] from = {"description", "time"};
        int[] to = {R.id.game_events_list_item_description, R.id.game_events_list_item_datetime};
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (GameEvent event: context.getGameStorage().getGame().getEvents()) {
            Map<String, String> row = new HashMap<String, String>();
            row.put("description", event.getHumanReadableDescr());
            row.put("time", event.getDateTime().toString(DateTimeFormat.shortDateTime()));
        }
        gameEventsList.setAdapter(
                new SimpleAdapter(this, data, R.layout.game_events_list_item, from, to));
    }

    private void enterCountdownState() {
        enterState(State.COUNTDOWN);
        countdownContainer.setVisibility(View.VISIBLE);
        waitingInd.setVisibility(View.GONE);
        gameRunningInfoContainer.setVisibility(View.GONE);
        playersLeftInd.setText("Players joined:  ");
        startTicker();
    }

    private void enterRunningState() {
        enterState(State.RUNNING);
        countdownContainer.setVisibility(View.GONE);
        gameRunningInfoContainer.setVisibility(View.VISIBLE);
        waitingInd.setVisibility(View.GONE);
        playersLeftInd.setText("Players left:  ");
    }

    private void setBasicInfo() {
        Game game = context.getGameStorage().getGame();
        gameNameInd.setText(game.getName());
        playersLeftInd.setText(Integer.toString(game.getPlayers().size()));
        if(state == State.RUNNING) {
            targetInd.setText(context.getSessionStorage().getCurrentTarget());
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
