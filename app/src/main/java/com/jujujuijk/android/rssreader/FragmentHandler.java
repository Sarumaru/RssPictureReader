package com.jujujuijk.android.rssreader;

import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class FragmentHandler {

    List<Fragment> mList;

    public FragmentHandler() {
        mList = new ArrayList<Fragment>();
    }

    public int size() {
        return mList.size();
    }

    public Fragment get(int location) {
        return mList.get(location);
    }

    public void add(Fragment f) {
        mList.add(f);
    }

    public void insert(int pos, Fragment f) {
        mList.add(pos, f);
    }

    public void delete(int pos) {
        if (pos < mList.size())
            mList.remove(pos);
    }

    public int indexOf(Object o) {
        return mList.indexOf(o);
    }

    public void clear() {
        mList.clear();
    }
}
