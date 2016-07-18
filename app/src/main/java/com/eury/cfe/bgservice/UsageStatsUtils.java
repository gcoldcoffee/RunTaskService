package com.eury.cfe.bgservice;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *开启权限相关管理类
 * Created by aoe on 2016/5/5.
 *
 * 相关资料
 * http://stackoverflow.com/questions/30619349/android-5-1-1-and-above-getrunningappprocesses-returns-my-application-packag/32366476#32366476
 *http://blog.csdn.net/luo6620378xu/article/details/8629098
 * http://blog.csdn.net/chaozhung_no_l/article/details/49490401
 *
 * http://bbs.finereport.com/forum.php?mod=viewthread&tid=63517&extra=page%3D1
 *
 *
 * //        String[] args = {"dumpsys activity activities | sed -En -e '/Running activities/,/Run #0/p'"};
 //        AntiEmulator.CommandResult commandResult=AntiEmulator.execCommand(args,false,true);

 //dumpsys activity
 //dumpsys activity activits
 //adb shell grep "mFocusedActivity"
 //adb shell procrank | grep "com.hamao.ahongbao"
 //dumpsys activity | grep "mFocusedActivity"
 //dumpsys activity | findstr "mFocusedActivity"

 ///proc/%d/status
 ///system/bin/top -n 1
 //        List<String> commandResult1= CommandUtil.execute("proc/bin/status");
 //        Alog.i(TAG,"successMsg:"+commandResult.successMsg);
 //        Alog.i(TAG,"errorMsg:"+commandResult.errorMsg);
 //        Alog.i(TAG,"result:"+commandResult.result);
 //        for(String str:commandResult1){
 //            Alog.i(TAG,"commandResult1:"+str);
 //        }
 *
 *adb shell am force-stop packname
 *
 * 区分SDK >5.0   <5.0
 */
public class UsageStatsUtils {

    private static final String TAG=UsageStatsUtils.class.getSimpleName();

    /**
     * 跳转启使用记录访问权限界面（系统隐藏）
     * @param context
     */
    public static boolean startUsageStatsUI(Context context){
        if (Build.VERSION.SDK_INT < 21){
            return false;
        }
        try{
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return true;
            }
            return false;
        }catch (ActivityNotFoundException exception){
            return false;
        }

    }

    public static String getDeviceInfo(){
        String DEVICE = Build.DEVICE; // 设备参数
        String MODEL = Build.MODEL; // 版本
        String VERSIONRELEASE= Build.VERSION.RELEASE; // 系统版本
        StringBuilder sb=new StringBuilder();
        sb.append("DEVICE："+DEVICE).append("\t").append("MODEL：" + MODEL).append("\t").append("SDK VERSIONRELEASE："+VERSIONRELEASE);
        return sb.toString();
    }

    /**
     * 跳转系统应用商店
     * @param context
     */
    public static void startSysAppShop(Context context){
        Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + context.getPackageName()));
        try
        {
            context.startActivity(localIntent);
        }
        catch (Exception localException)
        {
            context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }


    /**
     * 跳转Android 原生权限管理
     * @param context
     */
    public static void startAppOpsSummary(Context context){
        Intent localIntent2 = new Intent();
        localIntent2.setClassName("com.android.settings", "com.android.settings.Settings");
        localIntent2.setAction("android.intent.action.MAIN");
        localIntent2.addCategory("android.intent.category.DEFAULT");
        localIntent2.setFlags(276856832);
        localIntent2.putExtra(":android:show_fragment", "com.android.settings.applications.AppOpsSummary");
        context.startActivity(localIntent2);
    }


    /**
     * 判断是否开启使用记录访问权限
     * @param paramContext
     * @return
     */
    public static boolean isOpenUsageState(Context paramContext)
    {
        if (Build.VERSION.SDK_INT < 21){
            return true;
        }
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        if (intent.resolveActivity(paramContext.getPackageManager()) == null) {
            Alog.i(TAG,"Not find Settings.ACTION_USAGE_ACCESS_SETTINGS ！"+getDeviceInfo());
            return true;
        }
        try
        {
            String str = getAppUsageStats(paramContext, 100000L);
            if (!isEmptyStrs(new String[]{str})){
                return true;
            }
        }catch (Throwable localThrowable){
        }
        return false;
    }

    /**
     * 获取应用使用统计相关信息
     * @param paramContext
     * @param paramLong
     * @return
     */
    public static String getAppUsageStats(Context paramContext, long paramLong)
    {
        String str = null;
        if (Build.VERSION.SDK_INT < 21)
            return null;
        try
        {
            long l = System.currentTimeMillis();
            Class localClass1 = Class.forName("android.app.usage.UsageStatsManager");
            Object localObject1 = paramContext.getSystemService(Context.USAGE_STATS_SERVICE);
            Method localMethod1 = localClass1.getMethod("queryUsageStats", new Class[]{Integer.TYPE, Long.TYPE, Long.TYPE});
            List localList = (List)localMethod1.invoke(localObject1, new Object[] { Integer.valueOf(4), Long.valueOf(l - paramLong), Long.valueOf(l) });
            if ((localList != null) && (!localList.isEmpty()))
            {
                Class localClass2 = Class.forName("android.app.usage.UsageStats");
                Method localMethod2 = localClass2.getMethod("getLastTimeUsed", new Class[0]);
                Method localMethod3 = localClass2.getMethod("getPackageName", new Class[0]);
                TreeMap localTreeMap = new TreeMap();
                Object localObject2 = localList.iterator();
                while (((Iterator)localObject2).hasNext())
                {
                    Object localObject3 = ((Iterator)localObject2).next();
                    try
                    {
                        Object localObject4 = localMethod2.invoke(localObject3, new Object[0]);
                        if (localObject4 == null)
                            continue;
                        localTreeMap.put(Long.valueOf(localObject4.toString()), localObject3);
                    }catch (Throwable localThrowable2){
                    }
                }
                if (!localTreeMap.isEmpty())
                {
                    localObject2 = localMethod3.invoke(localTreeMap.get(localTreeMap.lastKey()), new Object[0]);
                    str = localObject2 == null ? null : localObject2.toString();
                }
            }
        }catch (Throwable localThrowable1){
        }
        return (String)str;
    }



    public  static void printForegroundTask(Context context,String packName,BgService.RunTaskTopImpl runTaskTopImpl) {
        String currentApp = null;
        boolean flag=false;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            currentApp = cn.getPackageName();
        }
        if(currentApp!=null && !"".equals(currentApp) && packName!=null && !"".equals(packName)){
            if(currentApp.equals(packName)){
                flag=true;
            }
        }
        if(runTaskTopImpl!=null){
            runTaskTopImpl.runTaskTop(flag,currentApp);
        }
    }

    public  static boolean printForegroundTask(Context context,String packName) {
        String currentApp = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {

            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            currentApp = cn.getPackageName();
        }
        if(currentApp!=null && !"".equals(currentApp) && packName!=null && !"".equals(packName)){
            if(currentApp.equals(packName)){
                return true;
            }
        }

        return false;
    }

    public static boolean isEmptyStrs(String[] paramArrayOfString)
    {
        try
        {
            if (paramArrayOfString == null)
                return true;
            for (String str : paramArrayOfString)
            {
                if (str == null)
                    return true;
                if (str.trim().length() == 0)
                    return true;
            }
            return false;
        }catch (Throwable localThrowable){
        }
        return true;
    }




}
