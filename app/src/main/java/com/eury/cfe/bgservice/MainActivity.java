package com.eury.cfe.bgservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.eury.cfe.bgservice.data.Task;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Activity mActivity;

    private EditText et_pack_name;

    private boolean flag=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity=this;

        flag=UsageStatsUtils.isOpenUsageState(mActivity);

        et_pack_name=(EditText)this.findViewById(R.id.et_pack_name);
        et_pack_name.setText("cn.jj");

        findViewById(R.id.btn_start_run).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=UsageStatsUtils.isOpenUsageState(mActivity);
                if(flag){
                    final String packName=et_pack_name.getText().toString().trim();
                    if(TextUtils.isEmpty(packName)){
                        Toast.makeText(mActivity,"请输入包名",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean flag=AppManager.getInstance(mActivity).openAppPackName(packName);
                    if(!flag){
                        Toast.makeText(mActivity,"应用不存在",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<Task> tasks = new ArrayList<>();
                            tasks.add(new Task(packName,5));
                            Intent startintent = new Intent(MainActivity.this, BgService.class);
                            startintent.putExtra("tasks", tasks);
                            startService(startintent);
                        }
                    }, 3000L);

                }else{
                    Toast.makeText(mActivity, "还没有设置权限", Toast.LENGTH_SHORT).show();
                    UsageStatsUtils.startUsageStatsUI(mActivity);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BgService.stopService(mActivity);
        flag=UsageStatsUtils.isOpenUsageState(mActivity);
        if(!flag){
            Toast.makeText(mActivity, "还没有设置权限", Toast.LENGTH_SHORT).show();
        }else{
//            Toast.makeText(mActivity,"设置好呢～",Toast.LENGTH_SHORT).show();
        }
    }

}
