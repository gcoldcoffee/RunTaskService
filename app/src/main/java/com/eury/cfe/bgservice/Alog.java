package com.eury.cfe.bgservice;

import android.util.Log;

/**
 * Created by aoe on 2016/2/18.
 */
public class Alog {

    public static final boolean isDebug=true;

    public static void i(String tag,String ... strs){
        if(isDebug){
            StringBuilder sb = new StringBuilder();
            for(String str: strs){
                sb.append(str);
            }
            Log.i(tag, sb.toString());
        }
    }

    public static void v(String tag,String ... strs){
        if(isDebug){
            StringBuilder sb = new StringBuilder();
            for(String str: strs){
                sb.append(str);
            }
            Log.v(tag, sb.toString());
        }
    }

    public static void d(String tag,String ... strs){
        if(isDebug){
            StringBuilder sb = new StringBuilder();
            for(String str: strs){
                sb.append(str);
            }
            Log.d(tag, sb.toString());
        }
    }

    public static void w(String tag,String ... strs){
        if(isDebug){
            StringBuilder sb = new StringBuilder();
            for(String str: strs){
                sb.append(str);
            }
            Log.w(tag, sb.toString());
        }
    }

    public static void e(String tag,String ... strs){
        if(isDebug){
            StringBuilder sb = new StringBuilder();
            for(String str: strs){
                sb.append(str);
            }
            Log.e(tag, sb.toString());
        }
    }


}
