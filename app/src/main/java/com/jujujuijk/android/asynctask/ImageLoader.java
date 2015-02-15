package com.jujujuijk.android.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoader extends AsyncTask<String, Void, Bitmap> {

    ImageLoaderCallback mParent;
    int mId;

    public ImageLoader(ImageLoaderCallback parent, int id) {
        mParent = parent;
        mId = id;
    }

    /**
     * The system calls this to perform work in a worker thread and delivers it
     * the parameters given to AsyncTask.execute()
     */
    @Override
    protected Bitmap doInBackground(String... urls) {
        String src = urls[0];

        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream is = connection.getInputStream();

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, o);

            connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            is = connection.getInputStream();

            //The new size we want to scale to
            final int REQUIRED_SIZE = 300;

            //Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            return BitmapFactory.decodeStream(is, null, o2);
//            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            System.gc();
            e.printStackTrace();
        }
        return null;
    }

    /**
     * The system calls this to perform work in the UI thread and delivers the
     * result from doInBackground()
     */
    @Override
    protected void onPostExecute(Bitmap result) {
        if (mParent instanceof Fragment && (((Fragment) mParent).isRemoving()
                || ((Fragment) mParent).isDetached() || ((Fragment) mParent).getActivity() == null))
            return;
        mParent.onImageLoaderPostExecute(mId, result);
    }

    public interface ImageLoaderCallback {
        abstract void onImageLoaderPostExecute(int id, Bitmap image);
    }
}
