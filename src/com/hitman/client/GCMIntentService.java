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
import com.hitman.client.activity.ShowGame;
import com.hitman.client.activity.Startup;
import com.hitman.client.activity.TakePictures;
import com.hitman.client.event.*;
import com.hitman.client.http.Either;
import com.hitman.client.http.Left;
import com.hitman.client.http.Right;
import com.hitman.client.http.WrongSideException;
import com.hitman.client.model.*;
import org.joda.time.DateTime;

public class GCMIntentService extends GCMBaseIntentService {

    public static final String TAG = "HITMAN-GCMIntentService";

    public static final String REG_RECEIVED_ACTION = "com.hitman.client.action.REG_RECEIVED";
    public static final String GAME_EVENT_ACTION = "com.hitman.client.action.GAME_EVENT";
    private static final int LOCATION_STATIONARY_MSGID = 1;
    private static final int LOCATION_MOVING_MSGID = 2;
    private static final int TARGET_REASSIGNED_MSGID = 3;
    private static final int GAME_STARTED_MSGID = 4;
    private static final int JOIN_MSGID = 5;
    private static final int TAKE_PHTOS_MSGID = 6;

    public GCMIntentService() {
        super(Startup.SENDER_ID);
    }

    private static Either<Exception,GameEvent> parseMessage(Intent intent) {
        String type = intent.getStringExtra("type");
        GameEvent evt = null;
        if (type.equals("location_stationary")) {
            evt = new StationaryLocationEvent(DateTime.now(), intent.getStringExtra("location"));
        } else if(type.equals("player_join")) {
            evt = new JoinEvent(DateTime.now(), intent.getStringExtra("name"));
        } else if (type.equals("game_start")) {
            evt = new GameStartedEvent(DateTime.now(), intent.getStringExtra("target"));
        } else if(type.equals("location_moving")) {
            evt = new MovingLocationEvent(DateTime.now(), intent.getStringExtra("locationFrom"), intent.getStringExtra("locationTo"));
        } else if(type.equals("reassigned")) {
            evt = new TargetAssignedEvent(DateTime.now(), intent.getStringExtra("target"));
        } else if(type.equals("killed")) {
            evt = new KilledEvent(DateTime.now());
        } else if(type.equals("take_photo")) {
            evt = new TakePhotoEvent(DateTime.now(), Integer.parseInt(intent.getStringExtra("photoset")));
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
            Intent showGameIntent = new Intent(this, ShowGame.class);
            if(evt instanceof StationaryLocationEvent) {
                showNotification("Target Update", evt.getHumanReadableDescr(), LOCATION_STATIONARY_MSGID, showGameIntent);
            } else if(evt instanceof MovingLocationEvent) {
                showNotification("Target Update", evt.getHumanReadableDescr(), LOCATION_MOVING_MSGID, showGameIntent);
            } else if(evt instanceof TargetAssignedEvent) {
                if(evt instanceof GameStartedEvent) {
                    showNotification("Game Started!", evt.getHumanReadableDescr(), GAME_STARTED_MSGID, showGameIntent);
                } else {
                    showNotification("Target Assigned", evt.getHumanReadableDescr(), TARGET_REASSIGNED_MSGID, showGameIntent);
                }
                context.getSessionStorage().setCurrentTarget(((TargetAssignedEvent)evt).getNewTarget());
            } else if(evt instanceof JoinEvent) {
                showNotification("New Player Joined", evt.getHumanReadableDescr(), JOIN_MSGID, showGameIntent);
            } else if(evt instanceof KilledEvent) {
                Log.i(TAG, "you were killed");
            } else if(evt instanceof TakePhotoEvent) {
                Intent takePhotos = new Intent(this, TakePictures.class);
                takePhotos.putExtra("photoset_id", ((TakePhotoEvent) evt).getPhotoSetId());
                String msg = "Someone else's target is nearby!" +
                            " Discreetly take photos of people near you;" +
                            " they'll be sent to that person's assassin" +
                            " and will help them figure out who their target is.";
                showNotification("Take Photos!", msg, TAKE_PHTOS_MSGID, takePhotos);
            }
            // update model, send broadcast
            final PlayingContext finalContext = context;
            new AsyncTask<GameEvent, Void, GameEvent>() {
                @Override
                protected GameEvent doInBackground(GameEvent... params) {
                    assert params.length == 1;
                    finalContext.getGameStorage().addEvent(params[0]);
                    return params[0];
                }
                @Override
                protected void onPostExecute(GameEvent evt) {
                    // send broadcast to update view
                    Intent evtBroadcast = new Intent(GAME_EVENT_ACTION);
                    evtBroadcast.putExtra("event", evt);
                    sendBroadcast(evtBroadcast);
                }
            }.execute(evt);
        } catch (WrongSideException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "An error occurred while receiving a GCM message.", Toast.LENGTH_LONG).show();
        }
    }

    private void showNotification(String title, String body, int id, Intent resultIntent) {
        // TODO: check whether activity is running or not
        Notification.Builder mBuilder = new Notification.Builder(this)
                                                    .setContentTitle(title)
                                                    .setContentText(body)
                                                    .setSmallIcon(R.drawable.ic_launcher);
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
