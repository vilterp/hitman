package com.hitman.client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.common.base.Function;
import com.hitman.client.activity.ShowGame;
import com.hitman.client.activity.Startup;
import com.hitman.client.gcm.GCMMessage;
import com.hitman.client.gcm.LocationStationaryMessage;
import com.hitman.client.gcm.StartGameMessage;
import com.hitman.client.http.Either;
import com.hitman.client.http.Left;
import com.hitman.client.http.Right;
import com.hitman.client.http.WrongSideException;
import org.json.JSONException;
import org.json.JSONObject;

public class GCMIntentService extends GCMBaseIntentService {

    public static final String TAG = "HITMAN-GCMIntentService";

    public static final String REG_RECEIVED_ACTION = "com.hitman.client.action.REG_RECEIVED";
    public static final int STARTGAME_MSGID = 0;
    private static final int LOCATION_STATIONARY_MSGID = 1;

    public GCMIntentService() {
        super(Startup.SENDER_ID);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "onMessage: " + intent.toString());

        String json = "";//...
        Either<Object, GCMMessage> message =
            Util.collapse(
                Util.parseJsonObject.apply(json)
            .bindRight(new Function<JSONObject, Either<JSONException, GCMMessage>>() {
                public Either<JSONException, GCMMessage> apply(JSONObject jsonObject) {
                    try {
                        String type = jsonObject.getString("type");
                        GCMMessage msg = null;
                        if (type.equals("location_stationary")) {
                            msg = new LocationStationaryMessage(jsonObject.getString("target"),
                                        jsonObject.getString("location"));
                        } else if (type.equals("start_game")) {
                            msg = new StartGameMessage(jsonObject.getString("target"));
                        } // locationmoving, takepictures, initiatekill, etc
                        return new Right<JSONException, GCMMessage>(msg);
                    } catch (JSONException e) {
                        return new Left<JSONException, GCMMessage>(e);
                    }
                }
            }));

        try {
            GCMMessage msg = message.getRight();
            // apparently I have to do this....
            if(msg instanceof StartGameMessage) {
                handleMessage((StartGameMessage) msg);
            } else if(msg instanceof LocationStationaryMessage) {
                handleMessage((LocationStationaryMessage) msg);
            }
        } catch (WrongSideException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "An error occurred while receiving a GCM message.", Toast.LENGTH_LONG).show();
        }
    }

    private void handleMessage(StartGameMessage message) {
        // show notification
        String userMessage = String.format("You have been assigned a target, nickname \"%s\"." +
                " You will receive periodic updates about their whereabouts," +
                " which you must use to track them down. Good luck!",
                message.getTarget());
        showNotification("Game Started", userMessage, STARTGAME_MSGID);
        // TODO: update model...
    }

    private void handleMessage(LocationStationaryMessage message) {
        String userMessage = String.format("Your target is currently at %s", message.getPlaceName());
        showNotification("Target Update", userMessage, LOCATION_STATIONARY_MSGID);
    }

    private void showNotification(String title, String body, int id) {
        Notification.Builder mBuilder = new Notification.Builder(this)
                                                    .setContentTitle(title)
                                                    .setContentText(body);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, ShowGame.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ShowGame.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(id, mBuilder.build());
    }

    @Override
    protected void onError(Context context, String errorId) {
        Log.i(TAG, "onError");
    }

    @Override
    protected void onRegistered(Context context, final String registrationId) {
        Log.i(TAG, "onRegistered");
        Intent broadcast = new Intent();
        broadcast.putExtra("registration", registrationId);
        broadcast.setAction(REG_RECEIVED_ACTION);
        sendBroadcast(broadcast);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "onUnregistered");
    }

}
