package com.hitman.client.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.hitman.client.R;
import com.hitman.client.http.Either;
import com.hitman.client.http.Right;
import com.hitman.client.model.*;
import org.apache.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;

public class TakePictures extends Activity {

    private static final int TAKE_PICTURE_REQ = 0;
    private Button takeAnother;
    private Button imDone;
    private GridView photosGrid;
    private TextView progressInd;

    private PhotoArrayAdapter photos;

    private PlayingContext playingContext;
    private int photoSetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_pictures);
        // get context
        photoSetId = getIntent().getIntExtra("photoSetId", -1);
        try {
            playingContext = PlayingContext.readFromStorage(LoggedInContext.readFromStorage(new LoggedOutContext(this)));
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
        // get refs
        takeAnother = (Button) findViewById(R.id.take_pictures_take_another);
        imDone = (Button) findViewById(R.id.take_pictures_done);
        photosGrid = (GridView) findViewById(R.id.taking_pictures_grid_view);
        progressInd = (TextView) findViewById(R.id.taking_pictures_progress_ind);
        // set handlers
        takeAnother.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePhoto();
            }
        });
        imDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                imDone.setEnabled(false);
                // wish I didn't have to write this...
                List<Bitmap> bitmaps = new ArrayList<Bitmap>();
                for (int i = 0; i < photos.getCount(); i++) {
                    bitmaps.add(photos.getItem(i));
                }
                new AsyncTask<List<Bitmap>, Pair<Integer,Integer>, Pair<Integer,Integer>>() {
                    @Override
                    protected Pair<Integer, Integer> doInBackground(List<Bitmap>... params) {
                        assert params.length == 1;
                        int uploadedSuccessfully = 0;
                        List<Bitmap> theBitmaps = params[0];
                        for (int i = 0; i < theBitmaps.size(); i++) {
                            publishProgress(new Pair<Integer, Integer>(i, theBitmaps.size()));
                            Bitmap theBitmap = theBitmaps.get(i);
                            Either<Object,HttpResponse> res = playingContext.uploadImage(theBitmap, photoSetId);
                            if(res instanceof Right) {
                                uploadedSuccessfully++;
                            }
                        }
                        // x out of y uploaded successfully
                        return new Pair<Integer, Integer>(uploadedSuccessfully, theBitmaps.size());
                    }

                    @Override
                    protected void onProgressUpdate(Pair<Integer,Integer>... values) {
                        assert values.length == 1;
                        Pair<Integer,Integer> prog = values[0];
                        progressInd.setText(String.format("%d/%d uploaded", prog.first, prog.second));
                    }

                    @Override
                    protected void onPostExecute(Pair<Integer, Integer> stats) {
                        Toast.makeText(TakePictures.this, String.format("%d/%d pictures uploaded successfully.",
                                stats.first, stats.second), Toast.LENGTH_LONG).show();
                        finish();
                    }

                }.execute(bitmaps);
            }
        });
        // set up gridview adapter
        photos = new PhotoArrayAdapter(this);
        photosGrid.setAdapter(photos);
    }

    private void dispatchTakePhoto() {
        if(!isPhotoIntentAvailable()) {
            Toast.makeText(this, "Photo intent not available. Install some kind of camera app!", Toast.LENGTH_LONG).show();
        } else {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, TAKE_PICTURE_REQ);
        }
    }

    // http://developer.android.com/training/camera/photobasics.html
    private boolean isPhotoIntentAvailable() {
        final PackageManager packageManager = getPackageManager();
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        assert requestCode == TAKE_PICTURE_REQ;
        assert resultCode == RESULT_OK;
        Bitmap newImage = (Bitmap) data.getExtras().get("data");
        photos.add(newImage);
    }

    // http://developer.android.com/guide/topics/ui/layout/gridview.html
    public class PhotoArrayAdapter extends ArrayAdapter<Bitmap> {

        public PhotoArrayAdapter(Context context) {
            super(context, R.layout.dummy_text_view);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if(convertView == null) {
                imageView = new ImageView(this.getContext());
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageBitmap(this.getItem(position));
            return imageView;
        }

    }

}
