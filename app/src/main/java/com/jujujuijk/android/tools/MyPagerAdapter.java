package com.jujujuijk.android.tools;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

import com.jujujuijk.android.rssreader.FragmentHandler;

public class MyPagerAdapter extends FragmentStatePagerAdapter {

    private FragmentHandler mFh;

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
        mFh = new FragmentHandler();
    }

    @Override
    public Fragment getItem(int pos) {
        return mFh.get(pos);
    }

    @Override
    public int getCount() {
        return mFh.size();
    }

    @Override
    public int getItemPosition(Object o) {
        int pos = mFh.indexOf(o);

        if (pos == -1) {
            return POSITION_NONE;
        }
        return pos;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        FragmentManager manager = ((Fragment) object).getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove((Fragment) object);
        trans.commit();
        Log.v("debug", "Destroy fragment #" + position);

        super.destroyItem(container, position, object);
    }

    public void add(Fragment f) {
        Log.v("list", "List add #" + mFh.size() + " : " + f);
        mFh.add(f);
    }

    public void insert(int pos, Fragment f) {
        mFh.insert(pos, f);
    }

    public void delete(int pos) {
        mFh.delete(pos);
    }

    public void clear() {
        mFh.clear();
    }
}
