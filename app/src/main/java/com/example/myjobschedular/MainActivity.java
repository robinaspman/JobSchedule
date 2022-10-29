package com.example.myjobschedular;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.myjobschedular.services.MyService;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    public static final int JOB_ID = 101;
    public static final String JOB = "Job";
    public static final String TAG = "JOB";
    private Button startJob;
    private Button cancel;
    private TextView textView;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                try {
                    JSONObject res = new JSONObject(bundle.getString(MyService.VAL));
                    textView.setText(res.getString("title"));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            Bundle bundle = intent.getExtras();
//            if (bundle != null) {
//                int val = bundle.getInt(MyService.VAL);
//                textView.setText(String.valueOf(val));
//                Log.i(TAG, "onReceive: " + val);
//            }

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter(MyService.MACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startJob = findViewById(R.id.button);
        cancel = findViewById(R.id.cancel_button);
        textView = findViewById(R.id.textView);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                jobScheduler.cancel(JOB_ID);
                Log.i(JOB, "onClick: Job Canceled");

            }
        });
        startJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

                JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(getApplicationContext(),
                        MyService.class))
                        //.setMinimumLatency(0)
                        .setRequiresCharging(true)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                        .setPersisted(true)
                        .setPeriodic(15 * 60 * 1000)
                        .build();
                jobScheduler.schedule(jobInfo);

            }
        });
    }
}