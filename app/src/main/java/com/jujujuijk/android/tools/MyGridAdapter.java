package com.jujujuijk.android.tools;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.rssreader.R;

import java.util.List;

/**
 * Created by Thebestsong on 15/02/15.
 */
public class MyGridAdapter extends BaseAdapter {
    private static final String TAG = "GridAdapter";
    private Activity activity;
    private LayoutInflater inflater;
    private List<Feed> feedList;


    public MyGridAdapter(Activity activity, List<Feed> feedList) {
        this.activity = activity;
        this.feedList = feedList;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return feedList.size();
    }

    @Override
    public Feed getItem(int location) {
        return feedList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.drawer_list_item, null);
        try {
            TextView name = (TextView) convertView.findViewById(R.id.drawerlist_feed_name);
            ImageView cover = (ImageView) convertView.findViewById(R.id.drawerlist_cover);
            ImageView notifyStar = (ImageView) convertView.findViewById(R.id.drawerlist_notifystar);

            // getting feed item
            Feed item = getItem(position);


            name.setText(item.getName());

            if (item.getCover() == null)
                item.loadCover();
            else {
                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    cover.setImageDrawable(item.getCover());
                } else {
                    cover.setImageDrawable(item.getCover());
                }
            }


            if (item.getNotify() != 0)
                notifyStar.setVisibility(View.VISIBLE);
            else
                notifyStar.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "Exception occurred " + e.getClass().getName(), e);
        }
        return convertView;
    }
}
