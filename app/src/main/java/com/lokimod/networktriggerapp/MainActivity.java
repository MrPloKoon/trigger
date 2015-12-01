package com.lokimod.networktriggerapp;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MainActivity extends AppCompatActivity {

    private EditText editTextSetWlanSubnetwork;
    private EditText editTextSetTunSubnetwork;
    private SharedPreferences mSettings;

    public static final String APP_PREFERENCES = "app_settings";
    public static final String APP_PREFERENCES_WLAN_SUBNETWORK = "WLAN_subnetwork";
    public static final String APP_PREFERENCES_TUN_SUBNETWORK = "TUN_subnetwork";
    public static final String APP_PREFERENCES_VPN_STATE = "VPN_state";
    public static final String APP_PREFERENCES_ALARM_STATE = "ALARM_state";
    public static final String APP_PREFERENCES_ENABLE_WIFI_AP_MOBILE_DATA_ON_BOOT = "ENABLE_on_boot";
    private Context cntxt;
    private static final String subnetworkPattern =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
             "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
             "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
             "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\/" +
             "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static boolean validateSubnetwork (final String subnetwork) {
        Pattern pattern = Pattern.compile(subnetworkPattern);
        Matcher matcher = pattern.matcher(subnetwork);
        return matcher.matches();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.cntxt = getApplicationContext();

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        Button btnObtainRoot = (Button)findViewById(R.id.btnObtainRoot);
        Button btnSetWlanSubnetwork = (Button)findViewById(R.id.btnSetWlanSubnetwork);
        Button btnSetTunSubnetwork = (Button)findViewById(R.id.btnSetTunSubnetwork);
        CheckBox checkBoxEnableWifiApMobileDataOnBoot = (CheckBox)findViewById(R.id.checkBoxEnableWifiApMobileDataOnBoot);
        editTextSetWlanSubnetwork = (EditText)findViewById(R.id.editTextSetWlanSubnetwork);
        editTextSetTunSubnetwork = (EditText)findViewById(R.id.editTextSetTunSubnetwork);

        // checkBox is checked by default, and BootUpBroadcastReceiver is registered in manifest
        String WifiApMobileDataOnBootStatus = mSettings.getString(APP_PREFERENCES_ENABLE_WIFI_AP_MOBILE_DATA_ON_BOOT, "ON");
        if (WifiApMobileDataOnBootStatus.equals("ON")) {
            checkBoxEnableWifiApMobileDataOnBoot.setChecked(true);
        } else if (WifiApMobileDataOnBootStatus.equals("OFF")) {
            checkBoxEnableWifiApMobileDataOnBoot.setChecked(false);
        }
        // Set default value for tun0 subnetwork
        if (mSettings.getString(APP_PREFERENCES_TUN_SUBNETWORK, "000.0.0.0/16").equals("000.0.0.0/16")) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(APP_PREFERENCES_TUN_SUBNETWORK, "172.5.0.0/16");
            editor.apply();
        };

        checkBoxEnableWifiApMobileDataOnBoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                if ( checkBox.isChecked()) {
                    PackageManager pm = cntxt.getPackageManager();
                    ComponentName componentName = new ComponentName(cntxt, BootUpBroadcastReceiver.class);
                    pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(APP_PREFERENCES_ENABLE_WIFI_AP_MOBILE_DATA_ON_BOOT, "ON");
                    editor.apply();
                    Toast.makeText(cntxt, "on boot autostart is enabled (+)", Toast.LENGTH_SHORT).show();
                } else {
                    PackageManager pm = cntxt.getPackageManager();
                    ComponentName componentName = new ComponentName(cntxt, BootUpBroadcastReceiver.class);
                    pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(APP_PREFERENCES_ENABLE_WIFI_AP_MOBILE_DATA_ON_BOOT, "OFF");
                    editor.apply();
                    Toast.makeText(cntxt, "on boot autostart is disabled (-)", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnObtainRoot.setOnClickListener(new OnClickListener(){
            public void onClick(View w){
                final Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("su");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        btnSetWlanSubnetwork.setOnClickListener(new OnClickListener(){
            public void onClick(View w){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String currentAPsubnetwork = mSettings.getString(APP_PREFERENCES_WLAN_SUBNETWORK, "000.0.000.0/24");
                String newAPsubnetwork = editTextSetWlanSubnetwork.getText().toString();
                boolean isNetworkAddrValid = validateSubnetwork(newAPsubnetwork);
                if (isNetworkAddrValid) {
                    final String newAPsubnetworkCorrect = newAPsubnetwork;
                    builder.setTitle("Change wlan0 subnetwork?")
                            .setMessage("Do you really want to change\nthe current subnetwork:\n" + currentAPsubnetwork +
                                                                     "\nto the new subnetwork:\n" + newAPsubnetwork)
                            .setCancelable(false)
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = mSettings.edit();
                                    editor.putString(APP_PREFERENCES_WLAN_SUBNETWORK, newAPsubnetworkCorrect);
                                    editor.apply();
                                    Toast.makeText(cntxt, "wlan0 subnetwork is saved", Toast.LENGTH_SHORT).show();
                                    editTextSetWlanSubnetwork.setText("");
                                    editTextSetWlanSubnetwork.setHint(mSettings.getString(APP_PREFERENCES_WLAN_SUBNETWORK, "000.0.000.0/24"));
                                }
                            })
                            .setNeutralButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();
                } else {
                    String titleString = editTextSetWlanSubnetwork.getText().toString().isEmpty() ? "Input is empty!" : "Address is invalid!";
                    builder.setTitle(titleString)
                            .setMessage("The address should match CIDR notation:\n192.168.1.0/24 (pattern for example)")
                            .setCancelable(false)
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();
                }
            }
        });

        btnSetTunSubnetwork.setOnClickListener(new OnClickListener(){
            public void onClick(View w){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String currentTUNsubnetwork = mSettings.getString(APP_PREFERENCES_TUN_SUBNETWORK, "000.0.0.0/16");
                String newTUNsubnetwork = editTextSetTunSubnetwork.getText().toString();
                boolean isNetworkAddrValid = validateSubnetwork(newTUNsubnetwork);
                if (isNetworkAddrValid) {
                    final String newAPsubnetworkCorrect = newTUNsubnetwork;
                    builder.setTitle("Change tun0 subnetwork?")
                            .setMessage("Do you really want to change\nthe current subnetwork:\n" + currentTUNsubnetwork +
                                    "\nto the new subnetwork:\n" + newTUNsubnetwork)
                            .setCancelable(false)
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = mSettings.edit();
                                    editor.putString(APP_PREFERENCES_TUN_SUBNETWORK, newAPsubnetworkCorrect);
                                    editor.apply();
                                    Toast.makeText(cntxt, "tun0 subnetwork is saved", Toast.LENGTH_SHORT).show();
                                    editTextSetTunSubnetwork.setText("");
                                    editTextSetTunSubnetwork.setHint(mSettings.getString(APP_PREFERENCES_TUN_SUBNETWORK, "000.0.0.0/16"));
                                }
                            })
                            .setNeutralButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();
                } else {
                    String titleString = editTextSetTunSubnetwork.getText().toString().isEmpty() ? "Input is empty!" : "Address is invalid!";
                    builder.setTitle(titleString)
                            .setMessage("The address should match CIDR notation:\n192.1.0.0/16 (pattern for example)")
                            .setCancelable(false)
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();
                }
            }
        });

        editTextSetWlanSubnetwork.setHint(mSettings.getString(APP_PREFERENCES_WLAN_SUBNETWORK, "000.0.000.0/24"));
        editTextSetTunSubnetwork.setHint(mSettings.getString(APP_PREFERENCES_TUN_SUBNETWORK, "000.0.0.0/16"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // noinspection SimplifiableIfStatement
        if (id == R.id.about_app) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Network Trigger")
                    .setMessage("Application automatically monitors the activity of the network interfaces " +
                            "(tun0 and wlan0 devices) and adds routes and addresses for them.\n\n" +
                            "All addresses should match CIDR notation (compact representation of an "+
                            "IP address and its associated routing prefix), and can be changed in application main activity.\n\n" +
                            "For working with the system network settings application needs ROOT-access!")
                    .setCancelable(true)
                    .setIcon(R.drawable.ic_launcher);
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();
            return true;
        } else if (id == R.id.app_settings) {
            Toast.makeText(cntxt, "Settings", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}