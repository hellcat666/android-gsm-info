package com.catsoft.android_gsm_info;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 15.04.2018.
 */

/**
 * This Activity is the StartUp Activity.
 * Its role is to initialize the following resources:
 * - CellTowerDB
 * - UnwiredLabsService
 * - CellTowerScanner
 *
 * Once initialization is done, it starts the Map Activity.
 */
public class StartUpActivity extends AppCompatActivity {

    private static final String TAG = "GSMInfo-StartUpActivity";

    public static final int REQUEST_CODE = 1;

    private static final int PERMISSION_REQUEST_ACCESS_COARSE_LOCATION = 450;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 451;

    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 460;

    private static final String TOKEN = UnwiredLabsService.TOKEN;
    private static final String UNWIREDLABS_TOKEN = "9798a2800e6bf9";

    private static final String PERMISSIONS_GRANTED = "permissions-granted";
    private static final String GPS_TRACKING_SERVICE_READY = GPSTrackingService.GPS_TRACKING_SERVICE_READY;
    private static final String GPS_TRACKING_SERVICE_ERROR = GPSTrackingService.GPS_TRACKING_SERVICE_ERROR;
    private static final String GPS_TRACKING_PERMISSION_GRANTED = "gps-tracking-permission-granted";
    private static final String GPS_TRACKING_PERMISSION_DENIED = "gps-tracking-permission-denied";
    private static final String CELLTOWER_SERVICE_READY = CellTowerService.CELLTOWER_SERVICE_READY;
    private static final String CELLTOWER_SERVICE_ERROR = CellTowerService.CELLTOWER_SERVICE_ERROR;
    private static final String CELLTOWER_PERMISSION_GRANTED = "celltower-permission-granted";
    private static final String CELLTOWER_PERMISSION_DENIED = "celltower-permission-denied";
    public static final String UNWIREDLABS_SERVICE_READY = UnwiredLabsService.UNWIREDLABS_SERVICE_READY;
    public static final String UNWIREDLABS_SERVICE_ERROR = UnwiredLabsService.UNWIREDLABS_SERVICE_ERROR;
    private static final String CELLTOWERS_DB_READY = CellTowerDB.CELLTOWERS_DB_READY;
    private static final String SETTINGS_READY = SettingsActivity.SETTINGS_READY;
    private static final String INITIALIZED = "initialized";
    private static final String DIALOG_OK_ANSWER = "dialog-ok-answer";
    private static final String LANDSCAPE = "Landscape";
    private static final String PORTRAIT = "Portrait";
    public static final String EXIT = "exit";

    private static Context mContext;

    private AppSettings mAppSettings;

    private CellTowerDB mCellTowerDB = null;
    private boolean mCellDBReady = false;

    private UnwiredLabsService mCellTowerLocationService = null;

    private boolean mGPSTrackingServiceReady = false;
    private boolean mCellTowerServiceReady = false;

    private IntentFilter mAppFilter = null;
    private boolean mReceiverRegistered = false;

    protected boolean mConnected = false;

    private ImageView mSplashImage = null;
    private TextView mSplashTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        Log.i(TAG, "StartUpActivity.onCreate(...)");

        mContext = this.getApplicationContext();

        displaySplashImage();       // Display the App. StartUp Splash Image

