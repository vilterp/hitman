package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.androidsofdeath.client.R;
import org.androidsofdeath.client.model.UnexpectedResponseStatusException;
import org.androidsofdeath.client.model.Game;
import org.androidsofdeath.client.model.GameSession;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;

import java.io.IOException;
import java.util.*;

public class GameList extends Activity implements JoinGameDialogFragment.JoinGameDialogListener {

    public static final String TAG = "HITMAN_GameSession";
    private static final int REQ_NEW_GAME = 1;
    private GameSession session;
    private ListView gameList;
    private ProgressBar spinner;
    private State state;
    private HashMap<Integer, Game> gameIndicies;

    enum State {
        LOADING,
        DISPLAYING
    }

    public void onCreate(Bundle savedInstanceState) {
        // TODO: show loading indicator
        super.onCreate(savedInstanceState);
        gameIndicies = new HashMap<Integer, Game>();
        setContentView(R.layout.game_list);
        session = (GameSession) getIntent().getSerializableExtra("session");
        gameList = (ListView) findViewById(R.id.game_list_list);
        // spinner = (ProgressBar) findViewById(R.id.game_list_progress);
        gameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Game game = gameIndicies.get(position);
                DialogFragment joinDialog = new JoinGameDialogFragment(game);
                joinDialog.show(getFragmentManager(), "JoinGameDialogFragment");
            }
        });
        loadList();
    }

    public void onPositiveClick(final Game game) {
        final Toast toast = Toast.makeText(this, "Joining...", Toast.LENGTH_LONG);
        toast.show();
        new AsyncTask<Game, Void, GameSession.JoinResult>() {
            @Override
            protected GameSession.JoinResult doInBackground(Game... params) {
                assert params.length == 1;
                Game gameToJoin = params[0];
                try {
                    return session.joinGame(gameToJoin);
                } catch (UnexpectedResponseStatusException e) {
                    Log.e(TAG, e.toString());
                    return null;
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    return null;
                }
            }
            @Override
            protected void onPostExecute(GameSession.JoinResult res) {
                toast.cancel();
                if(res == null) {
                    Toast.makeText(GameList.this, "Error while joining. Try again.", Toast.LENGTH_LONG);
                } else {
                    switch(res) {
                        case JOINED:
                            Intent showGame = new Intent(GameList.this, ShowGame.class);
                            showGame.putExtra("session", session);
                            showGame.putExtra("game", game);
                            startActivity(showGame);
                            break;
                        case ALREADY_IN_GAME:
                            // really, the UI shouldn't let it get to this point.
                            Toast.makeText(GameList.this, "Already in game.", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        }.execute(game);
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
                } catch (UnexpectedResponseStatusException e) {
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
                startActivityForResult(launchNewGame, REQ_NEW_GAME);
                break;
            case R.id.game_list_refresh:
                loadList();
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        assert reqCode == REQ_NEW_GAME;
        assert resCode == RESULT_OK;
        showJustJoinedGame((Game) data.getExtras().getSerializable("game"));
    }

    private void showJustJoinedGame(Game game) {
        // save game id
        SharedPreferences prefs = getSharedPreferences(Startup.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Startup.PREF_CURRENT_GAME_ID, game.getId());
        editor.commit();
        // launch show game screen
        Intent showGame = new Intent(this, ShowGame.class);
        showGame.putExtra("session", session);
        showGame.putExtra("game", game);
        startActivity(showGame);
    }

    private void updateList(Set<Game> games) {
        gameIndicies.clear();
        String[] from = {"name", "location", "numPlayers", "startDate"};
        int[] to = {R.id.game_list_item_name, R.id.game_list_item_location,
                R.id.game_list_item_players, R.id.game_list_item_startdate};
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        int ind = 0;
        for (Game game : games) {
            Map<String, String> row = new HashMap<String, String>();
            // TODO: nicer formatting
            row.put("name", game.getName());
            row.put("location", String.format("%f, %f",
                    game.getLocation().getLat(), game.getLocation().getLng()));
            row.put("numPlayers", String.format("%d players", game.getPlayers().size()));
            row.put("startDate", game.getStartDate().toString(DateTimeFormat.shortDateTime()));
            data.add(row);
            gameIndicies.put(ind, game);
            ind++;
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
