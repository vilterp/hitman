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
import com.google.common.base.Function;
import org.androidsofdeath.client.R;
import org.androidsofdeath.client.http.Either;
import org.androidsofdeath.client.http.Left;
import org.androidsofdeath.client.http.UnexpectedResponseStatusException;
import org.androidsofdeath.client.http.WrongSideException;
import org.androidsofdeath.client.model.*;
import org.joda.time.DateTime;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashSet;

public class NewGame extends Activity {

    private static final String TAG = "HITMAN-NewGame";
    private EditText gameName;
    private DatePicker startDate;
    private TimePicker startTime;
    private Button submitButton;
    private LatLng location;
    private TextView waitingMsg;
    private LoggedInContext context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);
        location = null;
        context = (LoggedInContext) getIntent().getSerializableExtra("context");
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
                new AsyncTask<Game, Void, Either<Object,Either<LoggedInContext.AlreadyInGameException,PlayingContext>>>() {
                    @Override
                    protected Either<Object,Either<LoggedInContext.AlreadyInGameException,PlayingContext>>
                                doInBackground(Game... params) {
                        assert params.length == 0;
                        Game theGame = params[0];
                        return LoggedInContext.collapse(context.createGame(theGame).bindRight(
                                new Function<Game, Either<Object, Either<LoggedInContext.AlreadyInGameException, PlayingContext>>>() {
                                    public Either<Object, Either<LoggedInContext.AlreadyInGameException, PlayingContext>> apply(Game game) {
                                        return context.joinGame(game);
                                    }
                                }));
                    }
                    @Override
                    protected void onPostExecute(Either<Object,Either<LoggedInContext.AlreadyInGameException,PlayingContext>> res) {
                        toast.cancel();
                        try {
                            Either<LoggedInContext.AlreadyInGameException,PlayingContext> joinRes = res.getRight();
                            if(joinRes instanceof Left) {
                                throw new RuntimeException("user already in game");
                            } else {
                                Intent data = new Intent();
                                data.putExtra("context", joinRes.getRight());
                                setResult(RESULT_OK, data);
                                finish();
                            }
                        } catch (WrongSideException e) {
                            Toast.makeText(NewGame.this, "An error occured. Try again?", Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute(game);
            }
        });
        // get location
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                NewGame.this.location = new LatLng(location);
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
