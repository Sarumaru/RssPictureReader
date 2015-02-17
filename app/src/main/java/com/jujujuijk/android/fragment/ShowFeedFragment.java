package com.jujujuijk.android.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jujujuijk.android.asynctask.FeedParser;
import com.jujujuijk.android.asynctask.ImageLoader;
import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.database.MyDatabase;
import com.jujujuijk.android.rssreader.MainActivity;
import com.jujujuijk.android.rssreader.R;
import com.jujujuijk.android.tools.MyPagerAdapter;
import com.jujujuijk.android.tools.MyViewPager;

import java.util.List;

public class ShowFeedFragment
        extends Fragment
        implements ImageLoader.ImageLoaderCallback, FeedParser.RssParserCallBack {

    public static final int NB_BASE_ITEMS = 5;
    public static final int NB_BEFORE_CONTINUE_LOAD = 2;
    public static final int NB_MAX_ITEMS = 90;

    private MyViewPager myViewPager = null;
    public MyPagerAdapter mPagerAdapter = null;

    private List<Bundle> mItems = null;

    private Feed mFeed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = (View) inflater.inflate(R.layout.item_pager, container,
                false);

        if (!(getActivity() instanceof MainActivity))
            return null;

        mFeed = ((MainActivity) getActivity()).getCurrentFeed();

        myViewPager = (MyViewPager) v.findViewById(R.id.fragment_container);

        // Creation de l'adapter qui s'occupera de l'affichage de la liste de fragments
        mPagerAdapter = new MyPagerAdapter(getActivity()
                .getSupportFragmentManager());

        myViewPager.setAdapter(mPagerAdapter);
        myViewPager.setParent(this);
        myViewPager.setOnPageChangeListener();

        launchParser();
        return v;
    }

    @Override
    public void onImageLoaderPostExecute(int id, Bitmap image) {
        Bundle b = mItems.get(id);

        mPagerAdapter.delete(mPagerAdapter.getCount() - 1);

        ItemFragment newFragment = new ItemFragment(image);
        newFragment.setArguments(b);
        mPagerAdapter.add(newFragment);

        if (id == 0) { // 1st image
            mFeed.setItemLast(b.getString("title"));
            mFeed.setItemSeen(b.getString("title"));
            MyDatabase.getInstance().updateFeed(mFeed);
        }

        try {
            mPagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (id < mItems.size() - 1 && id < NB_BASE_ITEMS - 1) {
            // continue to load until NB_BASE_ITEMS
            launchLoading(id + 1);
        } else if (myViewPager.getCurrentItem() + NB_BEFORE_CONTINUE_LOAD >= id
                && id + 1 < mItems.size()) {
            // we were waiting for the picture, load next
            launchLoading(id + 1);
        } else {
            myViewPager.mLastLoadedId = id;
        }

    }

    @Override
    public void onRssParserPostExecute(List<Bundle> items, Feed feed) {
        // Toast.makeText(getActivity(), "callback XML", 0).show();
        mPagerAdapter.clear();
        mPagerAdapter.add(new LoadingFragment());
        mItems = items;
        if (mItems != null && mItems.size() > 0) {
            launchLoading(0);
        } else if (mItems == null) {
            mPagerAdapter.delete(mPagerAdapter.getCount() - 1);
            ErrorFragment newFragment = new ErrorFragment(this);
            mPagerAdapter.add(newFragment);

            try {
                mPagerAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else { // m_image.size() == 0
            Toast.makeText(getActivity(), "Unable to find items into XML feed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPagerAdapter != null)
            mPagerAdapter.clear();
    }

    public void launchParser() {
        mPagerAdapter.clear();
        mPagerAdapter.add(new LoadingFragment());
        try {
            mPagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        new FeedParser(this, mFeed).execute(999);
    }

    public void launchLoading(int id) {
        if (id >= NB_MAX_ITEMS || id >= mItems.size())
            return;

        Bundle b = mItems.get(id);

        if (id != 0) {
            mPagerAdapter.add(new LoadingFragment());
            mPagerAdapter.notifyDataSetChanged();
        }
        myViewPager.mLastLoadedId = -1;
        new ImageLoader(this, id).execute(b.getString("imageUrl"));
    }

}
