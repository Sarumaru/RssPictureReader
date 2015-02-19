package com.jujujuijk.android.database;

import android.R;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.jujujuijk.android.asynctask.CoverLoader;
import com.jujujuijk.android.rssreader.ApplicationContextProvider;
import com.jujujuijk.android.rssreader.MainActivity;

import java.util.HashMap;

public class Feed extends HashMap<String, String> {

    private final static String TAG = "Feed";
    private long mId;
    private String mName;
    private String mUrl;
    private Integer mNotify = 0;
    private Drawable mCover;
    private String mItemSeen = "";
    private String mItemLast = "";

    public Feed() {

    }

    public Feed(String name, String url) {
        mName = name;
        mUrl = url;
    }

    public Feed(long id, String name, String url) {
        mId = id;
        mName = name;
        mUrl = url;
    }

    public Feed(long id, String name, String url, int notify, String pictureSeen, String pictureLast) {
        mId = id;
        mName = name;
        mUrl = url;
        mNotify = notify;
        mItemSeen = pictureSeen;
        mItemLast = pictureLast;
    }

    public void loadCover() {
        if (this.mUrl != null) {
            try {
               new CoverLoader(this).execute();
            } catch (Exception e) {
                Log.e(TAG, "Exception occurred " + e.getClass().getName(), e);
            }
        }
    }

    @Override
    public String get(Object k) {
        String key = (String) k;
        if (key.equals("name"))
            return mName;
        else if (key.equals("notifystar")) {
            int res = 0;
            if ((mNotify & Notify.NOTIF) != 0) {
                if (!mItemLast.equals(mItemSeen))
                    res = R.drawable.star_big_on;
                else
                    res = R.drawable.star_big_off;
            }
            return Integer.toString(res);
        }
        return null;
    }

    /**
     * Getters/Setters
     */

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public Drawable getCover() {
        return mCover;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public Integer getNotify() {
        return mNotify;
    }

    public void setNotify(Integer mNotify) {
        this.mNotify = mNotify;
    }

    public String getItemSeen() {
        return mItemSeen;
    }

    public void setItemSeen(String itemSeen) {
        this.mItemSeen = itemSeen;
    }

    public String getItemLast() {
        return mItemLast;
    }

    public void setItemLast(String itemLast) {
        this.mItemLast = itemLast;
    }

    public void setCover(Drawable mCover) {
        this.mCover = mCover;
    }

    static public abstract class Notify {
        static public int NOTIF = (1 << 0);
        static public int LIVE_WALLPAPER = (1 << 1);
    }
}
