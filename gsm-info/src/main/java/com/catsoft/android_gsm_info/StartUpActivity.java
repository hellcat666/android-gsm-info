package com.catsoft.android_gsm_info;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
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
 * - CellTowerLocationService
 * - CellTowerScanner
 *
 * Once initialization is done, it starts the Map Activity.
 */
public class StartUpActivity extends AppCompatActivity {

    private static final String TAG = "GSMInfo-StartUpActivity";

    private static final String UNIWIREDLABS_TOKEN = "9798a2800e6bf9";

    private static final String CELLTOWER_DETECTOR_READY = "celltower-detector-ready";
    private static final String LOCATION_SERVICE_READY = "location-service-ready";
    private static final String CELLTOWERS_DB_READY = "celltowers-db-ready";
    private static final String CELLTOWER_DETECTOR_ERROR = "celltower-detector-error";
    private static final String INITIALIZED = "initialized";
    private static final String DIALOG_OK_ANSWER = "dialog-ok-answer";
    private static final String LANDSCAPE = "Landscape";
    private static final String PORTRAIT = "Portrait";
    private static final String EXIT = "exit";

    private static Context mContext;

    private CellTowerDB mCellTowerDB = null;
    private boolean mCellDBReady = false;

    private CellTowerLocationService mCellTowerLocationService = null;
    private boolean mLocationServiceReady = false;

    private CellTowersScanner mCellTowersScanner = null;
    private boolean mCellDetectorReady = false;

    private IntentFilter mAppFilter = null;
    private boolean mReceiverRegistered = false;

    protected boolean mConnected = false;

    private ImageView mSplashImage = null;
    private TextView mSplashTextView = null;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get incoming Intent to check if it's an Exit
        Intent intent = getIntent();
        if((intent!=null) && (intent.getExtras()!=null)) {
            String cmd = (String) intent.getExtras().get(EXIT);
            if ((cmd!=null) &&(cmd.equals(EXIT))) {
                exitApp(false);         // Exit sent from MapActivity
            }
        }
        setContentView(R.layout.activity_startup);
        mContext = this.getApplicationContext();

        startGPSTrackingService();

        displaySplashImage();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        if(isConnected()) {
            setCellTowerDB();
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        if((mAppFilter==null) || (!mReceiverRegistered)) {
            mAppFilter = new IntentFilter();
            mAppFilter.addAction(CELLTOWERS_DB_READY);
            mAppFilter.addAction(LOCATION_SERVICE_READY);
            mAppFilter.addAction(CELLTOWER_DETECTOR_READY);
            mAppFilter.addAction(INITIALIZED);
            mAppFilter.addAction(DIALOG_OK_ANSWER);
            mAppFilter.addAction(EXIT);
            registerReceiver(mMessageReceiver, mAppFilter);
            mReceiverRegistered = true;
        }
    }

    private void unregisterReceiver() {
        if (mReceiverRegistered) {
            this.unregisterReceiver(mMessageReceiver);
            mReceiverRegistered = false;
        }
    }

    private boolean checkConnection() {

        mConnected = false;

        // Check if we're connected to GSM Network
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if((telephonyManager!=null) && (telephonyManager.getSimState()==TelephonyManager.SIM_STATE_READY) && (telephonyManager.getNetworkOperator()!=null && !telephonyManager.getNetworkOperator().equals(""))) {
            // Airplane Mode is OFF and SIMCard is PRESENT and READY, let's check Network
              // The following code is not supported with all Operators
//            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
//            if(networkInfo!=null && networkInfo.getType()==0) {
                mConnected = true;
//            }
//            else {
//                mConnected = false;
//                exitApp(true);
//            }
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
        mSplashTextView.setText("Loading Database ...");
        mCellTowerDB = new CellTowerDB(mContext, mContext.getString(R.string.db_name), null, 1);
    }

    /**
     * setCellTowerLocationService()
     * Start the CellTower Location Service that returns the GPS Coordinates of a given CellTower.
     * This is performed by calling a WebService hosted by UniWiredLabs, passing the CellTower
     * informations (cid, lac, mcc,mnc)
     * It returns the complete information about GPS Coordinates as well as the Address in plain text.
     *
     */
    private void setCellTowerLocationService() {
        mSplashTextView.setText("Start Location Service ...");
        mCellTowerLocationService = new CellTowerLocationService(mContext, UNIWIREDLABS_TOKEN);
    }

    /**
     * startCellTowersDetection()
     * Start CellTower Detection Service that will detect the CellTower we are connected to,
     * as well as when current CellTower changes
     *
     */
    private void startCellTowersDetection() {
        mSplashTextView.setText("Start CellTower Detection Service ...");
        mCellTowersScanner = new CellTowersScanner(mContext);
        mCellTowersScanner.start();
    }

    private void startGPSTrackingService() {
        Intent gpsTrackingServiceIntent = new Intent(this, GPSTrackingService.class);
        this.startService(gpsTrackingServiceIntent);
    }
    private void stopGPSTrackingService() {
        Intent gpsTrackingServiceIntent = new Intent(this, GPSTrackingService.class);
        this.stopService(gpsTrackingServiceIntent);
    }

    private void startMapActivity() {
        Intent anIntent = new Intent(this, MapActivity.class);
        this.startActivity(anIntent);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null) { return; }

            switch(intent.getAction()) {
                case CELLTOWERS_DB_READY:
//                    Log.i(TAG, "CellTowers DB OK");
                    mCellDBReady = true;
                    setCellTowerLocationService();
                    break;
                case LOCATION_SERVICE_READY:
//                    Log.i(TAG, "Location Service STARTED");
                    mLocationServiceReady = true;
                    startCellTowersDetection();
                    break;
                case CELLTOWER_DETECTOR_READY:
//                    Log.i(TAG, "CellTower Detector STARTED");
                    mCellDetectorReady = true;
                    Intent anIntent = new Intent();
                    anIntent.setAction(INITIALIZED);
                    sendBroadcast(anIntent);
                    break;
                case CELLTOWER_DETECTOR_ERROR:
//                    Log.i(TAG, "CellTower Detector ERROR");
                    mCellDetectorReady = false;
                    exitApp(true);
                    break;
                case INITIALIZED:
//                    Log.i(TAG, "App INITIALIZED");
                    mSplashTextView.setText("Loading Info & Map ...");
                    unregisterReceiver();
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
//        Log.i(TAG, "Exiting...");
        stopGPSTrackingService();
        if(error==true) {
            exitOnError();
        }
        else {
            finish();
        }
    }
}
