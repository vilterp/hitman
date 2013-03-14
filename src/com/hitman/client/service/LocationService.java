package com.hitman.client.service;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.hitman.client.http.Either;
import com.hitman.client.http.Left;
import com.hitman.client.model.*;
import org.apache.http.HttpResponse;

public class LocationService extends Service {

    private static final String TAG = "HITMAN-LocationService";
    private static final long MIN_INTERVAL = 1 * 60 * 1000; // 1 minute in ms
//    private static final long MIN_INTERVAL = 10 * 1000;

    private LocationManager manager;
    private LocationListener listener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        // set up location listener....
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {
            public void onLocationChanged(Location location) {
                new AsyncTask<Location, Void, Either<Object,HttpResponse>>() {
                    @Override
                    protected Either<Object,HttpResponse> doInBackground(Location... params) {
                        try {
                            PlayingContext context = PlayingContext.readFromStorage(
                                    LoggedInContext.readFromStorage(new LoggedOutContext(LocationService.this)));
                            return context.updateLocation(params[0]);
                        } catch (StorageException e) {
                            Log.i(TAG, "no game found; stopping");
                            stopSelf();
                            return new Left<Object, HttpResponse>("game stopped");
                        }
                    }
                    protected void onPostExecute(Either<Object,HttpResponse> res) {
                        if(res instanceof Left) {
                            Log.e(TAG, "location update failed: " + res.getValue().toString());
                        } else {
                            Log.i(TAG, "sent location");
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
        manager.requestLocationUpdates(MIN_INTERVAL, 0, new Criteria(), listener, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        manager.removeUpdates(listener);
    }

}
