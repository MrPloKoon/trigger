package com.lokimod.networktriggerapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStateBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION ="android.net.conn.CONNECTIVITY_CHANGE";

    public NetworkStateBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d("NetworkStateBroadcast", "onReceive");
        String action = intent.getAction();
        if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            Log.d("NetworkStateBroadcast", "ConnectivityManager.CONNECTIVITY_ACTION");
            ConnectivityManager conMan = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobileNetInfo = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if ( mobileNetInfo.isAvailable() && mobileNetInfo.isConnected() ) {
                Log.d("NetworkStateBroadcast", "mobileNetInfo.isAvailable() && mobileNetInfo.isConnected()");
                SharedPreferences mSettings = context.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
                PackageManager pm = context.getPackageManager();
                ComponentName componentName = new ComponentName(context, AlarmBroadcastReceiver.class);
                pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(MainActivity.APP_PREFERENCES_ALARM_STATE, "ON");
                editor.apply();
                AlarmBroadcastReceiver.SetAlarm(context); // Mobile Network is ON... Waiting for VPN connection ()
            } else {
                Log.d("NetworkStateBroadcast", "ELSE_mobileNetInfo.isAvailable() && mobileNetInfo.isConnected()");
                SharedPreferences mSettings = context.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
                String alarm_state = mSettings.getString(MainActivity.APP_PREFERENCES_ALARM_STATE, "OFF");
                if (alarm_state.equals("ON")) {
                    Log.d("NetworkStateBroadcast", "alarm_state.equals(ON)");
                    AlarmBroadcastReceiver.CancelAlarm(context); // Mobile Network is OFF... VPN connection OFF
                    PackageManager pm = context.getPackageManager();
                    ComponentName componentName = new ComponentName(context, AlarmBroadcastReceiver.class);
                    pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(MainActivity.APP_PREFERENCES_ALARM_STATE, "OFF");
                    editor.apply();
                }
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(MainActivity.APP_PREFERENCES_VPN_STATE, "OFF");
                editor.apply();
            }
        }
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
