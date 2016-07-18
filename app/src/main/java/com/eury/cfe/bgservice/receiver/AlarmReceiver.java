package com.eury.cfe.bgservice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.eury.cfe.bgservice.RunTaskUtils;
import com.eury.cfe.bgservice.UsageStatsUtils;

/**
 * Created by aoe on 2016/5/10.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION="TANGYINGRUNTASKTIME";
    public static final String ACTION_LOCK="LockScreenReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        if(!TextUtils.isEmpty(action)){
            if(action.equals(ACTION)){
                boolean flag= UsageStatsUtils.printForegroundTask(context, RunTaskUtils.getInstance().packName);
                RunTaskUtils.getInstance().sendMessage(flag);
            }
            if(action.equals(ACTION_LOCK)){
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_SCREEN_ON);
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                filter.addAction(Intent.ACTION_USER_PRESENT);
                RunTaskUtils.getInstance().mContext.registerReceiver(LockScreenReceiver.getInstance(), filter);
            }
        }
    }
}