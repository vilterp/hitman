package com.hitman.client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gcm.GCMBaseIntentService;
import com.hitman.client.activity.*;
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
    private static final int PHOTO_RECEIVED_MSGID = 7;
    private static final int KILLED_MSGID = 8;
    private static final int YOU_WON_MSGID = 9;
    private static final int GAME_CANCELLED_MSGID = 10;

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
            evt = new GameStartedEvent(DateTime.now(), intent.getStringExtra("target"), intent.getStringExtra("kill_code"));
        } else if(type.equals("location_moving")) {
            evt = new MovingLocationEvent(DateTime.now(), intent.getStringExtra("locationFrom"), intent.getStringExtra("locationTo"));
        } else if(type.equals("new_target")) {
            evt = new TargetAssignedEvent(DateTime.now(), intent.getStringExtra("target"));
        } else if(type.equals("killed")) {
            evt = new KillEvent(DateTime.now(), intent.getStringExtra("victim"));
        } else if(type.equals("take_photo")) {
            evt = new TakePhotoEvent(DateTime.now(), Integer.parseInt(intent.getStringExtra("photoset")));
        } else if(type.equals("photo_received")) {
            evt = new PhotoReceivedEvent(DateTime.now(), intent.getStringExtra("url"));
        } else if(type.equals("game_end")) {
            evt = new GameWonEvent(DateTime.now());
        } else if(type.equals("game_canceled")) {
            evt = new GameCanceledEvent(DateTime.now());
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
            Log.i(TAG, "playing context not found; stopping");
            stopSelf();
        }

        // TODO: checkForNull or something
        Either<Exception, GameEvent> parseRes = parseMessage(intent);
        try {
            GameEvent evt = parseRes.getRight();
            Intent showGameIntent = new Intent(this, ShowGame.class);
            if(evt instanceof StationaryLocationEvent) {
                showNotification("Target Update", evt.getHumanReadableDescr(), LOCATION_STATIONARY_MSGID, null, showGameIntent);
            } else if(evt instanceof MovingLocationEvent) {
                showNotification("Target Update", evt.getHumanReadableDescr(), LOCATION_MOVING_MSGID, null, showGameIntent);
            } else if(evt instanceof TargetAssignedEvent) {
                if(evt instanceof GameStartedEvent) {
                    showNotification("Game Started!", evt.getHumanReadableDescr(), GAME_STARTED_MSGID, null, showGameIntent);
                    context.getGameStorage().setKillCode(((GameStartedEvent)evt).getKillCode());
                } else {
                    showNotification("Target Assigned", evt.getHumanReadableDescr(), TARGET_REASSIGNED_MSGID, null, showGameIntent);
                }
                context.getSessionStorage().setCurrentTarget(((TargetAssignedEvent)evt).getNewTarget());
            } else if(evt instanceof JoinEvent) {
                showNotification("New Player Joined", evt.getHumanReadableDescr(), JOIN_MSGID, null, showGameIntent);
            } else if(evt instanceof KillEvent) {
                String currentUser = null;
                try {
                    currentUser = context.getSessionStorage().readLoginCredentials().getUsername();
                } catch (SessionStorage.NoCredentialsException e) {
                    throw new RuntimeException(e);
                }
                KillEvent killEvent = (KillEvent) evt;
                if(killEvent.getVictim().equals(currentUser)) {
                    // current user killed
                    Intent gameListIntent = new Intent(this, GameList.class);
                    showNotification("You Were Killed!", "Better luck next time.", KILLED_MSGID, null, gameListIntent);
                    context.leaveGame();
                } else {
                    // other user killed
                    context.getGameStorage().removePlayerByName(killEvent.getVictim());
                }
            } else if(evt instanceof TakePhotoEvent) {
                Intent takePhotos = new Intent(this, TakePhotos.class);
                takePhotos.putExtra("photoset_id", ((TakePhotoEvent) evt).getPhotoSetId());
                String msg = "Someone else's target is nearby!" +
                            " Discreetly take photos of people near you;" +
                            " they'll be sent to that person's assassin" +
                            " and will help them figure out who their target is.";
                showNotification("Take Photos!", msg, TAKE_PHTOS_MSGID, null, takePhotos);
            } else if(evt instanceof PhotoReceivedEvent) {
                Intent viewPhoto = new Intent(this, ViewPhoto.class);
                viewPhoto.putExtra("photo_event", evt);
                String msg = "Photo of your target received! Click to view.";
                showNotification("Photo Received", msg, PHOTO_RECEIVED_MSGID, ((PhotoReceivedEvent) evt).getPath(), viewPhoto);
            } else if(evt instanceof GameWonEvent) {
                showNotification("You Won!", "Congratulations.", YOU_WON_MSGID, null, new Intent(this, GameList.class));
                context.leaveGame();
            } else if(evt instanceof GameCanceledEvent) {
                showNotification("Game Canceled", evt.getHumanReadableDescr(), GAME_CANCELLED_MSGID, null,
                        new Intent(this, GameList.class));
                context.leaveGame();
            }
            // update model, send broadcast
            if(context.getGameStorage().isActive()) {
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
                        sendEventBroadcast(evt);
                    }
                }.execute(evt);
            } else {
                sendEventBroadcast(evt);
            }
        } catch (WrongSideException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "An error occurred while receiving a GCM message.", Toast.LENGTH_LONG).show();
        }
    }

    private void sendEventBroadcast(GameEvent evt) {
        Intent evtBroadcast = new Intent(GAME_EVENT_ACTION);
        evtBroadcast.putExtra("event", evt);
        sendBroadcast(evtBroadcast);
    }

    private void showNotification(String title, String body, int id, String tag, Intent resultIntent) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                                                    .setContentTitle(title)
                                                    .setContentText(body)
                                                    .setSmallIcon(R.drawable.ic_launcher)
                                                    .setAutoCancel(true)
                                                    .setContentIntent(contentIntent);
        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        if(tag == null) {
            tag = title;
        }
        notificationManager.notify(tag, id, builder.build());
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
