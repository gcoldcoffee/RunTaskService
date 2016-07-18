package com.eury.cfe.bgservice;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.eury.cfe.bgservice.data.Task;
import com.eury.cfe.bgservice.data.XToast;
import com.eury.cfe.bgservice.receiver.AlarmReceiver;
import com.eury.cfe.bgservice.receiver.LockScreenReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * 2016/5/9.
 */
public class RunTaskUtils {
    public static  String TAG=RunTaskUtils.class.getSimpleName();

    public Context mContext;

    //执行间隔时间
    private long DELAYTTIME=1000L;
    //首次开始执行时间
    private long STARTDELAYTTIME=3000L;

    /**
     * 分两种模式：Build.VERSION.SDK_INT < 21 使用AlarmManager 否则 ScheduledExecutorService
     * 启动统计线程
     */
    public ScheduledExecutorService scheduledThreadPool;

    /**
     * 统计起始时间
     */
    public long startTime=0;
    /**
     * 统计结束时间
     */
    private long endTime;
    /**
     * 是否锁屏(true：锁屏 / false：解锁)
     */
    public boolean isScreen=false;
    /**
     * 当前任务是否在运行(true：否 / false：是)
     */
    public boolean isRunTime=true;
    /**
     * 任务意外弹出(true：是 / false：否)
     */
    public boolean isAccidentRunTime=false;
    /**
     * 所有任务都已完成
     */
    public boolean taskRunFinish=false;
    /**
     * 需要统计计时的应用包名
     */
    public String packName=null;
    /**
     * 任务运行时间
     */
    public Integer runTimes=null;
    public List<Task> taskTimes=new ArrayList<>();
    public List<Task> baseTaskTimes=new ArrayList<>();

    private BgService.RunTaskImpl runTaskImpl;

    /**
     * 采用的是IntentService,所以此处继续使用Service中的Handler
     * 请查看代码具体获取方法
     */
    public ServiceHandler mHandler;
    private volatile Looper mServiceLooper;


    public static RunTaskUtils runTaskUtils;

    public static RunTaskUtils getInstance(){
        if(runTaskUtils==null){
            runTaskUtils=new RunTaskUtils();
        }
        return runTaskUtils;
    }

    public void setContext(Context context){
        this.mContext=context;
    }

    public void setTaskRunFinish(boolean taskRunFinish) {
        this.taskRunFinish = taskRunFinish;
    }

    public void setRunTaskImpl(BgService.RunTaskImpl runTaskImpl) {
        this.runTaskImpl = runTaskImpl;
    }

    public RunTaskUtils(){}

    /**
     * 初始化数据
     * @param intent
     */
    public void getIntentData(Intent intent){
        stopRunTimeThread(true);

        if(intent!=null){

            taskRunFinish=false;

            ArrayList<Task> taskRunTimes=(ArrayList<Task>)intent.getSerializableExtra("tasks");
            if(taskRunTimes==null || taskRunTimes.isEmpty()){
                BgService.stopService(mContext);
            }
            for (Task taskRuntime: taskRunTimes) {
                Alog.i(TAG,taskRuntime.toString());
                packName=taskRuntime.getPackName();
                runTimes=taskRuntime.getTaskTime();
            }

            if(!TextUtils.isEmpty(packName)){
                taskTimes.addAll(taskRunTimes);
                baseTaskTimes.addAll(taskRunTimes);
                if(taskTimes!=null && !taskTimes.isEmpty()){
                    startTaskThread();
                    //注册锁屏回调
                    LockScreenReceiver.getInstance().addOnLockkSceenLisstener(new MyLockScreenListener());
                    PreferenceUtil.setBSharedPreferences(mContext, LockScreenReceiver.ACTION_FINISH, true);
                }else{
                    BgService.stopService(mContext);
                }
            }else{
                Alog.i(TAG, "packName or runTimes,taskID is null,does not start monitor RunTaskThread!");
                BgService.stopService(mContext);
            }
        }
    }


    /**
     * 意外情况弹出，重新初始化计时数据
     */
    private void initRunTimesData(){
        if(taskTimes!=null && !taskTimes.isEmpty()){
            taskTimes.clear();
        }
        if(!taskTimes.containsAll(baseTaskTimes)) {
            taskTimes.addAll(baseTaskTimes);
            Alog.i(TAG,"意外情况弹出，重新初始化计时数据"+taskTimes.size());
        }
    }

    /**
     * 接收统计线程反馈消息
     */
    public void sendMessage(boolean flag){
        sumTime(flag);
    }


