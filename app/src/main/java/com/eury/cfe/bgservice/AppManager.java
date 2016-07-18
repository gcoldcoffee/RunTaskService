package com.eury.cfe.bgservice;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppManager {

	public static final String TAG = "=AppManager=";

	public Context context;


	public static AppManager appManager;

	public static AppManager getInstance(Context context) {
		if (appManager == null) {
			appManager = new AppManager(context);
		}
		return appManager;
	}

	public AppManager(Context context) {
		super();
		this.context = context;
	}

	/**
	 * 获取所有已安装的应用
	 * @return
	 */
	public ArrayList<MyAppInfo> queryAppInfo() {
		ArrayList<MyAppInfo> mlistAppInfo = new ArrayList<MyAppInfo>();
		PackageManager pm = context.getPackageManager(); // 获得PackageManager对象
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent,
				PackageManager.GET_INTENT_FILTERS);
		Collections.sort(resolveInfos,
				new ResolveInfo.DisplayNameComparator(pm));
		if (mlistAppInfo != null) {
			mlistAppInfo.clear();
			for (ResolveInfo reInfo : resolveInfos) {
				// String activityName = reInfo.activityInfo.name; //
				// 获得该应用程序的启动Activity的name
				String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
				String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
				Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
				String versionName = getVersionName(pkgName);// 版本号
				// 为应用程序的启动Activity 准备Intent
				// Intent launchIntent = new Intent();
				// launchIntent.setComponent(new ComponentName(pkgName,
				// activityName));
				// 创建一个AppInfo对象，并赋值
				MyAppInfo newInfo = new MyAppInfo();
				newInfo.appName = appLabel;
				newInfo.packName = pkgName;
				newInfo.appIcon = icon;
				newInfo.versionName = versionName;
				// Alog.i(TAG,
				// "newInfo.appName:"+newInfo.appName+"==newInfo.packName:"+newInfo.packName+"=newInfo.versionCode:"+newInfo.versionName);
				mlistAppInfo.add(newInfo); // 添加至列表中
			}
		}
		return mlistAppInfo;
	}

	/**
	 * 获取应用版本号
	 * @param packName
	 * @return
	 */
	public String getVersionName(String packName) {
		PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packs = packageManager.getInstalledPackages(0);
		for (PackageInfo packageInfo : packs) {
			// if(filterApp(packageInfo.applicationInfo)){
			String pack_name = packageInfo.packageName;
			if (pack_name != null && pack_name.equals(packName)) {
				String versionName = packageInfo.versionName;
				return versionName;
			}
			// }
		}
		return null;
	}

	/**
	 * 获取对应应用信息
	 * flag 表示是否获取全部应用包括系统应用（true 是    false 否）
	 */
	 public ArrayList<MyAppInfo> getInstalledApps(boolean flag) {
		 ArrayList<MyAppInfo> res = new ArrayList<MyAppInfo>();
		 PackageManager packageManager=context.getPackageManager();
		 List<PackageInfo> packs = packageManager.getInstalledPackages(0);
		 for (PackageInfo packageInfo : packs) {
		 // ApplicationInfo appinfo = packageInfo.applicationInfo;
			 if(flag){
				 MyAppInfo newInfo = new MyAppInfo();
				 newInfo.appName =
				 packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
				 newInfo.packName = packageInfo.packageName;
				 newInfo.versionName = packageInfo.versionName;
				 newInfo.appIcon =
				 packageInfo.applicationInfo.loadIcon(context.getPackageManager());
				 res.add(newInfo);
			 }else {
				 if(filterApp(packageInfo.applicationInfo)){
					 MyAppInfo newInfo = new MyAppInfo();
					 newInfo.appName =
					 packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
					 newInfo.packName = packageInfo.packageName;
					 newInfo.versionName = packageInfo.versionName;
					 newInfo.appIcon =
					 packageInfo.applicationInfo.loadIcon(context.getPackageManager());
					 res.add(newInfo);
					 // newInfo.isSystem=false;
				 }
			}
			 // else {
			 // newInfo.isSystem=true;
			 // }
		 }
	 	return res;
	 }

	public boolean filterApp(ApplicationInfo info) {
		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			return true;
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
			return true;
		}
		return false;
	}

	/**
	 * 系统总内存
	 * @return
	 */
	public long getTotalMemory() {
		try {
			FileInputStream fis = new FileInputStream(new File("/proc/meminfo"));
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String totalInfo = br.readLine();
			StringBuffer sb = new StringBuffer();
			for (char c : totalInfo.toCharArray()) {
				if (c >= '0' && c <= '9') {
					sb.append(c);
				}
			}
			long bytesize = Long.parseLong(sb.toString()) * 1024;
			return bytesize;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 运行进程总个数
	 * @param context
	 * @return
	 */
	public int getRunningPocessCount(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcessInfos = am
				.getRunningAppProcesses();
		int count = runningAppProcessInfos.size();
		return count;
	}

	/**
	 * 获取可用内存大小
	 * @param context
	 * @return
	 */
	public long getAvailMemory(Context context) {

		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		return mi.availMem;
	}


	/**
	 * 清理进程 释放运行内存
	 * @param context
	 */
	public void killRunningPocess(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> infoList = am.getRunningAppProcesses();
		List<ActivityManager.RunningServiceInfo> serviceInfos = am
				.getRunningServices(100);
		int count = 0;
		if (infoList != null) {
			for (int i = 0; i < infoList.size(); ++i) {
				RunningAppProcessInfo appProcessInfo = infoList.get(i);
				// importance 该进程的重要程度 分为几个级别，数值越低就越重要。
				// 一般数值大于RunningAppProcessInfo.IMPORTANCE_SERVICE的进程都长时间没用或者空进程了
				// 一般数值大于RunningAppProcessInfo.IMPORTANCE_VISIBLE的进程都是非可见进程，也就是在后台运行着
				if (appProcessInfo.importance > RunningAppProcessInfo.IMPORTANCE_SERVICE) {
					String[] pkgList = appProcessInfo.pkgList;
					for (int j = 0; j < pkgList.length; ++j) {
						if(!pkgList[j].equals("com.kanke.control.tv") || !pkgList[j].equals("com.kanke.control.plugin")){
							am.killBackgroundProcesses(pkgList[j]);
							count++;
						}
					}
				}
			}
		}
	}


	/**
	 * 通过包名打开应用
	 * @param app_packName
	 * @return
	 */
	public boolean openAppPackName(String app_packName) {
		if (app_packName != null) {
			Alog.i(TAG, "正在启动应用：" + app_packName);
			return startApp(app_packName);
		}
		return false;
	}

	/**
	 * 根据包名判断该设备是否已安装该应用
	 * @param app_packName
	 * @return true 已安装
	 */
	public boolean getInstallAppinfo(String app_packName){
		List<MyAppInfo> myAppInfos = queryAppInfo();
		for (MyAppInfo myAppInfo : myAppInfos) {
			if (myAppInfo.packName.equals(app_packName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据包名判断该设备是否已安装该应用(只判断自己的安装的包，不包含系统的)
	 * @param app_packName
	 * @return true 已安装
	 */
	public boolean getInstallAppFromMe(String app_packName){
		List<MyAppInfo> myAppInfos = getInstalledApps(false);
		for (MyAppInfo myAppInfo : myAppInfos) {
			if (myAppInfo.packName.equals(app_packName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否已经安装app(速度快)
	 * @param appPackageName app包名
	 * @return true 已安装
     */
	public boolean isAppInstalled(String appPackageName) {
		PackageManager pm = context.getPackageManager();
		boolean installed =false;
		try {
			pm.getPackageInfo(appPackageName,PackageManager.GET_ACTIVITIES);
			installed =true;
		} catch(NameNotFoundException e) {
			installed =false;
		}
		return installed;
	}

	/**
	 * 通过包名启动应用
	 * @param packName
	 * @return
	 */
	public boolean startApp(String packName) {
		boolean flag = false;
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					packName, 0);
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(pi.packageName);
			List<ResolveInfo> apps = context.getPackageManager()
					.queryIntentActivities(resolveIntent, 0);
			if(apps==null || apps.isEmpty()){
				Alog.i(TAG,"device not find "+packName);
				return false;
			}
			ResolveInfo ri = apps.iterator().next();
			if (ri != null) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ComponentName cn = new ComponentName(
						ri.activityInfo.packageName, ri.activityInfo.name);
				intent.setComponent(cn);
				context.startActivity(intent);
				flag = true;
			}
		} catch (NameNotFoundException e) {
			flag = false;
			Alog.i(TAG, "应用程序无法启动");
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 通过判断应用是否运行
	 * @param packName
	 * @return
	 */
	public  boolean isAppRunable(String packName) {
		boolean isRunable = false;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(100);
		for (RunningTaskInfo info : list) {
			if (info.topActivity.getPackageName().equals(packName) && info.baseActivity.getPackageName().equals(packName)) {
				isRunable = true;
				break;
			}
		}
		return isRunable;
	}

	/**
	 * 通过判断应用是否位于堆栈的顶层
	 * @param packName
	 * @return
	 */
	public boolean isRunningForeground (String packName)
	{
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if(!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(packName))
		{
			return true ;
		}
		return false ;
	}



	public  String printForegroundTask() {
		String currentApp = "NULL";
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
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
			List<RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
			currentApp = tasks.get(0).processName;
		}

		Log.e("adapter", "Current App in foreground is: " + currentApp);
		return currentApp;
	}


	/**
	 * 通过包名卸载应用
	 * @param app_packName
	 */
	public void uninstallApp(String app_packName) {
		if (app_packName != null) {
			uninstall(app_packName);
		}
	}

	public void uninstall(String packName) {
		String uristr = "package:" + packName;
		Uri uri = Uri.parse(uristr);
		Intent deleteIntent = new Intent();
		deleteIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		deleteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		deleteIntent.setAction(Intent.ACTION_DELETE);
		deleteIntent.setData(uri);
		context.startActivity(deleteIntent);
	}

	/**
	 * 安装应用
	 * @param path 需要安装的apk路劲
	 */
	public static void executeInstallView(Context context,String path){
		if(!TextUtils.isEmpty(path)){
			Intent installIntent = new Intent(Intent.ACTION_VIEW);
			installIntent.setAction(Intent.ACTION_VIEW);
			installIntent.setAction(Intent.ACTION_PACKAGE_ADDED);
			installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			installIntent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
			context.startActivity(installIntent);
		}
	}


	class MyAppInfo {
		public String versionName;
		public String appName;
		public String packName;
		public Drawable appIcon;
		public boolean isSystem;
		@Override
		public String toString() {
			return "MyAppInfo [versionName=" + versionName + ", appName=" + appName
					+ ", packName=" + packName + ", appIcon=" + appIcon
					+ ", isSystem=" + isSystem + "]";
		}
	}




}
