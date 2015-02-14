package com.jujujuijk.android.asynctask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

import com.jujujuijk.android.rssreader.ApplicationContextProvider;

public class ImageLoader extends AsyncTask<String, Void, Bitmap> {

    ImageLoaderCallback m_parent;
    int m_id;

    public ImageLoader(ImageLoaderCallback parent, int id) {
        m_parent = parent;
        m_id = id;
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
            BitmapFactory.decodeStream(is,null,o);

            connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            is = connection.getInputStream();

            //The new size we want to scale to
            final int REQUIRED_SIZE= 300;

            //Find the correct scale value. It should be the power of 2.
            int scale=1;
            while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
                scale*=2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;

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
        if (m_parent instanceof Fragment && (((Fragment)m_parent).isRemoving()
                || ((Fragment)m_parent).isDetached() || ((Fragment)m_parent).getActivity() == null))
            return;
        m_parent.onImageLoaderPostExecute(m_id, result);
    }

    public interface ImageLoaderCallback {
        abstract void onImageLoaderPostExecute(int id, Bitmap image);
    }
}
