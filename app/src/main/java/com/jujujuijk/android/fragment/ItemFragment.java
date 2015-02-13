package com.jujujuijk.android.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.jujujuijk.android.rssreader.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressLint("ValidFragment")
public class ItemFragment extends Fragment {

    public ItemFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle b = getArguments();

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.image_show, container, false);
        TextView meta = (TextView) v.findViewById(R.id.meta);
        TextView title = (TextView) v.findViewById(R.id.title);
        TextView link = (TextView) v.findViewById(R.id.link);
        WebView wv = (WebView) v.findViewById(R.id.webview);

        if (b == null)
            return v;

        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy hh:mm:ss", Locale.ENGLISH);
        Date date = new Date();
        try {
            date = format.parse(b.getString("date"));
        } catch (ParseException e) {

        }

        String metaStr = date.toString();
        for (String i : new String[]{
                "category", "author", "date"
        }) {
            if (b.containsKey(i))
                metaStr += (metaStr.length() > 0 ? " / " : "") + b.getString(i);
        }

        if (metaStr.length() == 0)
            meta.setVisibility(View.GONE);
        else
            meta.setText(metaStr);

        title.setText(b.getString("title"));

        if (b.containsKey("link"))
            link.setText(b.getString("link"));
        else
            link.setVisibility(View.GONE);

        wv.loadDataWithBaseURL("", b.getString("description"), "text/html", "UTF-8", "");

        return v;
    }
}
