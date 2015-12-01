package com.lokimod.networktriggerapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.net.SocketException;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    // tun0 interface activity checking interval [seconds]
    public static int checkInterval = 60;

    public AlarmBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        Log.d("AlarmBroadcastReceiver", "onReceive");

        /** tun0 interface activity checking **/
        boolean tun0isUP = false;
        NetworkInterface intf = null;
        try {
            intf = NetworkInterface.getByName("tun0");
            if (intf!=null)
                tun0isUP = intf.isUp();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.d("AlarmBroadcastReceiver",String.valueOf(tun0isUP));
        SharedPreferences mSettings = context.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
        String vpn_state = mSettings.getString(MainActivity.APP_PREFERENCES_VPN_STATE, "OFF");
        boolean b = vpn_state.equals("OFF");
        if (tun0isUP && vpn_state.equals("OFF")) {
            Log.d("AlarmBroadcastReceiver", "tun0isUP && vpn_state.equals(OFF)");
            Intent intentShell = new Intent(context, RunSUshellService.class);
            intentShell.putExtra(RunSUshellService.INTENT_EXTRA_SHELL_COMMAND_DETAILS, "tun0_route");
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(MainActivity.APP_PREFERENCES_VPN_STATE, "ON");
            editor.apply();
            context.startService(intentShell);
        } else if (!tun0isUP && vpn_state.equals("ON")) {
            Log.d("AlarmBroadcastReceiver", "!tun0isUP && vpn_state.equals(ON)");
            mSettings = context.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(MainActivity.APP_PREFERENCES_VPN_STATE, "OFF");
            editor.apply();
        }
        /** tun0 interface activity checking **/

        wl.release();
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void SetAlarm(Context context) {
        Log.d("AlarmBroadcastReceiver", "SetAlarm");
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*checkInterval, pi);
    }

    public static void CancelAlarm(Context context) {
        Log.d("AlarmBroadcastReceiver", "CancelAlarm");
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent sendPI = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sendPI);
    }

}
