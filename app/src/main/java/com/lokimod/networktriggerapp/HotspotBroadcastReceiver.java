package com.lokimod.networktriggerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HotspotBroadcastReceiver extends BroadcastReceiver {

    public HotspotBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        if("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
            int state = intent.getIntExtra("wifi_state", 0);
            // AP is on  12->13
            // AP is off 10->11
            if (state == 13) {
                Intent intentShell = new Intent(context, RunSUshellService.class);
                intentShell.putExtra(RunSUshellService.INTENT_EXTRA_SHELL_COMMAND_DETAILS, "wlan0_addr");
                context.startService(intentShell);
            }
        }
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
