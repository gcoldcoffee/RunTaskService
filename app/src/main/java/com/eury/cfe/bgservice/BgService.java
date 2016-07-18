package com.eury.cfe.bgservice;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.eury.cfe.bgservice.receiver.AlarmReceiver;
import com.eury.cfe.bgservice.receiver.LockScreenReceiver;

/**
 * Created by aoe on 2016/6/1.
 */
public class BgService extends IntentService{

    private final static String TAG=BgService.class.getSimpleName();

    private Context mContext;

    public interface RunTaskImpl{
        public void TaskRunFinish();//所有任务都已经完成
    }

    public interface RunTaskTopImpl{
        public void runTaskTop(boolean flag,String packName);
    }

    /**
     * 监测手机锁屏
     */
    public interface OnLockSceenListener{
        public void onScreenOn();//开屏

        public void onScreenOff();//锁屏

        public void onUserPresent();//解锁
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public BgService() {
        super("EuryIntentService");
    }

    private LockScreenReceiver lockScreenReceiver;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext=this;
        Alog.i(TAG, "onCreate()");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Alog.i(TAG,"onHandleIntent()");
        RunTaskUtils.getInstance().setRunTaskImpl(new MyRunTaskImpl());
        RunTaskUtils.getInstance().setContext(mContext);
        RunTaskUtils.getInstance().getIntentData(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Alog.i(TAG, "onDestroy()");
        Intent intent = new Intent(mContext,AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_LOCK);
        mContext.sendBroadcast(intent);
    }

    /**
     * 关闭监测任务应用运行状态
     * @param context
     */
    public static void stopService(final Context context){
        Alog.i(TAG, "++stopService++");

        RunTaskUtils.getInstance().setContext(context);
        RunTaskUtils.getInstance().setTaskRunFinish(true);
        RunTaskUtils.getInstance().stopRunTimeThread(true);

    }

    /**
     * 取消注册监测锁屏广播
     */
    public void unregisterLockScreenListener() {
        if(LockScreenReceiver.getInstance()!=null){
            unregisterReceiver(LockScreenReceiver.getInstance());
        }
    }

    public class MyRunTaskImpl implements RunTaskImpl{
        @Override
        public void TaskRunFinish() {
            unregisterLockScreenListener();
        }
    }
}
