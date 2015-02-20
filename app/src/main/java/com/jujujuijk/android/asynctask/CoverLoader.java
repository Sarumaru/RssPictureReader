package com.jujujuijk.android.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.rssreader.ApplicationContextProvider;
import com.jujujuijk.android.tools.MyGridAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Thebestsong on 15/02/15.
 */
public class CoverLoader extends AsyncTask<Void, Void, Drawable> {
    private URL mUrl;
    private String feedName;

    private static final int NB_MAX_LOAD = 5;
    private Document mDoc;
    private Element mRoot;
    private NodeList mNodes;
    private MyGridAdapter mAdapter;

    public CoverLoader(final MyGridAdapter adapter, String Url, String name) {
        feedName = name;
        mAdapter = adapter;
        try {
            mUrl = new URL(Url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Drawable doInBackground(Void... params) {

        URL coverUrl = getCoverUrl();
        if (coverUrl != null)
            try {
                HttpURLConnection connection = (HttpURLConnection) coverUrl.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream is = connection.getInputStream();

                //Decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, o);

                connection = (HttpURLConnection) coverUrl.openConnection();
                connection.setDoInput(true);
                connection.connect();
                is = connection.getInputStream();

                //The new size we want to scale to
                final int REQUIRED_SIZE = 100;

                //Find the correct scale value. It should be the power of 2.
                int scale = 1;
                while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;

                //Decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;

                Bitmap bitmap = BitmapFactory.decodeStream(is, null, o2);

                Drawable cover = new BitmapDrawable(ApplicationContextProvider.getContext().getResources(), bitmap);
                return cover;
            } catch (Exception e) {
                e.printStackTrace();
            }
        return null;
    }


    @Override
    protected void onPostExecute(Drawable cover) {
//        mFeed.setCover(cover);
        mAdapter.onCoverLoaderPostExecute(cover, feedName);
    }


    public interface CoverLoaderCallBack {
        abstract void onCoverLoaderPostExecute(Drawable cover, String feedName);
    }

    private URL getCoverUrl() {

        if (mUrl == null)
            return null;

        try {
            mDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(mUrl.openStream());
            mRoot = mDoc.getDocumentElement();
            mNodes = mRoot.getElementsByTagName("item");
            String imageUrl = null;

            for (int i = 0; i < mNodes.getLength() && i < NB_MAX_LOAD && imageUrl == null; i++) {
                Element elem = (Element) mNodes.item(i);
                NodeList enclosure = elem.getElementsByTagName("enclosure");
                NodeList desc = elem.getElementsByTagName("description");

                if (enclosure.getLength() > 0) {
                    Element tmp = (Element) enclosure.item(0);
                    if (tmp.getAttribute("type").contains("image"))
                        imageUrl = tmp.getAttribute("url");
                }

                if (desc.getLength() > 0) {
                    String strDesc = desc.item(0).getTextContent();
                    if (imageUrl == null) {
                        final Pattern ptrn = Pattern.compile("<img src=\"(.+?)\"/>");
                        final Matcher mtchr = ptrn.matcher(strDesc);
                        if (mtchr.find()) {
                            imageUrl = strDesc.substring(mtchr.start(), mtchr.end()).replace("<img src=\"", "").replace("\"/>", "");
                        }
                    }
                }

            }

            if (imageUrl != null)
                return new URL(imageUrl);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
