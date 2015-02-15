package com.jujujuijk.android.tools;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;

import com.jujujuijk.android.fragment.ShowFeedFragment;

public class MyViewPager extends ViewPager {

    public int mLastLoadedId = -1;
    private ShowFeedFragment mParent = null;
    private PageChangeListener mListener = new PageChangeListener();

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setParent(ShowFeedFragment parent) {
        mParent = parent;
    }

    public void setOnPageChangeListener() {
        super.setOnPageChangeListener(mListener);
    }

    private class PageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int pos) {
            if (mLastLoadedId == -1
                    || mLastLoadedId + 1 < ShowFeedFragment.NB_BASE_ITEMS
                    || mLastLoadedId >= ShowFeedFragment.NB_MAX_ITEMS)
                return;

            if (pos + ShowFeedFragment.NB_BEFORE_CONTINUE_LOAD >= ShowFeedFragment.NB_BASE_ITEMS) {
                int id = mLastLoadedId;
                mLastLoadedId = -1;
                if (mParent != null)
                    mParent.launchLoading(id + 1);
                else
                    Log.v("ERROR", "myviewpager: no parent set");
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
    }
}
