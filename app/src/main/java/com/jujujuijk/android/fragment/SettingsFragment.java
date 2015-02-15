package com.jujujuijk.android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jujujuijk.android.dialog.NotificationsDialog;
import com.jujujuijk.android.rssreader.MainActivity;
import com.jujujuijk.android.rssreader.R;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings, container, false);
        List<View> titles = findViewWithTagRecursively((ViewGroup) v, getResources().getString(R.string.yes));
        for (View title : titles) {
            if (!(title instanceof TextView))
                continue;
            ((TextView) title).setText(((TextView) title).getText().toString().toUpperCase());
        }

        LinearLayout ll = (LinearLayout) v.findViewById(R.id.settings_edit_notif);
        ll.setOnClickListener(new SettingsOnClickListener());

        return v;
    }

    /**
     * Get all the views which matches the given Tag recursively
     *
     * @param root parent view. for e.g. Layouts
     * @param tag  tag to look for
     * @return List of views
     */
    public static List<View> findViewWithTagRecursively(ViewGroup root, Object tag) {
        List<View> allViews = new ArrayList<View>();

        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View childView = root.getChildAt(i);

            if (childView instanceof ViewGroup) {
                allViews.addAll(findViewWithTagRecursively((ViewGroup) childView, tag));
            } else {
                final Object tagView = childView.getTag();
                if (tagView != null && tagView.equals(tag))
                    allViews.add(childView);
            }
        }

        return allViews;
    }

    private class SettingsOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.settings_edit_notif:
                    if (!(getActivity() instanceof MainActivity))
                        break;
                    NotificationsDialog notifAlert = new NotificationsDialog((MainActivity) getActivity());

                    notifAlert.setCanceledOnTouchOutside(true);
                    notifAlert.show();
                    break;
            }
        }
    }


}
