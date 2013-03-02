package com.hitman.client.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import com.hitman.client.model.Game;

public class JoinGameDialogFragment extends DialogFragment {

    private static final String TAG = "HITMAN-joingamedialog";

    private JoinGameDialogListener listener;
    private Game game;

    public JoinGameDialogFragment(Game game) {
        this.game = game;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Join this game?")
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "join game");
                        listener.onPositiveClick(game);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "cancel");
                        getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (JoinGameDialogListener) activity;
    }

    public interface JoinGameDialogListener {
        public void onPositiveClick(Game game);
    }

}
