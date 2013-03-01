package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.androidsofdeath.client.R;
import org.androidsofdeath.client.Util;
import org.androidsofdeath.client.http.Either;
import org.androidsofdeath.client.http.Left;
import org.androidsofdeath.client.http.WrongSideException;
import org.androidsofdeath.client.model.Game;
import org.androidsofdeath.client.model.LoggedInContext;
import org.androidsofdeath.client.model.LoginCredentials;
import org.androidsofdeath.client.model.PlayingContext;
import org.joda.time.format.DateTimeFormat;

import java.util.*;

public class GameList extends Activity implements JoinGameDialogFragment.JoinGameDialogListener {

    public static final String TAG = "HITMAN_LoggedInContext";
    private static final int REQ_NEW_GAME = 1;
    private LoggedInContext context;
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
        context = new LoggedInContext((LoginCredentials) getIntent().getSerializableExtra("credentials"));
        gameList = (ListView) findViewById(R.id.game_list_list);
        gameList.setEmptyView(findViewById(R.id.game_list_empty));
        spinner = (ProgressBar) findViewById(R.id.progress);
        gameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Game game = gameIndicies.get(position);
                DialogFragment joinDialog = new JoinGameDialogFragment(game);
                joinDialog.show(getFragmentManager(), "JoinGameDialogFragment");
            }
        });
        loadList();
    }

    public void onResume() {
        super.onResume();
        loadList();
    }

    public void onPositiveClick(final Game game) {
        final Toast toast = Toast.makeText(this, "Joining...", Toast.LENGTH_LONG);
        toast.show();
        new AsyncTask<Game, Void, Either<Object,Either<LoggedInContext.AlreadyInGameException,PlayingContext>>>() {
            @Override
            protected Either<Object,Either<LoggedInContext.AlreadyInGameException,PlayingContext>> doInBackground(Game... params) {
                assert params.length == 1;
                Game gameToJoin = params[0];
                return context.joinGame(GameList.this, gameToJoin);
            }
            @Override
            protected void onPostExecute(Either<Object,Either<LoggedInContext.AlreadyInGameException,PlayingContext>> res) {
                toast.cancel();
                try {
                Either<LoggedInContext.AlreadyInGameException,PlayingContext> joinRes = res.getRight();
                if(joinRes instanceof Left) {
                    throw new RuntimeException("already in game");
                } else {
                    Intent showGame = new Intent(GameList.this, ShowGame.class);
                    PlayingContext ctx = joinRes.getRight();
                    showGame.putExtra("credentials", ctx.getCredentials());
                    showGame.putExtra("game", ctx.getGame());
                    startActivity(showGame);
                }
                } catch (WrongSideException e) {
                    Toast.makeText(GameList.this, "Error while joining. Try again.", Toast.LENGTH_LONG).show();
                }
            }
        }.execute(game);
    }

    private void loadList() {
        enterLoadingState();
        new AsyncTask<Void, Void, Either<Object,Set<Game>>>() {
            @Override
            protected Either<Object,Set<Game>> doInBackground(Void... params) {
                return context.getGameList();
            }
            protected void onPostExecute(Either<Object,Set<Game>> games) {
                try {
                    updateList(games.getRight());
                } catch (WrongSideException e) {
                    Util.handleError(GameList.this, e);
                }
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
                launchNewGame.putExtra("credentials", context.getCredentials());
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
        Intent showGame = new Intent(this, ShowGame.class);
        showGame.putExtras(data.getExtras());
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
        spinner.setVisibility(View.GONE);
        gameList.setVisibility(View.VISIBLE);
    }

    private void enterLoadingState() {
        state = State.LOADING;
        spinner.setVisibility(View.VISIBLE);
        gameList.setVisibility(View.GONE);
    }

}
