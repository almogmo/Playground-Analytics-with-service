package com.selina.playgroundanalytics.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.selina.playgroundanalytics.utils.Constants;
import com.selina.playgroundanalytics.utils.NetworkController;
import com.selina.playgroundanalytics.R;
import com.selina.playgroundanalytics.activities.MainActivity;

import java.util.ArrayList;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class BluetoothScanService extends Service {

    private static final String TAG = "BluetoothScanService";

    private ArrayList devicesArray;
    private Handler handler;
    private Runnable runnable;
    private boolean isScanning;

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner btScanner;


    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getName() == null) {
                if (!devicesArray.contains(result.getDevice())) {
                    devicesArray.add(result.getDevice());
                }
            }
        }
    };

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        isScanning = true;
        devicesArray = new ArrayList();



        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        if (btAdapter != null) {
            btScanner = btAdapter.getBluetoothLeScanner();
        }

        if (btScanner != null) {
            btScanner.startScan(leScanCallback);
        }

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isScanning) {
                    NetworkController.postPeopleAmount(devicesArray.size());
                }
                handler.postDelayed(this, intent.getLongExtra(Constants.INTENT_EXTRA_SCAN_INTERVAL_MS_NAME, Constants.DEFAULT_SCAN_INTERVAL)); //
            }
        }, 3000); // First submission in 3 seconds and use the given interval later

        buildForegroundNotification();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        isScanning = false;
        if (btAdapter != null && btAdapter.isEnabled() && btScanner != null) {
            btScanner.stopScan(leScanCallback);
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void buildForegroundNotification() {
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification.Builder builder = new Notification.Builder(this, NotificationChannel.DEFAULT_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setAutoCancel(false)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.ic_launcher_background);

            Notification notification = builder.build();
            startForeground(123, notification);

        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(false)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.ic_launcher_background);

            Notification notification = builder.build();
            startForeground(123, notification);
        }
    }
}

