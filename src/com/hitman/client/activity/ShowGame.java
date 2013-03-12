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
import android.widget.*;
import com.google.common.collect.Ordering;
import com.hitman.client.GCMIntentService;
import com.hitman.client.R;
import com.hitman.client.event.GameEvent;
import com.hitman.client.event.GameStartedEvent;
import com.hitman.client.event.GameWonEvent;
import com.hitman.client.model.*;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.util.*;

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

    private Button killedTargetButton;
    private Button beenKilledButton;

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
        // buttons
        killedTargetButton = (Button) findViewById(R.id.show_game_killed_target_button);
        beenKilledButton = (Button) findViewById(R.id.show_game_been_killed_button);
        // events list
        gameEventsList = (ListView) findViewById(R.id.show_game_events_list);
        // set up initial state
        if(context.getGameStorage().getGame().getStartDateTime().isAfterNow()) {
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
                GameEvent evt = (GameEvent) intent.getSerializableExtra("event");
                if(evt instanceof GameStartedEvent) {
                    enterRunningState();
                } else if(evt instanceof GameWonEvent) {
                    finish();
                }
            }
        };
        this.registerReceiver(receiver, new IntentFilter(GCMIntentService.GAME_EVENT_ACTION));
        // button listeners
        killedTargetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent startEnterKillCode = new Intent(ShowGame.this, EnterKillCode.class);
                startActivity(startEnterKillCode);
            }
        });
        beenKilledButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent startShowKillCode = new Intent(ShowGame.this, ShowKillCode.class);
                startShowKillCode.putExtra("kill_code", context.getGameStorage().getGame().getKillCode());
                startActivity(startShowKillCode);
            }
        });
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
        // sort reverse-chron
        List<GameEvent> games = Ordering.from(new Comparator<GameEvent>() {
            public int compare(GameEvent lhs, GameEvent rhs) {
                return lhs.getDateTime().compareTo(rhs.getDateTime());
            }
        }).reverse().sortedCopy(context.getGameStorage().getGame().getEvents());
        for (GameEvent event: games) {
            Map<String, String> row = new HashMap<String, String>();
            row.put("description", event.getHumanReadableDescr());
            row.put("time", event.getDateTime().toString(DateTimeFormat.shortDateTime()));
            data.add(row);
        }
        gameEventsList.setAdapter(
                new SimpleAdapter(this, data, R.layout.game_events_list_item, from, to));
    }

    private void enterCountdownState() {
        enterState(State.COUNTDOWN);
        countdownContainer.setVisibility(View.VISIBLE);
        waitingInd.setVisibility(View.GONE);
        gameRunningInfoContainer.setVisibility(View.GONE);
        playersLeftLabel.setText("Players joined:  ");
        startTicker();
    }

    private void enterRunningState() {
        enterState(State.RUNNING);
        countdownContainer.setVisibility(View.GONE);
        gameRunningInfoContainer.setVisibility(View.VISIBLE);
        waitingInd.setVisibility(View.GONE);
        playersLeftLabel.setText("Players left:  ");
    }

    private void enterWaitingState() {
        enterState(State.WAITING_FOR_TARGET);
        countdownContainer.setVisibility(View.GONE);
        gameRunningInfoContainer.setVisibility(View.GONE);
        waitingInd.setVisibility(View.VISIBLE);
    }

    private void setBasicInfo() {
        Game game = context.getGameStorage().getGame();
        gameNameInd.setText(game.getName());
        // +1 to account for current player
        playersLeftInd.setText(Integer.toString(game.getPlayers().size() + 1));
        String target = context.getSessionStorage().getCurrentTarget();
        if(target != null) {
            enterRunningState();
            targetInd.setText(target);
        }
    }

    private void startTicker() {
        Thread ticker = new Thread(new Runnable() {
                    public void run() {
            DateTime countdownTo = context.getGameStorage().getGame().getStartDateTime();
            while(true) {
                DateTime now = DateTime.now();
                if(now.isBefore(countdownTo)) {
                    final Period diff = new Interval(now, countdownTo).toPeriod();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            countdownTimer.setText(String.format("%dd, %dh, %dm, %ds",
                                    diff.getDays(), diff.getHours(),
                                    diff.getMinutes(), diff.getSeconds()));
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
                            enterWaitingState();
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
