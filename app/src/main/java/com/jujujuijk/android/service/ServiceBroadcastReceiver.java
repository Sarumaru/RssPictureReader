package com.jujujuijk.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.database.MyDatabase;

import java.util.ArrayList;
import java.util.List;

public class ServiceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;
        if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {
            if (!NotificationServiceTools.isMyServiceRunning(context)) {
                NotificationServiceTools.startService(context);
            }
        } else if (intent.getAction() == NotificationService.ACTION_DISMISS) {
            // Retrieve all feeds that we are currently getting notified. update picture_seen to avoid notification untill the next pic is released
            List<Feed> feedList = new ArrayList<Feed>(MyDatabase.getInstance().getAllFeeds());
            List<Feed> toUpdate = new ArrayList<Feed>();

            for (Feed f : feedList) {
                if (f.getNotify() == 1 && !f.getItemLast().equals(f.getItemSeen())) {
                    f.setItemSeen(f.getItemLast());
                    toUpdate.add(f);
                }
            }
            MyDatabase.getInstance().updateFeeds(toUpdate);
        }
    }
}
