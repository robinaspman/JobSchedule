package com.example.myjobschedular.services;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.IBinder;
import android.os.Trace;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class MyService extends JobService {
    private static final String TAG = "JOB";
    public static final String MACTION = "com.example.myapp";
    public static final String VAL = "val";
    private boolean jobCancelled = false;
    public static final String STRING_URL = "https://jsonplaceholder.typicode.com/todos/1";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob: " + params.getJobId());
        //jobFinished(params, false);
        //idoBackgroundWork(params);
        downloadJson(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob: " + params.getJobId());
        jobCancelled = true;
        return true;
    }

    private void downloadJson(JobParameters parameters) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Intent jobIntent = new Intent(MACTION);
                try {
                    URL url = new URL(STRING_URL);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    if (responseCode != 200) throw new RuntimeException("HttpResponseCode: "
                            + responseCode);
                    else {
                        Scanner scanner = new Scanner(url.openStream());
                        StringBuilder builder = new StringBuilder();
                        while (scanner.hasNext()) {
                            if (jobCancelled) return;
                            builder.append(scanner.nextLine());
                        }
                        Log.i(TAG, "Downloaded Text: " + builder.toString());

                        JSONObject jsonObject = new JSONObject(String.valueOf(builder));
                        Log.i(TAG, "JSON: " + jsonObject.getString("title"));

                        scanner.close();

                        jobIntent.putExtra(VAL, jsonObject.toString());
                        sendBroadcast(jobIntent);

                        jobFinished(parameters, false);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }


            }
        };
        Thread thread = new Thread(r);
        thread.start();
    }
    private void doBackgroundWork(JobParameters parameters) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent jobIntent = new Intent(MACTION);
                for (int i = 0; i < 10; i++) {
                    Log.i(TAG, "run: " + i);
                    if (jobCancelled) {
                        return;
                    }
                    jobIntent.putExtra(VAL, i);
                    sendBroadcast(jobIntent);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "run: Job done!");
                // Tell the system that I am finished!
                jobFinished(parameters, false);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}