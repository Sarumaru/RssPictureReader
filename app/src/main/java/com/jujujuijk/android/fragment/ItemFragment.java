package com.jujujuijk.android.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jujujuijk.android.rssreader.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@SuppressLint("ValidFragment")
public class ItemFragment extends Fragment {

    Bitmap mImage = null;

    public ItemFragment() {
    }

    public ItemFragment(Bitmap image) {
        mImage = image;
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
        ImageView image = (ImageView) v.findViewById(R.id.image);
        WebView wv = (WebView) v.findViewById(R.id.webview);

        if (b == null)
            return v;

        String dateStr = null;
        if (b.containsKey("date")) {
            try {
                Date date = new SimpleDateFormat("dd MMM yyyy hh:mm:ss", Locale.ENGLISH).parse(b.getString("date"));
                Date now = new Date();
                long diffMs = now.getTime() - date.getTime();

                if (TimeUnit.MILLISECONDS.toDays(diffMs) > 0)
                    dateStr = String.valueOf(TimeUnit.MILLISECONDS.toDays(diffMs)) + " " + getResources().getString(R.string.days);
                else if (TimeUnit.MILLISECONDS.toHours(diffMs) > 0)
                    dateStr = String.valueOf(TimeUnit.MILLISECONDS.toHours(diffMs)) + " " + getResources().getString(R.string.hours);
                else if (TimeUnit.MILLISECONDS.toMinutes(diffMs) > 0)
                    dateStr = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(diffMs)) + " " + getResources().getString(R.string.minutes);

                if (getResources().getConfiguration().locale.equals(Locale.FRANCE))
                    dateStr = getResources().getString(R.string.ago) + " " + dateStr;
                else
                    dateStr += " " + getResources().getString(R.string.ago);

            } catch (ParseException e) {
            }
        }

        String metaStr = "";
        for (String i : new String[]{
                "category", "author"
        }) {
            if (b.containsKey(i))
                metaStr += (metaStr.length() > 0 ? " / " : "") + b.getString(i);
        }
        if (dateStr != null && dateStr.length() > 0)
            metaStr += (metaStr.length() > 0 ? " / " : "") + dateStr;

        if (metaStr.length() == 0)
            meta.setVisibility(View.GONE);
        else
            meta.setText(metaStr);

        title.setText(b.getString("title"));

        if (b.containsKey("link"))
            link.setText(b.getString("link"));
        else
            link.setVisibility(View.GONE);

        if (mImage != null)
            image.setImageBitmap(mImage);

        wv.loadDataWithBaseURL("", b.getString("description"), "text/html", "UTF-8", "");

        return v;
    }
}
