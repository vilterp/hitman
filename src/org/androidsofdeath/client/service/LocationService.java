package org.androidsofdeath.client.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import org.androidsofdeath.client.http.Either;
import org.androidsofdeath.client.http.Left;
import org.androidsofdeath.client.model.*;
import org.apache.http.HttpResponse;

public class LocationService extends Service {

    private static final String TAG = "HITMAN-LocationService";
//    private static final long MIN_INTERVAL = 1 * 60 * 1000; // 1 minute in ms
    private static final long MIN_INTERVAL = 10 * 1000;

    private SessionStorage storage;
    private PlayingContext context;

    @Override
    public void onCreate() {
        super.onCreate();
        storage = new SessionStorage(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        Game game = new Game(storage.readGameId(), null, null, null, null, true); // TODO: janky...
        LoginCredentials credentials = storage.readLoginCredentials();
        assert credentials != null;
        context = new PlayingContext(game, credentials);
        // set up location listener....
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                new AsyncTask<Location, Void, Either<Object,HttpResponse>>() {
                    @Override
                    protected Either<Object,HttpResponse> doInBackground(Location... params) {
                        return context.updateLocation(params[0]);
                    }
                    protected void onPostExecute(Either<Object,HttpResponse> res) {
                        Log.i(TAG, "sent location");
                        if(res instanceof Left) {
                            Log.e(TAG, "location update failed: " + res.getValue().toString());
                        }
                    }
                }.execute(location);
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
        locationManager.requestLocationUpdates(MIN_INTERVAL, 0, new Criteria(), locationListener, null);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
    }

}
