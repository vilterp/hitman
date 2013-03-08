package com.hitman.client.activity;

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
import com.hitman.client.R;
import com.hitman.client.Util;
import com.hitman.client.http.Either;
import com.hitman.client.http.Left;
import com.hitman.client.http.WrongSideException;
import com.hitman.client.model.*;
import com.hitman.client.service.LocationService;
import org.joda.time.DateTime;

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
        try {
            context = LoggedInContext.readFromStorage(new LoggedOutContext(this));
        } catch (SessionStorage.NoCredentialsException e) {
            throw new RuntimeException(e);
        }
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
                // http://stackoverflow.com/questions/4467816/datepicker-shows-wrong-value-of-month
                DateTime dateTime = new DateTime(startDate.getYear(), startDate.getMonth() + 1,
                        startDate.getDayOfMonth(), startTime.getCurrentHour(),
                        startTime.getCurrentMinute(), 0);
                Game game = new Game(-1, gameName.getText().toString(),
                                     location, dateTime, new HashSet<Player>(), null, false);
                submitButton.setEnabled(false);
                final Toast toast = Toast.makeText(NewGame.this, "Creating game....", Toast.LENGTH_LONG);
                toast.show();
                new AsyncTask<Game, Void, Either<Object,Either<LoggedInContext.AlreadyInGameException,PlayingContext>>>() {
                    @Override
                    protected Either<Object,Either<LoggedInContext.AlreadyInGameException,PlayingContext>>
                                doInBackground(Game... params) {
                        assert params.length == 0;
                        Game theGame = params[0];
                        return Util.collapse(context.createGame(theGame).bindRight(
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
                                PlayingContext ctx = joinRes.getRight();
                                // start location service
                                startService(new Intent(NewGame.this, LocationService.class));
                                // go back to game list, which will launch show game
                                Intent data = new Intent();
                                setResult(RESULT_OK, data);
                                finish();
                            }
                        } catch (WrongSideException e) {
                            Util.handleError(NewGame.this, e);
                            submitButton.setEnabled(true);
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