    public void sumTime(boolean flag){
        if(flag && isAccidentRunTime){
            startTime=System.currentTimeMillis();
            Alog.i(TAG, "任务意外情况弹出,现重新进入,初始化开始时间");

            initRunTimesData();

            isAccidentRunTime=false;
            isRunTime=true;
        }
        if(startTime<=0){
            Alog.i(TAG,"startTime is :"+startTime);
            return;
        }
        if(flag){//任务为运行状态
            endTime=System.currentTimeMillis();
            long time=endTime-startTime;
            mHandler.removeMessages(2);
            isRunTime=true;
            keepRunTime(flag,time);
        }else{
            endTime=System.currentTimeMillis();
            long time=endTime-startTime;
            keepRunTime(flag, time);
        }
    }


    public void startTaskThread(){
        if(!TextUtils.isEmpty(packName)){

            //由于使用的是IntentService,自动Handler 线程，所以直接拿出来用既可以
            HandlerThread thread = new HandlerThread("IntentService[" + "EuryIntentService" + "]");
            thread.start();
            mServiceLooper=thread.getLooper();
            mHandler=new ServiceHandler(mServiceLooper);

            //初始化开始记录时间
            mHandler.removeMessages(6);
            mHandler.sendEmptyMessageDelayed(6,STARTDELAYTTIME);

            if(Build.VERSION.SDK_INT < 21) {
                SystemAPI.startBroadcast(mContext, STARTDELAYTTIME, DELAYTTIME, AlarmReceiver.class, AlarmReceiver.ACTION);
            }else{
                if ((scheduledThreadPool != null) && (scheduledThreadPool.isShutdown()))
                    scheduledThreadPool = null;
                if(scheduledThreadPool==null){
                    scheduledThreadPool = Executors.newScheduledThreadPool(1);
                    scheduledThreadPool.scheduleAtFixedRate(new ExecoutServer(), STARTDELAYTTIME, DELAYTTIME, TimeUnit.MILLISECONDS);
                    Alog.i(TAG, "START ScheduledExecutorService Broadcast");
                }
            }
        }
    }

