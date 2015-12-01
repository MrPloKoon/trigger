package com.lokimod.networktriggerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BootUpBroadcastReceiver extends BroadcastReceiver {
    public BootUpBroadcastReceiver() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d("BootUpBroadCast", "onReceive");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (!isApOn(context)) {
                configApState(context);
            }
            if (!isMobileDataOn(context)) {
                enableMobileData(context);
            }
        }
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    // check whether wifi hotspot on or off
    public static boolean isApOn (Context context) {
        Log.d("BootUpBroadCast", "isApOn");
        WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    // toggle wifi hotspot on or off
    public static  boolean configApState (Context context) {
        Log.d("BootUpBroadCast", "configApState");
        WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            // if WiFi is on, turn it off
            if (isApOn(context)) {
                wifimanager.setWifiEnabled(false);
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, !isApOn(context));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    // check whether mobile data transfer on or off
    //exception!!!!
    public static boolean isMobileDataOn (Context context) {
        Log.d("BootUpBroadCast", "isMobileDataOn");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.d("BootUpBroadCast", "connectivityManager");
        //connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        Log.d("BootUpBroadCast", String.valueOf(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting()));
//        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
    }

    // check whether mobile data transfer on or off
    public static boolean enableMobileData (Context context) {
        Log.d("BootUpBroadCast", "enableMobileData");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //final Runtime runtime = Runtime.getRuntime();
                try {

                        Process pr = Runtime.getRuntime().exec("su");
                        DataOutputStream os = new DataOutputStream(pr.getOutputStream());
                        os.writeBytes("svc data enable");

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, 3000); // start delay [milliseconds]

/*        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Method method = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
            method.setAccessible(true);
            method.invoke(connectivityManager, true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }*/
        return true;
    }

}
