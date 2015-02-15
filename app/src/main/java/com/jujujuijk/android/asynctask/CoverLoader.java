package com.jujujuijk.android.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.rssreader.ApplicationContextProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Thebestsong on 15/02/15.
 */
public class CoverLoader extends AsyncTask<Void, Void, Drawable> {
    protected Feed mFeed;
    protected URL mUrl;

    private static final int NB_MAX_LOAD = 5;
    private Document mDoc;
    private Element mRoot;
    private NodeList mNodes;

    public CoverLoader(final Feed feed) {
        mFeed = feed;
        try {
            mUrl = new URL(mFeed.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Drawable doInBackground(Void... params) {

        URL coverUrl = getCoverUrl();
        if (coverUrl != null)
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(coverUrl.openConnection().getInputStream());

                Drawable cover = new BitmapDrawable(ApplicationContextProvider.getContext().getResources(), bitmap);
                return cover;
            } catch (Exception e) {
                e.printStackTrace();
            }
        return null;
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

                if (enclosure.getLength() > 0) {
                    Element tmp = (Element) enclosure.item(0);
                    if (tmp.getAttribute("type").contains("image"))
                        imageUrl = tmp.getAttribute("url");
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
