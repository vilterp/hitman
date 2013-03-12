package com.hitman.client.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.hitman.client.R;
import com.hitman.client.Util;
import com.hitman.client.event.PhotoReceivedEvent;
import com.hitman.client.http.Either;
import com.hitman.client.http.WrongSideException;
import com.hitman.client.model.*;

import java.net.URI;

public class ViewPhoto extends Activity {

    private PlayingContext context;
    private PhotoReceivedEvent evt;

    private ProgressBar progress;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_photo);
        try {
            context = PlayingContext.readFromStorage(LoggedInContext.readFromStorage(new LoggedOutContext(this)));
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
        evt = (PhotoReceivedEvent) getIntent().getSerializableExtra("photo_event");
        // view refs
        progress = (ProgressBar) findViewById(R.id.progress);
        imageView = (ImageView) findViewById(R.id.view_photo_image_view);
        // load image
        new AsyncTask<String, Void, Either<Object,Bitmap>>() {
            @Override
            protected void onPreExecute() {
                imageView.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
            }
            @Override
            protected Either<Object,Bitmap> doInBackground(String... params) {
                assert params.length == 1;
                return context.getBitmap(params[0]);
            }
            @Override
            protected void onPostExecute(Either<Object,Bitmap> res) {
                try {
                    Bitmap bitmap = res.getRight();
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                } catch (WrongSideException e) {
                    Util.handleError(ViewPhoto.this, e);
                }
            }
        }.execute(evt.getPath());
    }

}
