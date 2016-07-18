package com.eury.cfe.bgservice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eury.cfe.bgservice.Alog;
import com.eury.cfe.bgservice.BgService;
import com.eury.cfe.bgservice.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aoe on 2016/3/2.
 */
public class LockScreenReceiver extends BroadcastReceiver{

    private String TAG=getClass().getSimpleName();

    public static String ACTION_FINISH="LOCKRECEIVERFINISH";

    private String action=null;

    private Context mContext;

    public static LockScreenReceiver lockScreenReceiver;

    public static LockScreenReceiver getInstance(){
        if(lockScreenReceiver==null){
            lockScreenReceiver=new LockScreenReceiver();
        }
        return lockScreenReceiver;
    }

    public LockScreenReceiver(){}

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext=context;
        action = intent.getAction();
        boolean flag= PreferenceUtil.getBSharedPreferences(context, ACTION_FINISH);
        Alog.i(TAG, "ACTION_FINISH+++" + flag);
        if(flag){
            if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
                Alog.i(TAG,"开屏");
                if(onLockSceenListeners==null || onLockSceenListeners.size()<=0){
                    Alog.i(TAG,"onLockSceenListeners is null");
                }else{
                    synchronized (onLockSceenListeners){
                        for (BgService.OnLockSceenListener onLockSceenListener : onLockSceenListeners) {
                            onLockSceenListener.onScreenOn();
                        }
                    }
                }

            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
                Alog.i(TAG,"锁屏");
                if(onLockSceenListeners==null || onLockSceenListeners.size()<=0){
                    Alog.i(TAG,"onLockSceenListeners is null");
                }else{
                    synchronized (onLockSceenListeners){
                        for (BgService.OnLockSceenListener onLockSceenListener : onLockSceenListeners) {
                            onLockSceenListener.onScreenOff();
                        }
                    }
                }
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁
                Alog.i(TAG,"解锁");
                if(onLockSceenListeners==null || onLockSceenListeners.size()<=0){
                    Alog.i(TAG,"onLockSceenListeners is null");
                }else{
                    synchronized (onLockSceenListeners){
                        for (BgService.OnLockSceenListener onLockSceenListener : onLockSceenListeners) {
                            onLockSceenListener.onUserPresent();
                        }
                    }
                }
            }
        }
    }


    public List<BgService.OnLockSceenListener> onLockSceenListeners=new ArrayList<>();

    public void addOnLockkSceenLisstener(BgService.OnLockSceenListener onLockSceenListener){
        if(!onLockSceenListeners.contains(onLockSceenListener)){
            onLockSceenListeners.add(onLockSceenListener);
        }
    }

    public void clear(){
        if(onLockSceenListeners!=null && !onLockSceenListeners.isEmpty()){
            onLockSceenListeners.clear();
        }
    }

}
