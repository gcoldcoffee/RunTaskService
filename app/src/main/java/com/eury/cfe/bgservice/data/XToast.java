package com.eury.cfe.bgservice.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.eury.cfe.bgservice.R;
import com.eury.cfe.bgservice.RunTaskUtils;

/**
 */
public class XToast {
    private Context mContext;
    private WindowManager wm;
    private int mDuration;
    private View mNextView;
    public static final int LENGTH_SHORT = 1500;
    public static final int LENGTH_LONG = 3000;

    public XToast(Context context) {
        mContext = context.getApplicationContext();
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    public static XToast makeText(Context context, CharSequence text,
                                  int duration) {
        XToast result = new XToast(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        ll.setBackgroundResource(R.drawable.bg_toast);
        TextView tv = new TextView(context);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(dip2px(context, 15), dip2px(context, 10), dip2px(context, 15), dip2px(context, 10));
        tv.setLayoutParams(params );
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#ffffff"));
        ll.addView(tv);

        result.mNextView = (View)ll;
        result.mDuration = duration;
        return result;
    }

    public static XToast makeText(Context context, int resId, int duration)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId),
                duration);
    }

    public void show(RunTaskUtils.ViewHandler handler) {
        if (mNextView != null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = R.style.Animation_Toast;
            params.y = dip2px(mContext, 64);
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            wm.addView(mNextView, params);
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (mNextView != null) {
                        wm.removeView(mNextView);
                        mNextView = null;
                        wm = null;
                    }
                }
            }, mDuration);
        }
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
