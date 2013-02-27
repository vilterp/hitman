package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import org.androidsofdeath.client.R;
import org.androidsofdeath.client.model.ApiException;
import org.androidsofdeath.client.model.Game;
import org.androidsofdeath.client.model.GameSession;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;

import java.io.IOException;
import java.util.*;

public class GameList extends Activity {

    public static final String TAG = "HITMAN_GameSession";
    private GameSession session;
    private ListView gameList;
    private ProgressBar spinner;
    private State state;

    enum State {
        LOADING,
        DISPLAYING
    }

    public void onCreate(Bundle savedInstanceState) {

        // TODO: show loading indicator

        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_list);
        session = (GameSession) getIntent().getSerializableExtra("session");
        gameList = (ListView) findViewById(R.id.game_list_list);
        // spinner = (ProgressBar) findViewById(R.id.game_list_progress);
        loadList();
    }

    private void loadList() {
        enterLoadingState();
        new AsyncTask<Void, Void, Set<Game>>() {
            @Override
            protected Set<Game> doInBackground(Void... params) {
                try {
                    return session.getGameList();
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    return null;
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                    return null;
                } catch (ApiException e) {
                    Log.e(TAG, e.toString());
                    return null;
                }
            }
            protected void onPostExecute(Set<Game> games) {
                updateList(games);
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.game_list_new_game:
                Intent launchNewGame = new Intent(this, NewGame.class);
                launchNewGame.putExtra("session", session);
                startActivity(launchNewGame);
                break;
            case R.id.game_list_refresh:
                loadList();
                break;
        }
        return true;
    }

    private void updateList(Set<Game> games) {
        String[] from = {"name", "location", "numPlayers", "startDate"};
        int[] to = {R.id.game_list_item_name, R.id.game_list_item_location,
                R.id.game_list_item_players, R.id.game_list_item_startdate};
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (Game game : games) {
            Map<String, String> row = new HashMap<String, String>();
            // TODO: nicer formatting
            row.put("name", game.getName());
            row.put("location", String.format("%f, %f",
                    game.getLocation().getLatitude(), game.getLocation().getLongitude()));
            row.put("numPlayers", String.format("%d players", game.getPlayers().size()));
            row.put("startDate", game.getStartDate().toString(DateTimeFormat.shortDateTime()));
            data.add(row);
        }
        gameList.setAdapter(new SimpleAdapter(this, data, R.layout.game_list_item, from, to));
        state = State.DISPLAYING;
//        spinner.setVisibility(View.INVISIBLE);
        gameList.setVisibility(View.VISIBLE);
    }

    private void enterLoadingState() {
        state = State.LOADING;
//        spinner.setVisibility(View.VISIBLE);
        gameList.setVisibility(View.INVISIBLE);
    }

}
