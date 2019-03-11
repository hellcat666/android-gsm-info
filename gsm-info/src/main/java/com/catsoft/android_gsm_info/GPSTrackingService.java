package com.catsoft.android_gsm_info;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 12.02.2019.
 */
public class GPSTrackingService extends Service implements LocationListener {

    private static final String TAG = "GPSTrackingService";

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // Minimum Distance to change Updates in meters (10m)
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // Minimum Time between Updates in milliseconds (6000ms -> 1min)

    private static Context mContext = null;              // Context

    private Looper mServiceLooper = null;               // Service Looper
    private ServiceHandler mServiceHandler = null;      // Service Handler
    private HandlerThread mHandlerThread = null;        // Handler Thread

    private LocationManager mLocationManager = null;    // LocationManager

    private String mProvider0 = LocationManager.GPS_PROVIDER;
    private String mProvider1 = LocationManager.NETWORK_PROVIDER;
    private int mInterval = 5000;           // Minimum time interval between location updates, in [msec] prv. 2500
    private int mMinDistance = 1;           // Minimum distance between location updates, in [m]

    private Location mLocation = null;      // Holds the current Location Data
    private boolean mProviderActive = false;

    private boolean mGPSEnabled = false;                // GPS Status
    private boolean mNetworkEnabled = false;            // Network Status

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = this;                           // Set Context

        turnGPSOn();

        Log.i(TAG, "LocationService.onCreate()");
        // To avoid cpu-blocking, we create a background handler to run our service
        mHandlerThread = new HandlerThread("LocationService", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        this.mServiceLooper = mHandlerThread.getLooper();
        // start the service using the background handler
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper, this);
        this.mLocationManager = (LocationManager) this.mContext.getSystemService(Context.LOCATION_SERVICE);
        this.mProviderActive = this.mLocationManager.isProviderEnabled(this.mProvider0.toString());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        this.mLocationManager.requestLocationUpdates(this.mProvider0.toString(), this.mInterval, this.mMinDistance, this);
        this.sendStatusMessage();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "LocationService.onLocationChanged()");
        this.mLocation = location;
        this.sendDataMessage();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(TAG, "LocationService.onStatusChanged()");
        this.sendStatusMessage();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "LocationService.onProviderEnabled()");
        mProviderActive = true;
        this.sendStatusMessage();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "LocationService.onProviderDisabled()");
        mProviderActive = false;
        this.sendStatusMessage();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "LocationService.onStartCommand(...)");

        // call a new service handler. The service ID can be used to identify the service
        Message message = this.mServiceHandler.obtainMessage();
        message.arg1 = startId;
        this.mServiceHandler.sendMessage(message);

        return START_REDELIVER_INTENT;
    }

    public void turnGPSOn() {
        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", true);
        sendBroadcast(intent);
    }

    // automatic turn off the gps
    public void turnGPSOff() {
        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", false);
        sendBroadcast(intent);
    }

    // Object responsible for Messaging
    private final class ServiceHandler extends Handler {

        private GPSTrackingService parent;

        public ServiceHandler(Looper looper, GPSTrackingService parent) {
            super(looper);
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {
            // Well calling mServiceHandler.sendMessage(message); from onStartCommand,
            // this method will be called.

            // Add your cpu-blocking activity here
        }
    }

    private void sendDataMessage() {
        Log.i(TAG, "LocationService.sendStatusMessage(" + String.valueOf(mLocation.getLatitude()) + "," + String.valueOf(mLocation.getLongitude()) + ")");
        Intent intent = new Intent("gps-location-data");
        // You can also include some extra data.
        intent.putExtra("message", "SENDING LOCATION SERVICE DATA.");
        intent.putExtra("location", this.mLocation);
        mContext.sendBroadcast(intent);
    }

    private void sendStatusMessage() {
        Log.i(TAG, "LocationService.sendStatusMessage()");
        Intent intent = new Intent("gps-location-status");
        Bundle statusBundle = new Bundle();
        statusBundle.putBoolean("provider-active", mProviderActive);
        intent.putExtra("status", statusBundle);
        mContext.sendBroadcast(intent);
    }

    private void sendProviderMessage() {
        Log.i(TAG,"LocationService.sendProviderMessage()");
        Intent intent = new Intent("gps-location-provider");
        Bundle statusBundle = new Bundle();
        statusBundle.putString("provider-active", String.valueOf(mProviderActive));
        intent.putExtra("status", statusBundle);
        mContext.sendBroadcast(intent);
    }

    private void sendExitMessage() {
        Log.i(TAG,"LocationService.sendExitMessage()");
        Intent intent = new Intent("location-exit");
        // You can also include some extra data.
        intent.putExtra("message", "LOCATION SERVICE EXIT.");
        mContext.sendBroadcast(intent);
    }
}
