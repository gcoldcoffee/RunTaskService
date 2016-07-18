package com.eury.cfe.bgservice;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * Created by aoe on 2016/5/10.
 */
public class SystemAPI {

    public static NotificationManager a(Context paramContext)
    {
        return (NotificationManager)paramContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static TelephonyManager b(Context paramContext)
    {
        return (TelephonyManager)paramContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public static WifiManager c(Context paramContext)
    {
        return (WifiManager)paramContext.getSystemService(Context.WIFI_SERVICE);
    }

    public static AlarmManager d(Context paramContext)
    {
        return (AlarmManager)paramContext.getSystemService(Context.ALARM_SERVICE);
    }

    public static ActivityManager e(Context paramContext)
    {
        return (ActivityManager)paramContext.getSystemService(Context.ACTIVITY_SERVICE);
    }


    // 开启定时服务
    public static void startBroadcast(Context context, long initialDelay,long period,Class<?> cls, String action) {
        if(context!=null){
            // 获取AlarmManager系统服务
            AlarmManager manager = SystemAPI.d(context);
            Intent intent = new Intent(context, cls);
            intent.setAction(action);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, initialDelay, period, pendingIntent);
            Alog.i("SystemAPI","START AlarmManager Broadcast");
        }
    }

    // 停止定时服务
    public static void stopBroadcast(Context context, Class<?> cls, String action) {
        if(context!=null){
            AlarmManager manager = SystemAPI.d(context);
            Intent intent = new Intent(context, cls);
            intent.setAction(action);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // 取消
            manager.cancel(pendingIntent);
            Alog.i("SystemAPI", "STOP AlarmManager Broadcast");
        }
    }

}
