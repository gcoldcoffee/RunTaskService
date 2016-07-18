package com.eury.cfe.bgservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceUtil {
    private static PreferenceUtil mInstance;


    private Context mContext;
    private SharedPreferences mPref;

    private PreferenceUtil() {
    }

    public synchronized static PreferenceUtil getInstance() {
        if (null == mInstance) {
            mInstance = new PreferenceUtil();
        }
        return mInstance;
    }

    public void init(Context context) {
        if (mContext == null) {
            mContext = context;
        }
        if (mPref == null) {
            mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
    }


    public String getString(String key) {
        return mPref.getString(key, "");
    }

    public String getString(String key, String def) {
        return mPref.getString(key, def);
    }

    public long getLong(String key) {
        return mPref.getLong(key, 0);
    }

    public long getLong(String key, int defInt) {
        return mPref.getLong(key, defInt);
    }

    public boolean contains(String key) {
        return mPref.contains(key);
    }

    public boolean getBoolean(String key){
        return mPref.getBoolean(key, false);
    }

    public boolean getBooleanDefaultTrue(String key){
        return mPref.getBoolean(key, true);
    }

    public void putBoolean(String key, boolean value){
        Editor editor = mPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void remove(String key) {
        Editor editor = mPref.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void setBSharedPreferences(Context context, String attr, boolean value){
        SharedPreferences preference = context.getSharedPreferences("RayArrayingSharedPreferences",Context.MODE_PRIVATE);
        Editor edit = preference.edit();
        edit.putBoolean(attr, value);
        edit.commit();
    }

    public static boolean getBSharedPreferences(Context context, String attr) {
        boolean result = false;
        if (context != null) {
            SharedPreferences preference = context.getSharedPreferences("RayArrayingSharedPreferences",Context.MODE_PRIVATE);
            if (preference != null) {
                result = preference.getBoolean(attr, false);
            }
        }
        return result;
    }

    public class SplashType{
        public static final String LOCAL_LOGIN_KEY="LOCAL_LOGIN_SPLASH";
    }

}
