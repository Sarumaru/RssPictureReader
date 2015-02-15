package com.jujujuijk.android.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.jujujuijk.android.rssreader.R;

@SuppressLint("ValidFragment")
public class ErrorFragment extends Fragment {

    ShowFeedFragment mParent = null;

    public ErrorFragment() {
    }

    public ErrorFragment(ShowFeedFragment parent) {
        mParent = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.item_error, container, false);
        ImageButton b = (ImageButton) v.findViewById(R.id.button_reload_feed);

        if (mParent != null) {
            b.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mParent.launchParser();
                }
            });
        }

        return v;
    }
}
