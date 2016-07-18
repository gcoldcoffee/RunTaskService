# RunTaskService
采用IntentService后台统计其他应用前台运行时长，同时监测了手机锁屏解锁状态，用于暂停以及重新开始计时。 IntentService是继承于Service并处理异步请求的一个类，在IntentService内有一个工作线程来处理耗时操作，启动IntentService的方式和启动传统Service一样，同时，当任务执行完后，IntentService会自动停止，而不需要我们去手动控制。


Android 5.0以前我们可以采用：

ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE); ComponentName cn = am.getRunningTasks(1).get(0).topActivity; String currentApp = cn.getPackageName();

5.0以后，使用UsageStatsManager获取，但是这种获取方法需要用户在手机上赋予APP权限才可以使用，就是在安全-高级-有权查看使用情况的应用 在这个模块中勾选上指定APP就可以获取到栈顶的应用名 同时添加权限，Demo里面有详细的代码：

5.0之前：android.permission.GET_TASKS

5.0之后：android.permission.PACKAGE_USAGE_STATS

部分手机例如小米，在自身系统添加了电池性能模块--神隐模式，4～3钟左右会将后台应用关闭网络,GPS功能，此时需要手动开启(http://www.chinaz.com/mobile/2015/0824/437808.shtml )