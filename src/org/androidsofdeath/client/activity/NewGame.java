package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.androidsofdeath.client.R;
import org.androidsofdeath.client.model.Game;
import org.androidsofdeath.client.model.GameSession;
import org.androidsofdeath.client.model.Player;
import org.joda.time.DateTime;
import org.json.JSONException;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

public class NewGame extends Activity {

    private static final String TAG = "HITMAN-NewGame";
    private EditText gameName;
    private DatePicker startDate;
    private TimePicker startTime;
    private Button submitButton;
    private Location location;
    private TextView waitingMsg;
    private GameSession session;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);
        location = null;
        session = (GameSession) getIntent().getSerializableExtra("session");
        // get refs to shit
        gameName = (EditText) findViewById(R.id.new_game_game_name);
        startDate = (DatePicker) findViewById(R.id.new_game_date);
//        startDate.setMinDate(new Date().getTime());
        startTime = (TimePicker) findViewById(R.id.new_game_time);
        submitButton = (Button) findViewById(R.id.new_game_submit_button);
        waitingMsg = (TextView) findViewById(R.id.new_game_waiting_msg);
        submitButton.setEnabled(false);
        // register submit callback
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DateTime dateTime = new DateTime(startDate.getYear(), startDate.getMonth(),
                        startDate.getDayOfMonth(), startTime.getCurrentHour(),
                        startTime.getCurrentMinute(), 0);
                Game game = new Game(-1, gameName.getText().toString(),
                                     location, new HashSet<Player>(), dateTime, false);
                submitButton.setEnabled(false);
                final Toast toast = Toast.makeText(NewGame.this, "Creating game....", Toast.LENGTH_LONG);
                toast.show();
                new AsyncTask<Game, Void, Game>() {
                    @Override
                    protected Game doInBackground(Game... params) {
                        assert params.length == 0;
                        Game theGame = params[0];
                        try {
                            Game createdGame = null;
                            try {
                                createdGame = session.createGame(theGame);
                            } catch (GameSession.ApiException e) {
                                Log.e(TAG, String.format("create game failed: %s", e.toString()));
                            }
                            try {
                                session.joinGame(createdGame);
                                return createdGame;
                            } catch (GameSession.ApiException e) {
                                Log.e(TAG, String.format("join game failed: %s", e.toString()));
                            }
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Game res) {
                        toast.cancel();
                        Intent launchGameScreen = new Intent(NewGame.this, ShowGame.class);
                        launchGameScreen.putExtra("session", session.joinedGame(res));
                        startActivity(launchGameScreen);
                    }
                }.execute(game);
            }
        });
        // get location
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                NewGame.this.location = location;
                submitButton.setEnabled(true);
                waitingMsg.setText(String.format("Location: %f, %f", location.getLatitude(), location.getLongitude()));
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i(TAG, "onStatusChanged");
            }

            public void onProviderEnabled(String provider) {
                Log.i(TAG, "onProviderEnabled");
            }

            public void onProviderDisabled(String provider) {
                Log.i(TAG, "onProviderDisabled");
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        // TODO: dispose this?
    }

}
