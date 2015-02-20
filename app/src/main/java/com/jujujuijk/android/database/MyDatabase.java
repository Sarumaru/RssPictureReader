package com.jujujuijk.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jujujuijk.android.rssreader.ApplicationContextProvider;

import java.util.ArrayList;
import java.util.List;

public class MyDatabase extends SQLiteOpenHelper {

    // database version
    private static final int DATABASE_VERSION = 1;

    // the name of the database and the table(s)
    private static String DB_NAME = "db_urls";
    private static String TABLE_NAME = "name_url";

    // For singleton
    private static MyDatabase mInstance = null;
    private final List<Feed> mFeedList = new ArrayList<Feed>();

    // Database watchers to notify when data changed
    private final List<IDatabaseWatcher> mWatcherList = new ArrayList<IDatabaseWatcher>();

    private MyDatabase() {
        super(ApplicationContextProvider.getContext(), DB_NAME, null,
                DATABASE_VERSION);
    }

    public static synchronized MyDatabase getInstance() {
        if (mInstance == null) {
            mInstance = new MyDatabase();
        }
        return mInstance;
    }

    public void follow(IDatabaseWatcher watcher) {
        mWatcherList.add(watcher);
    }

    public void unFollow(IDatabaseWatcher watcher) {
        mWatcherList.remove(watcher);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
                " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "url TEXT NOT NULL, " +
                "notify INTEGER DEFAULT 0, " +
                "item_seen TEXT DEFAULT '', " +
                "item_last TEXT DEFAULT ''" +
                ");";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No upgrades yet
    }

    /**
     * CRUD Operations
     */

    // Adding new feed
    public synchronized long addFeed(Feed feed) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db == null)
            return -1;

        ContentValues values = new ContentValues();
        values.put("name", feed.getName());
        values.put("url", feed.getUrl());
        values.put("notify", feed.getNotify());
        values.put("item_seen", feed.getItemSeen());
        values.put("item_last", feed.getItemLast());

        // Inserting Row
        long id = db.insert(TABLE_NAME, null, values);

        synchronizeFeedList();
        db.close();
        return id;
    }

    public synchronized int getFeedIdx(long id) {
        int ret = 0;
        for (Feed f : mFeedList) {
            if (f.getId() == id) {
                break;
            }
            ret++;
        }
        return ret;
    }

    // Getting single feed
    public synchronized Feed getFeed(long id) {
        Feed ret = null;
        for (Feed f : mFeedList) {
            if (f.getId() == id) {
                ret = f;
                break;
            }
        }
        return ret;
    }

    // Getting All feeds
    public synchronized List<Feed> getAllFeeds() {

        if (mFeedList.size() != 0)
            return mFeedList;

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();

        if (db == null)
            return mFeedList;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Feed contact = new Feed();

                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setName(cursor.getString(1));
                contact.setUrl(cursor.getString(2));

                contact.setNotify(Integer.parseInt(cursor.getString(3)));
                contact.setItemSeen(cursor.getString(4));
                contact.setItemLast(cursor.getString(5));

//                contact.loadCover();

                // Adding feed to list
                mFeedList.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();

        db.close();
        return mFeedList;
    }

    public synchronized  int updateFeed(Feed feed) { return updateFeed(feed, true); }
    // Updating single feed
    private synchronized int updateFeed(Feed feed, boolean synchronize) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (db == null)
            return -1;

        ContentValues values = new ContentValues();
        values.put("name", feed.getName());
        values.put("url", feed.getUrl());
        values.put("notify", feed.getNotify());
        values.put("item_seen", feed.getItemSeen());
        values.put("item_last", feed.getItemLast());

        // updating row
        int ret = db.update(TABLE_NAME, values, "id" + " = ?",
                new String[] { String.valueOf(feed.getId()) });

        db.close();
        if (synchronize)
            synchronizeFeedList();
        return ret;
    }

    // Deleting single feed
    public synchronized void deleteFeed(Feed feed) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (db == null)
            return;

        db.delete(TABLE_NAME, "id = ?",
                new String[] { String.valueOf(feed.getId()) });
        db.close();
        synchronizeFeedList();
    }

    private synchronized void  synchronizeFeedList() {
        mFeedList.clear();
        getAllFeeds();
        for (IDatabaseWatcher i : mWatcherList)
            i.notifyFeedListChanged();
    }

    public void updateFeeds(List<Feed> feedList) {
        for (Feed f : feedList)
            updateFeed(f, false);
        synchronizeFeedList();
    }

    public interface IDatabaseWatcher {
        abstract void notifyFeedListChanged();
    }

}
