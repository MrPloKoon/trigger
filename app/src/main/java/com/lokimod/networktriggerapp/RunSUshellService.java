package com.lokimod.networktriggerapp;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RunSUshellService extends Service {

    private SharedPreferences mSettings;
    Context ctx;

    public static final String INTENT_EXTRA_SHELL_COMMAND_DETAILS = "shell_command_details";

    public RunSUshellService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSettings = getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        // Delayed start0
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
               //final Runtime runtime = Runtime.getRuntime();
                //final Runtime runtime = Runtime.getRuntime();
                String shellCmd = intent.getStringExtra(INTENT_EXTRA_SHELL_COMMAND_DETAILS);
                try {
                    Log.d("RunSUshellService", "onStartCommand");
                    if (shellCmd.equals("tun0_route")) {
                        Log.d("RunSUshellService", "tun0 UP");
                        String strTUNsubnetwork = mSettings.getString(MainActivity.APP_PREFERENCES_TUN_SUBNETWORK, "000.0.0.0/16");
                        //runtime.exec("su");
                        //runtime.exec("su -c ip route add " + strTUNsubnetwork + " dev tun0");
                        Process pr = Runtime.getRuntime().exec("su");
                        DataOutputStream os = new DataOutputStream(pr.getOutputStream());
                        os.writeBytes("ip route add " + strTUNsubnetwork + " dev tun0");
                    } else if (shellCmd.equals("wlan0_addr")) {
                        Log.d("RunSUshellService", "wlan0 UP");
                        String strWLANsubnetwork = mSettings.getString(MainActivity.APP_PREFERENCES_WLAN_SUBNETWORK, "000.0.000.0/24");
                        //runtime.exec("su -c ip addr add " + strWLANsubnetwork + " dev wlan0");
                        Process pr = Runtime.getRuntime().exec("su");
                        DataOutputStream os = new DataOutputStream(pr.getOutputStream());
                        os.writeBytes("ip address add " + strWLANsubnetwork + " dev wlan0");
                }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, 2000); // start delay [milliseconds]

        stopSelf();
        return START_NOT_STICKY;
    }

    public static boolean isServiceRunning(Context c) {
        ActivityManager manager = (ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RunSUshellService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
