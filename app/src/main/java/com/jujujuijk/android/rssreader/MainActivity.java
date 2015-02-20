package com.jujujuijk.android.rssreader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.Toast;

import com.jujujuijk.android.asynctask.AutocompleteDataParser;
import com.jujujuijk.android.asynctask.CoverLoader;
import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.database.MyDatabase;
import com.jujujuijk.android.dialog.AddFeedDialog;
import com.jujujuijk.android.fragment.SettingsFragment;
import com.jujujuijk.android.fragment.ShowFeedFragment;
import com.jujujuijk.android.service.NotificationService;
import com.jujujuijk.android.service.NotificationServiceTools;
import com.jujujuijk.android.tools.MyGridAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements MyDatabase.IDatabaseWatcher {

    // Settings fragment
    private final SettingsFragment mSettingsFragment = new SettingsFragment();

    // Attributes to perform DrawerLayout
    private DrawerLayout mDrawerLayout;
    private FrameLayout mDrawerList;
    private GridView mDrawerGrid;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    // Synchronized list of available feeds
    private List<Feed> mFeedList = null;
    // Feed being currently displayed
    private Feed mCurrentFeed = null;
    // Hack to change feed when clicked from notif and app in bg.
    // Cant change fragment from onNewIntent(); mNextPosition set in onNewIntent() then used in onResume()
    private int mNextPosition = -1;
    // Synchronized lists to get all suggestions and their url
    private ArrayList<String> mAutocompleteNameList = new ArrayList<String>();
    private ArrayList<String> mAutocompleteUrlList = new ArrayList<String>();

    public List<Feed> getFeedList() {
        return mFeedList;
    }

    public Feed getCurrentFeed() {
        return mCurrentFeed;
    }

    public ArrayList<String> getAutocompleteNameList() {
        return mAutocompleteNameList;
    }

    public ArrayList<String> getAutocompleteUrlList() {
        return mAutocompleteUrlList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // Common tasks whatever Android version
        onCreateCommonTasks();

        // Init DrawerLayout
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (FrameLayout) findViewById(R.id.left_drawer);
        mDrawerGrid = (GridView) findViewById(R.id.grid_feed_list);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // set up the drawer's list view with items and click listener
        final MyGridAdapter adapter = new MyGridAdapter(this, mFeedList);
        mDrawerGrid.setAdapter(adapter);
        mDrawerGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        setCovers();
       /* mDrawerList.setAdapter(new SimpleAdapter(MainActivity.this,
                mFeedList,
                R.layout.drawer_list_item,
                new String[]{"name", "notifystar"},
                new int[]{R.id.drawerlist_feed_name, R.id.drawerlist_notifystar}));*/
        /*mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });*/

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= 14)
            getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                adapter.notifyDataSetChanged();
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Launch a specific Feed if an existing id is provided. Launch 0 otherwise
        if (getIntent() != null && getIntent().getLongExtra(NotificationService.NOTIFICATION_FEED_START, -1) != -1) {
            long feedId = getIntent().getLongExtra(NotificationService.NOTIFICATION_FEED_START, -1);
            selectItem(MyDatabase.getInstance().getFeedIdx(feedId));
        } else if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    private void setCovers() {
        try {
            for (int i = 0; i < mFeedList.size(); i++) {
                new CoverLoader(((MyGridAdapter) mDrawerGrid.getAdapter()),
                        mFeedList.get(i).getUrl(), mFeedList.get(i).getName()).execute();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Exception occurred " + e.getClass().getName(), e);
        }
    }

    private void onCreateCommonTasks() {
        // Get all feeds from database
        mFeedList = MyDatabase.getInstance().getAllFeeds();

        if (mFeedList.size() == 0) {
            putBaseUrls();
        }

        // Fill lists for autocomplete where adding new feed
        new AutocompleteDataParser(mAutocompleteNameList,
                mAutocompleteUrlList).execute("boap");

        // Check for the notification service
        NotificationServiceTools.tryStartService(this);
    }

    private void launchFeed(int pos) {

        Feed feed = mFeedList.get(pos);

        if (feed == null)
            feed = mFeedList.get(0);

        mCurrentFeed = feed;

        // Remove settings if the fragment is visible
        hideSettings();

        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out,
                    android.R.anim.slide_in_left, android.R.anim.fade_out);
            ft.replace(R.id.main_fragment, new ShowFeedFragment(), "item").commit();

            setTitle(mCurrentFeed.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.gc();
    }

    public boolean addNewUrl(String newName, String newUrl) {
        if (newName.length() <= 0 || newUrl.length() <= 0)
            return (true);

        MyDatabase.getInstance().addFeed(new Feed(newName, newUrl));
        new CoverLoader(((MyGridAdapter) mDrawerGrid.getAdapter()), newUrl, newName).execute();

        return (false);
    }

    private void deleteUrl(int pos) {

        MyDatabase.getInstance().deleteFeed(mFeedList.get(pos));

        if (mFeedList.size() == 0) {
            putBaseUrls();
        }

        if (pos >= mFeedList.size()) // We've removed the last item in the list.
            selectItem(mFeedList.size() - 1, true);
        else
            selectItem(pos, true);
    }

    private void putBaseUrls() {
        Toast.makeText(this,
                getResources().getString(R.string.base_url_refill),
                Toast.LENGTH_LONG).show();

        String[] nameList = getResources().getStringArray(R.array.base_names);
        String[] urlList = getResources().getStringArray(R.array.base_urls);

        for (int i = 0; i < nameList.length; ++i) {
            MyDatabase.getInstance().addFeed(new Feed(nameList[i], urlList[i]));
        }
    }

    private boolean handleButtonClick(int id) {
//        final int pos = mDrawerList.getCheckedItemPosition();
        final int pos = mDrawerGrid.getCheckedItemPosition();

        AlertDialog.Builder builder = null;
        Dialog dialog = null;

        switch (id) {
            case R.id.button_reload_feed:
                launchFeed(pos);
                break;
            case R.id.button_add_feed:
                AddFeedDialog alert = new AddFeedDialog(this);

                alert.setCanceledOnTouchOutside(true);
                alert.show();
                break;
            case R.id.button_remove_feed:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.delete) + " : "
                        + mFeedList.get(pos).getName());
                builder.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteUrl(pos);
                            }
                        });
                builder.setNegativeButton(R.string.no, null);

                // Create the AlertDialog object and return it
                dialog = builder.create();

                dialog.show();
                break;
            case R.id.button_settings:
                showSettings();
                break;
            default:
                return false;
        }
        return true;
    }

    private long removeLiveWallpaper() {
        long oldFeedId = -1;
        synchronized (mFeedList) {
            for (Feed f : mFeedList) {
                if ((f.getNotify() & Feed.Notify.LIVE_WALLPAPER) != 0) {
                    oldFeedId = f.getId();
                    f.setNotify(f.getNotify() & ~Feed.Notify.LIVE_WALLPAPER);
                }
            }
        }
        MyDatabase.getInstance().updateFeeds(mFeedList);
        return oldFeedId;
    }

    public void selectLastItem() {
        selectItem(mFeedList.size() - 1, true);
    }

    private void selectItem(int position) {
        if (position < 0 || position >= mFeedList.size())
            position = 0;
        selectItem(position, false);
    }

    private void selectItem(int position, boolean keepDrawerList) {
        if (mCurrentFeed == null || (mCurrentFeed.getId() != mFeedList.get(position).getId())) { // We didnt select the same item
            launchFeed(position);
            // update selected item and title, then close the drawer if wanted
            // mDrawerList.setItemChecked(position, true);
            mDrawerGrid.setItemChecked(position, true);
            mTitle = mFeedList.get(position).getName();
        }

        // Remove settings anyway if the fragment is visible
        if (getSupportFragmentManager().findFragmentByTag("settings") != null)
            getSupportFragmentManager().popBackStack();

        if (!mDrawerLayout.isDrawerOpen(mDrawerList))
            setTitle(mFeedList.get(position).getName());

        if (keepDrawerList == false) {
            setTitle(mFeedList.get(position).getName());
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    public void showSettings() {
        try {
            // Breaks if the "settings" fragment is visible and close it (popbackstack)
            if (getSupportFragmentManager().findFragmentByTag("settings") != null) {
                hideSettings();
                return;
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            ft.addToBackStack(null);

            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                    android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            Fragment prev = getSupportFragmentManager().findFragmentByTag("item");
            if (prev != null)
                ft.hide(prev);

            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out,
                    android.R.anim.slide_in_left, android.R.anim.fade_out);

            ft.add(R.id.main_fragment, mSettingsFragment, "settings");
            ft.commit();

            mDrawerLayout.closeDrawer(mDrawerList); // Close drawer

            setTitle(getResources().getString(R.string.menu_settings));

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.gc();
    }

    public void hideSettings() {
        hideSettings(true);
    }

    public void hideSettings(boolean emulateBackButton) {
        if (getSupportFragmentManager().findFragmentByTag("settings") == null)
            return; // Settings are not present

        if (emulateBackButton)
            getSupportFragmentManager().popBackStack();

        if (mCurrentFeed != null)
            setTitle(mCurrentFeed.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action buttons
        if (handleButtonClick(item.getItemId()) == false)
            return super.onOptionsItemSelected(item);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        MyDatabase.getInstance().unFollow(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        MyDatabase.getInstance().follow(this);
        if (mNextPosition != -1) {
            selectItem(mNextPosition);
            mNextPosition = -1;
        }
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null && intent.getLongExtra(NotificationService.NOTIFICATION_FEED_START, -1) != -1) {
            long feedId = intent.getLongExtra(NotificationService.NOTIFICATION_FEED_START, -1);
            mNextPosition = MyDatabase.getInstance().getFeedIdx(feedId);
        }
    }

    @Override
    public void onBackPressed() {
        hideSettings(false);
        super.onBackPressed();
    }

    @Override
    public void notifyFeedListChanged() {
//        ((SimpleAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
        ((MyGridAdapter) mDrawerGrid.getAdapter()).notifyDataSetChanged();
    }

}