    class ExecoutServer implements Runnable{
        @Override
        public void run() {
            try {
                UsageStatsUtils.printForegroundTask(mContext, packName, new BgService.RunTaskTopImpl() {
                    @Override
                    public void runTaskTop(boolean flag, String packName) {
                        if(packName!=null && !"".equals(packName)){
                            //如果最前台任务为本应用，停止统计线程不消耗多余资源，用户手动重新打开任务
                            if(packName.equals(mContext.getPackageName())){
                                stopTopFinish();
                                return;
                            }
                        }
                        if(!taskRunFinish){
                            sendMessage(flag);
                        }
                        if(taskRunFinish){
                            stopRunTimeThread(taskRunFinish);
                        }
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void keepRunTime(boolean isRun,long times){
        if(taskRunFinish){
            stopRunTimeThread(taskRunFinish);
            return;
        }
        //计时时间
        long s_time=times / 1000;//秒
        long m_time = s_time / 60;//分
        Alog.i(TAG, packName + " isRun:"+isRun+" Runtime:" + s_time + "s "+ m_time + "m");

        if(taskTimes!=null && !taskTimes.isEmpty()){
            for (int i=0;i<taskTimes.size();i++){
                Task task=taskTimes.get(i);
                //任务时间
                Integer taskTime=task.getTaskTime();
                Alog.i(TAG,"任务要求时间taskTime:"+taskTime);
                //条件审核成功(任务应用前台运行时间与任务审核条件时间对等)
                if (((isRun && m_time >= taskTime ) && !isScreen) || ((!isRun && m_time >= taskTime ) && !isScreen)) {
                    isRunTime=true;
                    removeHandler();
                    Message msg=new Message();
                    msg.what=1;
                    msg.obj=task;
                    mHandler.sendMessageDelayed(msg,500L);
                    return;
                }
                //条件审核超时(应用处于后台运行，但是任务后台应用运行统计时间与任务审核条件时间对等),此处判断不完善可能数据为[1,5,1]
//                int j=taskTimes.size()-1;
//                if(j<=taskTimes.size()){
//                    Integer lastTaskTime=taskTimes.get(j).getTaskTime();
//                    Alog.i(TAG,"任务要求时间lastTaskTime:"+lastTaskTime);
//                    if(!isRun && m_time >=lastTaskTime && !isScreen){
//                        removeHandler();
//                        mHandler.sendEmptyMessageDelayed(3, 500L);
//                    }
//                    return;
//                }
                /**
                 * 任务进行过程中，由于意外情况弹出（如：1，新短信通知点击查看之后又再次回到任务应用继续进行任务,重新计时统计 2，意外锁屏）
                 *
                 * 此处判定后台运行会有2秒延迟（意在解决部分手机判断存在误差问题）,3秒内如果转换为前台运行，会取消此处判定-->mHandler.removeMessages(2)，否则判定为后台运行了
                 */
                if (!isRun && m_time < taskTime && !isScreen) {
                    if(!isRunTime){
                        return;
                    }
                    removeHandler();
                    mHandler.sendEmptyMessageDelayed(2, 5000L);
                    isRunTime=false;
                    return;
                }
            }
        }
    }


    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    final Task task=(Task)msg.obj;
                    Alog.i(TAG, "任务审核条件已达成" + task.getTaskTime());

                    taskTimes.remove(task);

                    if(taskTimes!=null && taskTimes.size()<= 0) {
                        Alog.i(TAG, "所有任务审核条件已达成，总好耗时");
                        postToastInfo("所有任务审核条件已达成",0);
                        stopTopFinish();
                    }
                    break;
                case 2:
                    Alog.i(TAG, "任务意外情况弹出");
                    postToastInfo("任务意外情况弹出", 0);
                    startTime=0;
                    isAccidentRunTime=true;
                    break;
                case 3:
                    Alog.i(TAG, "任务审核条件超时");
                    stopTopFinish();
                    break;
                case 4://锁屏状态停止监测线程
                    stopRunTimeThread(false);
                    break;
                case 5://解锁重启监测线程
                    if(taskRunFinish){
                        Alog.i(TAG, "所有任务都已经完成");
                        return;
                    }
                    initRunTimesData();
                    startTaskThread();
                    break;
                case 6:
                    //初始化开始记录时间
                    startTime = System.currentTimeMillis();
                    break;
            }
        }
    }

    private void removeHandler(){
        if(mHandler!=null){
            mHandler.removeMessages(1);
            mHandler.removeMessages(2);
            mHandler.removeMessages(3);
            mHandler.removeMessages(4);
            mHandler.removeMessages(5);
        }
    }

    public void postToastInfo(final String str,long delayMillis){
        if(taskRunFinish){
            return;
        }
        if(mServiceLooper!=null){
            final ViewHandler viewHandler=new ViewHandler(mServiceLooper);
            viewHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    XToast.makeText(mContext, str, XToast.LENGTH_LONG).show(viewHandler);
                }
            }, delayMillis);
        }
    }
    public final class ViewHandler extends Handler {
        public ViewHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
        }
    }

    /**
     * 停止统计计时线程
     */
    private void clearData(){
        startTime=0;
        endTime=0;
        packName=null;
        if(taskTimes!=null && !taskTimes.isEmpty()){
            taskTimes.clear();
        }
        if(baseTaskTimes!=null && !baseTaskTimes.isEmpty()){
            baseTaskTimes.clear();
        }
    }

    public void stopRunTimeThread(boolean taskRunFinish){
        if(taskRunFinish){
            clearData();
            PreferenceUtil.setBSharedPreferences(mContext, LockScreenReceiver.ACTION_FINISH, false);
        }
        isRunTime=true;
        isAccidentRunTime=false;
        if(scheduledThreadPool!=null){
            if(!scheduledThreadPool.isShutdown()){
                scheduledThreadPool.shutdown();
                scheduledThreadPool=null;
            }
        }
        SystemAPI.stopBroadcast(mContext, AlarmReceiver.class, AlarmReceiver.ACTION);
        removeHandler();
    }

    private void stopTopFinish(){
        taskRunFinish=true;
        stopRunTimeThread(taskRunFinish);
        if(runTaskImpl!=null){
            runTaskImpl.TaskRunFinish();
        }
    }

    /**
     * 行为锁屏监听相应处理
     */
    public class MyLockScreenListener implements BgService.OnLockSceenListener {
        @Override
        public void onScreenOn() {
            isScreen=true;
            startTime=0;
        }
        @Override
        public void onScreenOff() {
            isScreen=true;
            startTime=0;
            if(mHandler!=null){
                Alog.i(TAG,"锁屏停止统计线程");
                mHandler.sendEmptyMessage(4);
            }
        }
        @Override
        public void onUserPresent() {
            isScreen=false;
            if (mHandler!=null) {
                boolean flag= UsageStatsUtils.printForegroundTask(mContext, mContext.getPackageName());
                if(flag){//如果最前台任务为本应用，停止统计线程不消耗多余资源，用户手动重新打开任务
                    Alog.i(TAG,mContext.getPackageName()+" is TopActivity");
                    stopTopFinish();
                }else{
                    Alog.i(TAG,"解锁重新开始统计线程");
                    mHandler.sendEmptyMessage(5);
                }
            }
        }
    }



}
