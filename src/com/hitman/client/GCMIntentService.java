package com.hitman.client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.common.base.Function;
import com.hitman.client.activity.ShowGame;
import com.hitman.client.activity.Startup;
import com.hitman.client.event.*;
import com.hitman.client.http.Either;
import com.hitman.client.http.Left;
import com.hitman.client.http.Right;
import com.hitman.client.http.WrongSideException;
import com.hitman.client.model.*;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

public class GCMIntentService extends GCMBaseIntentService {

    public static final String TAG = "HITMAN-GCMIntentService";

    public static final String REG_RECEIVED_ACTION = "com.hitman.client.action.REG_RECEIVED";
    public static final String GAME_EVENT_ACTION = "com.hitman.client.action.GAME_EVENT";
    private static final int LOCATION_STATIONARY_MSGID = 1;
    private static final int LOCATION_MOVING_MSGID = 2;
    private static final int TARGET_REASSIGNED_MSGID = 3;
    private static final int GAME_STARTED_MSGID = 4;
    private static final int JOIN_MSGID = 5;

    public GCMIntentService() {
        super(Startup.SENDER_ID);
    }

    private static Either<Exception,GameEvent> parseMessage(Intent intent) {
        String type = intent.getStringExtra("type");
        GameEvent evt = null;
        if (type.equals("location_stationary")) {
            evt = new StationaryLocationEvent(DateTime.now(), intent.getStringExtra("location"));
        } else if (type.equals("game_start")) {
            evt = new GameStartedEvent(DateTime.now(), intent.getStringExtra("target"));
        } else if(type.equals("location_moving")) {
            evt = new MovingLocationEvent(DateTime.now(), intent.getStringExtra("locationFrom"), intent.getStringExtra("locationTo"));
        } else if(type.equals("reassigned")) {
            evt = new TargetAssignedEvent(DateTime.now(), intent.getStringExtra("target"));
        } else if(type.equals("killed")) {
            evt = new KilledEvent(DateTime.now());
        } else {
            return new Left<Exception, GameEvent>(new Exception("unrecognized event type " + type));
        }
        return new Right<Exception, GameEvent>(evt);
    }
    
    @Override
    protected void onMessage(Context androidContext, Intent intent) {
        Log.i(TAG, "onMessage: " + intent.toString() + intent.getExtras().toString());

        // TODO: asyncify
        PlayingContext context = null;
        try {
            context = PlayingContext.readFromStorage(LoggedInContext.readFromStorage(new LoggedOutContext(this)));
        } catch (StorageException e) {
            e.printStackTrace();
        }

        // TODO: checkForNull or something
        Either<Exception, GameEvent> parseRes = parseMessage(intent);
        try {
            GameEvent evt = parseRes.getRight();
            if(evt instanceof StationaryLocationEvent) {
                showNotification("Target Update", evt.getHumanReadableDescr(), LOCATION_STATIONARY_MSGID);
            } else if(evt instanceof MovingLocationEvent) {
                showNotification("Target Update", evt.getHumanReadableDescr(), LOCATION_MOVING_MSGID);
            } else if(evt instanceof TargetAssignedEvent) {
                if(evt instanceof GameStartedEvent) {
                    showNotification("Game Started!", evt.getHumanReadableDescr(), GAME_STARTED_MSGID);
                } else {
                    showNotification("Target Assigned", evt.getHumanReadableDescr(), TARGET_REASSIGNED_MSGID);
                }
                context.getSessionStorage().setCurrentTarget(((TargetAssignedEvent)evt).getNewTarget());
            } else if(evt instanceof JoinEvent) {
                showNotification("New Player Joined", evt.getHumanReadableDescr(), JOIN_MSGID);
            } else if(evt instanceof KilledEvent) {
                Log.i(TAG, "you were killed");
            }
            // update model, send broadcast
            final PlayingContext finalContext = context;
            new AsyncTask<GameEvent, Void, Void>() {
                @Override
                protected Void doInBackground(GameEvent... params) {
                    assert params.length == 1;
                    finalContext.getGameStorage().addEvent(params[0]);
                    return null;
                }
                @Override
                protected void onPostExecute(Void bla) {
                    // send broadcast to update view
                    Intent evtBroadcast = new Intent();
                    evtBroadcast.setAction(GAME_EVENT_ACTION);
                    sendBroadcast(evtBroadcast);
                }
            }.execute(evt);
        } catch (WrongSideException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "An error occurred while receiving a GCM message.", Toast.LENGTH_LONG).show();
        }
    }

    private void showNotification(String title, String body, int id) {
        // TODO: check whether activity is running or not
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