        // Check if App. Settings already defined
        // Open Settings Activity if not yet defined
        mAppSettings = new AppSettings(getApplicationContext());
        if(!mAppSettings.isAlreadyDefined()) {
            Intent anIntent = new Intent(this, SettingsActivity.class);
            anIntent.putExtra("parent", this.getClass());
            anIntent.putExtra(SettingsActivity.APP_SETTINGS, mAppSettings);
            startActivity(anIntent);
        }
        // App Settings already defined
        else {
            registerReceiver();         // Register for incoming BroadcastReceiver Messages
            checkPermissions();         // Check Permissions. App. will Exit if User doesn't grant them
        }
    }

    private void displaySplashImage() {
        mSplashImage = findViewById(R.id.imgSplash);
        mSplashImage.setImageResource(R.drawable.gsm_info_splash512_3);
        mSplashTextView = findViewById(R.id.txtSplash);
        mSplashTextView.setText("Initializing ...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "StartUpActivity.onStart()");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "StartUpActivity.onResume()");
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "StartUpActivity.onPause()");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopGPSTrackingService();
        stopCellTowerService();
        unregisterReceiver();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showToast(LANDSCAPE);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            showToast(PORTRAIT);
        }
    }

    private void registerReceiver() {
        Log.i(TAG, "StartUpActivity.registerReceiver()");
        if((mAppFilter==null) || (!mReceiverRegistered)) {
            mAppFilter = new IntentFilter();
            mAppFilter.addAction(PERMISSIONS_GRANTED);
            mAppFilter.addAction(SETTINGS_READY);
            mAppFilter.addAction(GPS_TRACKING_SERVICE_READY);
            mAppFilter.addAction(GPS_TRACKING_SERVICE_ERROR);
            mAppFilter.addAction(GPS_TRACKING_PERMISSION_GRANTED);
            mAppFilter.addAction(GPS_TRACKING_PERMISSION_DENIED);
            mAppFilter.addAction(CELLTOWER_SERVICE_READY);
            mAppFilter.addAction(CELLTOWER_SERVICE_ERROR);
            mAppFilter.addAction(CELLTOWER_PERMISSION_GRANTED);
            mAppFilter.addAction(CELLTOWER_PERMISSION_DENIED);
            mAppFilter.addAction(CELLTOWERS_DB_READY);
            mAppFilter.addAction(UNWIREDLABS_SERVICE_READY);
            mAppFilter.addAction(UNWIREDLABS_SERVICE_ERROR);
            mAppFilter.addAction(INITIALIZED);
            mAppFilter.addAction(DIALOG_OK_ANSWER);
            mAppFilter.addAction(EXIT);
            registerReceiver(mMessageReceiver, mAppFilter);
            mReceiverRegistered = true;
        }
    }

    private void unregisterReceiver() {
        Log.i(TAG, "StartUpActivity.unregisterReceiver()");
        if (mReceiverRegistered) {
            this.unregisterReceiver(mMessageReceiver);
            mReceiverRegistered = false;
        }
    }

    private boolean checkConnection() {
        Log.i(TAG, "StartUpActivity.checkConnection()");
        mConnected = false;

        // Check if we're connected to GSM Network (Airplane Mode Off)
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if((telephonyManager!=null) && (telephonyManager.getSimState()==TelephonyManager.SIM_STATE_READY) && (telephonyManager.getNetworkOperator()!=null && !telephonyManager.getNetworkOperator().equals(""))) {
            mConnected = true;
        }
        else {
            mConnected = false;
            exitApp(true);
        }
        return mConnected;
    }

    private boolean isConnected() {
        checkConnection();
        return mConnected;
    }

    private void setCellTowerDB() {
        mSplashTextView.setText("Initializing Database ...");
        mCellTowerDB = new CellTowerDB(mContext, mContext.getString(R.string.db_name), null, 1);
    }
    /*
    private void setCellTowerLocationService() {
        mSplashTextView.setText("Start Location Service ...");
        mCellTowerLocationService = new UnwiredLabsService(mContext, UNWIREDLABS_TOKEN);
    }
    */
    /* UNUSED-UNUSED-UNUSED-UNUSED-UNUSED-UNUSED-UNUSED-UNUSED-UNUSED
    private void startCellTowersDetection() {

        mSplashTextView.setText("Start CellTower Detection Service ...");
        mCellTowerService = new CellTowerService();
        mCellTowerService.start();
    }
     */

    /**
     * startCellTowersDetection()
     * Start CellTower Detection Service that will detect the CellTower we are connected to,
     * as well as when current CellTower changes
     *
     */
    private void startCellTowerService() {
        Intent anIntent = new Intent(this, CellTowerService.class);
        this.startService(anIntent);
    }
    private void stopCellTowerService() {
        Intent anIntent = new Intent(this, GPSTrackingService.class);
        this.stopService(anIntent);
    }

    private void startGPSTrackingService() {
        Intent anIntent = new Intent(this, GPSTrackingService.class);
        anIntent.putExtra(GPSTrackingService.INTERVAL, mAppSettings.getGPSTrackingInterval());
        anIntent.putExtra(GPSTrackingService.MIN_DISTANCE, mAppSettings.getGPStrackingMinDistance());
        this.startService(anIntent);
    }
    private void stopGPSTrackingService() {
        Intent anIntent = new Intent(this, GPSTrackingService.class);
        this.stopService(anIntent);
    }
    /**
     * setUnwiredLabsService()
     * Start the UnwiredLabsService that returns the GPS Coordinates of a given CellTower.
     * This is performed by calling a WebService hosted by unwiredlabs.com, passing the CellTower
     * informations (cid, lac, mcc,mnc)
     * It returns the complete information about GPS Coordinates as well as the Address in plain text.
     *
     */
    private void startUnwiredLabsService() {
        Intent anIntent = new Intent(this, UnwiredLabsService.class);
        anIntent.putExtra(UnwiredLabsService.URL, mAppSettings.getUnwiredLabsUrl());
        anIntent.putExtra(UnwiredLabsService.TOKEN, mAppSettings.getUnwiredLabsToken());
        this.startService(anIntent);
    }
    private void stopUnwiredLabsService() {
        Intent anIntent = new Intent(this, UnwiredLabsService.class);
        this.stopService(anIntent);
    }

    private void startMapActivity() {
        Intent anIntent = new Intent(this, MapActivity.class);
        anIntent.putExtra(SettingsActivity.APP_SETTINGS, mAppSettings);
        this.startActivity(anIntent);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        Intent anIntent = null;
        if (intent == null) { return; }

        switch(intent.getAction()) {
            case PERMISSIONS_GRANTED:
                Log.i(TAG, "All Permissions GRANTED");
                startUnwiredLabsService();
                break;
            case SETTINGS_READY:
                Log.i(TAG, "Settings Ready");
                registerReceiver();
                displaySplashImage();       // Display the App. Splash Image
                checkPermissions();         // Check Permissions. App. will Exit if User doesn't grant them
                break;
            case GPS_TRACKING_PERMISSION_GRANTED:
                startGPSTrackingService();
                checkTelephonyPermissions();
                break;
            case GPS_TRACKING_PERMISSION_DENIED:
                exitApp(false);
                break;
            case GPS_TRACKING_SERVICE_READY:
                Log.i(TAG, "GPSTrackingService (Location) STARTED");
                mGPSTrackingServiceReady = true;
                break;
            case GPS_TRACKING_SERVICE_ERROR:
                Log.i(TAG, "GPSTrackingService (Location) ERROR");
                mGPSTrackingServiceReady = false;
                break;
            case CELLTOWER_PERMISSION_GRANTED:
                startCellTowerService();
                anIntent = new Intent();
                anIntent.setAction(PERMISSIONS_GRANTED);
                mContext.sendBroadcast(anIntent);
                break;
            case CELLTOWER_PERMISSION_DENIED:
                exitApp(false);
                break;
            case CELLTOWER_SERVICE_READY:
                Log.i(TAG, "CellTowerService STARTED");
                mCellTowerServiceReady = true;
                break;
            case CELLTOWER_SERVICE_ERROR:
                Log.i(TAG, "CellTowerService ERROR");
                mCellTowerServiceReady = false;
                exitApp(true);
                break;
            case CELLTOWERS_DB_READY:
                Log.i(TAG, "CellTowers DB READY");
                mCellDBReady = true;
                anIntent = new Intent();
                anIntent.setAction(INITIALIZED);
                sendBroadcast(anIntent);
                break;
            case UNWIREDLABS_SERVICE_READY:
                Log.i(TAG, "UnwiredLabs Service Ready");
                setCellTowerDB();
                break;
            case UNWIREDLABS_SERVICE_ERROR:
                break;
            case INITIALIZED:
                Log.i(TAG, "App INITIALIZED");
                mSplashTextView.setText("Loading Info & Map ...");
                unregisterReceiver();
                // Keep EXIT Action registered
                mAppFilter = new IntentFilter();
                mAppFilter.addAction(EXIT);
                registerReceiver(mMessageReceiver, mAppFilter);
                // Start App. main Activity
                startMapActivity();
                break;
            case DIALOG_OK_ANSWER:
                exitApp(false);
                break;
            case EXIT:
                exitApp(false);
                break;
            default:
                break;
        }
        }
    };

    /****************************************************************************************************/
    /* Permissions Management Methods                                                                   */
    /****************************************************************************************************/
    private void checkPermissions() {
        mSplashTextView.setText("Checking Permissions ...");
        checkGPSTrackingPermissions();
    }

    // Check GPSTracking related Permissions (ACCESS_COARSE_LOCATION & ACCESS_FINE_LOCATION)
    private void checkGPSTrackingPermissions() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Log.i(TAG, "Check Location Permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_ACCESS_COARSE_LOCATION);
        }
        else {
            // Already Granted, let's check Telephony related Permissions
            Log.i(TAG, "Location Permissions already Granted");
            Intent anIntent = new Intent();
            anIntent.setAction(GPS_TRACKING_PERMISSION_GRANTED);
            mContext.sendBroadcast(anIntent);
        }
    }

    // Check CellTowerService related Permissions (READ_PHONE_STATE)
    private void checkTelephonyPermissions() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Check Telephony Permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_READ_PHONE_STATE);
        }
        else {
            // Already Granted, notify that All permissions have been Granted
            Log.i(TAG, "Telephony Permissions already Granted");
            Intent anIntent = new Intent();
            anIntent.setAction(CELLTOWER_PERMISSION_GRANTED);
            mContext.sendBroadcast(anIntent);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
             case PERMISSION_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Log.i(TAG, "ACCESS_COARSE_LOCATION Granted");
                    // Now Check ACCESS_FINE_LOCATION
                    if ( Build.VERSION.SDK_INT >= 23 &&
                            (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                        Log.i(TAG, "Check ACCESS_FINE_LOCATION Permissions");
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
                    }
                    else {
                        Intent anIntent = new Intent();
                        anIntent.setAction(GPS_TRACKING_PERMISSION_GRANTED);
                        mContext.sendBroadcast(anIntent);
                    }
                } else {
                    // Permission Denied, Too bad ...
                    Log.w(TAG, "ACCESS_COARSE_LOCATION Denied, exiting ...");
                    Intent anIntent = new Intent();
                    anIntent.setAction(GPS_TRACKING_PERMISSION_DENIED);
                    mContext.sendBroadcast(anIntent);
                }
                break;
            }
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Log.i(TAG, "ACCESS_FINE_LOCATION Granted");
                    Intent anIntent = new Intent();
                    anIntent.setAction(GPS_TRACKING_PERMISSION_GRANTED);
                    mContext.sendBroadcast(anIntent);
                }
                else {
                    // Permission Denied, Too bad ...
                    Log.w(TAG, "ACCESS_FINE_LOCATION Denied, exiting ...");
                    Intent anIntent = new Intent();
                    anIntent.setAction(GPS_TRACKING_PERMISSION_DENIED);
                    mContext.sendBroadcast(anIntent);
                }
                break;
            case PERMISSION_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Log.i(TAG, "READ_PHONE_STATE Granted");
                    Intent anIntent = new Intent();
                    anIntent.setAction(CELLTOWER_PERMISSION_GRANTED);
                    mContext.sendBroadcast(anIntent);
                } else {
                    // Permission Denied, Too bad ...
                    Log.i(TAG, "READ_PHONE_STATE Denied, exiting ...");
                    Intent anIntent = new Intent();
                    anIntent.setAction(CELLTOWER_PERMISSION_DENIED);
                    mContext.sendBroadcast(anIntent);
                }
                break;
            }
        }
    }

    /****************************************************************************************************/

    private void showToast(final String msg){
        //gets the main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // run this code in the main thread
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void exitOnError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.device_offline_error);
        builder.setCancelable(true);

        builder.setNegativeButton(
                " OK ",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent anIntent = new Intent();
                        anIntent.setAction(DIALOG_OK_ANSWER);
                        mContext.sendBroadcast(anIntent);
                    }
                });

        AlertDialog alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();

    }

    public final void exitApp(boolean error) {
        Log.i(TAG, "Exiting...");
        stopGPSTrackingService();
        stopCellTowerService();
        stopUnwiredLabsService();
        if(error==true) {
            exitOnError();
        }
        else {
            finish();
            System.exit(0);
        }
    }
}
