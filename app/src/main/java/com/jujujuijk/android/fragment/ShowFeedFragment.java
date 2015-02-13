package com.jujujuijk.android.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jujujuijk.android.rssreader.MainActivity;
import com.jujujuijk.android.rssreader.R;
import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.database.MyDatabase;
import com.jujujuijk.android.asynctask.ImageLoader;
import com.jujujuijk.android.asynctask.FeedParser;
import com.jujujuijk.android.tools.MyPagerAdapter;

import java.util.List;

public class ShowFeedFragment
        extends Fragment
        implements FeedParser.RssParserCallBack {

    public static final int NB_BASE_ITEMS = 5;
    public static final int NB_BEFORE_CONTINUE_LOAD = 2;
    public static final int NB_MAX_ITEMS = 90;

    public MyPagerAdapter mPagerAdapter = null;

    private List<Bundle> mItems = null;

    private Feed mFeed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = (View) inflater.inflate(R.layout.image_pager, container,
                false);

        if (!(getActivity() instanceof MainActivity))
            return null;

        mFeed = ((MainActivity)getActivity()).getCurrentFeed();

        ViewPager vp = (ViewPager) v.findViewById(R.id.fragment_container);

        mPagerAdapter = new MyPagerAdapter(getActivity()
                .getSupportFragmentManager());

        vp.setAdapter(mPagerAdapter);

        launchParser();
        return v;
    }

    @Override
    public void onRssParserPostExecute(List<Bundle> items, Feed feed) {
        // Toast.makeText(getActivity(), "callback XML", 0).show();
        mItems = items;

        if (mItems == null) {
            mPagerAdapter.delete(mPagerAdapter.getCount() - 1);
            ErrorFragment newFragment = new ErrorFragment(this);
            mPagerAdapter.add(newFragment);

            try {
                mPagerAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else if (mItems.size() == 0) {
            Toast.makeText(getActivity(), "Unable to find items into XML feed",
                    Toast.LENGTH_SHORT).show();
        } else {
            mPagerAdapter.delete(mPagerAdapter.getCount() - 1);
            for (Bundle b : mItems) {

                ItemFragment newFragment = new ItemFragment();
                newFragment.setArguments(b);
                mPagerAdapter.add(newFragment);

//                For notification purpose
//                if (id == 0) { // 1st image
//                    mFeed.setPictureLast(b.getString("url"));
//                    mFeed.setPictureSeen(b.getString("url"));
//                    MyDatabase.getInstance().updateFeed(mFeed);
//                }

                try {
                    mPagerAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
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

}
