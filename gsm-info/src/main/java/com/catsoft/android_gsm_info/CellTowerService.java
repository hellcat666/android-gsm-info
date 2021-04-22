package com.catsoft.android_gsm_info;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 03.04.2018.
 */

/**
 * This class encapsulates the CellTower Scanner.
 * It instantiates a PhoneStateListener which listen the following events:
 *  - CellInfo
 *  - CellLocation
 *  - Signal Strength
 *
 *  It sends the following messages according to the received event:
 *  - cell-detected (onCellLocationChanged(..))
 *  - gsm-signal-strength-changed (onSignalStrengthsChanged(..))
 *
 * It handles the following messages
 *  - request-current-cell-tower
 *
 *
 */

public class CellTowerService extends Service {

    public static final String TAG = "CellTowerService";

    public static final String CELLTOWER_SERVICE_READY = "celltower-service-ready";
    public static final String CELLTOWER_SERVICE_ERROR = "celltower-service-error";
    private static final String REQUEST_CURRENT_CELLTOWER = "request-current-celltower";


    private static final String CELL = "cell";
    private static final String CELL_INFO = "cell-info";
    private static final String CELL_INFO_CHANGED = "cell-info-changed";
    private static final String CELL_LOCATION_CHANGED = "cell-location-changed";

    private static final String CELL_SIGNAL_STRENGTH_CHANGED = "cell-signal-strength-changed";
    private static final String CELL_SIGNAL_STRENGTH = "cell-signal-strength";

    private static final String GSM = "GSM";
//    private static final String PROVIDER = "Sunrise Switzerland";

    private Context mContext;

    private Looper mServiceLooper = null;                           // Service Looper
    private CellTowerService.ServiceHandler mServiceHandler = null; // Service Handler
    private HandlerThread mHandlerThread = null;                    // Handler Thread

    private TelephonyManager mTelephonyManager;
    private int mListenMask;
    private String mNetworkType = null;

    private IntentFilter mAppFilter;
    private boolean mReceiverRegistered = false;

    private CellInfo mCellInfo = null;
    private CellTower mCurrentCellTower = null;
    private int mGSMSignalStrengthDbm = -1;

    private Thread mTestThread = null;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = this;                           // Set Context

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // To avoid cpu-blocking, we create a background handler to run our service
        mHandlerThread = new HandlerThread("LocationService", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        this.mServiceLooper = mHandlerThread.getLooper();
        // start the service using the background handler
        this.mServiceHandler = new CellTowerService.ServiceHandler(this.mServiceLooper, this);

        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mListenMask = PhoneStateListener.LISTEN_CELL_INFO +
                      PhoneStateListener.LISTEN_CELL_LOCATION +
                      PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;

        registerReceiver();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Start CellTowerService");
        // call a new service handler. The service ID can be used to identify the service
         Message message = this.mServiceHandler.obtainMessage();
        message.arg1 = startId;
        this.mServiceHandler.sendMessage(message);
        this.start();
        return START_STICKY;        // START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroy/Stop CellTowerService");
        super.onDestroy();
        this.stop();
    }

    private void registerReceiver() {
        Log.i(TAG, "registerReceiver()");
        if (mReceiverRegistered == false) {
            try {
                mAppFilter = new IntentFilter();
                mAppFilter.addAction(REQUEST_CURRENT_CELLTOWER);
                mContext.registerReceiver(mMessageReceiver, mAppFilter);
                mReceiverRegistered = true;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            } finally {
            }
        }
    }

    private void unregisterReceiver() {
        Log.i(TAG, "unregisterReceiver()");
        if(mReceiverRegistered==true) {
            try {
                mContext.unregisterReceiver(mMessageReceiver);
                mReceiverRegistered = false;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            } finally {
            }
        }
    }

    protected PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

        public void onCellInfoChanged (CellInfo cellInfo) {
            Log.i(TAG, "onCellInfoChanged()");
            beep0();
            mCellInfo = cellInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Intent anIntent = new Intent();
                anIntent.setAction(CELL_INFO_CHANGED);
                anIntent.putExtra(CELL, mCellInfo);
                mContext.sendBroadcast(anIntent);
            }
        }

        public void onSignalStrengthsChanged (SignalStrength signalStrength) {
            Log.i(TAG, "onSignalStrengthsChanged()");
            super.onSignalStrengthsChanged(signalStrength);
//            Gson gson = new Gson();
            beep1();
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99)
                    mGSMSignalStrengthDbm = signalStrength.getGsmSignalStrength() * 2 - 113;
                else
                    mGSMSignalStrengthDbm = signalStrength.getGsmSignalStrength();
            } else {
                mGSMSignalStrengthDbm = signalStrength.getCdmaDbm();
            }

            Intent anIntent = new Intent();
            anIntent.setAction(CELL_SIGNAL_STRENGTH_CHANGED);
            anIntent.putExtra(CELL, mCurrentCellTower);
            anIntent.putExtra(CELL_SIGNAL_STRENGTH, mGSMSignalStrengthDbm);
            mContext.sendBroadcast(anIntent);
        }

        public void onCellLocationChanged(CellLocation location) {
        GsmCellLocation gsmLocation = null;
            Log.i(TAG, "onCellLocationChanged()");
            beep2();
            if (location instanceof GsmCellLocation) {
                gsmLocation = (GsmCellLocation) location;
                setCurrentCellInfo(gsmLocation);
            }
            else if (location instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaLocation = (CdmaCellLocation) location;
                StringBuilder sb = new StringBuilder();
                sb.append(cdmaLocation.getBaseStationId());
                sb.append("\n@");
                sb.append(cdmaLocation.getBaseStationLatitude());
                sb.append(cdmaLocation.getBaseStationLongitude());
                Toast.makeText(mContext,
                        sb.toString(),
                        Toast.LENGTH_LONG).show();
            }
            Log.i(TAG, "Current Cell ID is: " + String.valueOf(mCurrentCellTower.mCId));
            Intent anIntent = new Intent();
            anIntent.setAction(CELL_LOCATION_CHANGED);
            anIntent.putExtra(CELL, mCurrentCellTower);
            mContext.sendBroadcast(anIntent);
        }
    };

    private void setCurrentCellInfo(GsmCellLocation location) {
        Log.i(TAG, "setCurrentCellInfo(...)");
        GsmCellLocation gsmLocation;
        int cid = -1;
        int lac = -1;
        int mcc = -1;
        int mnc = -1;
        String networkType = "";
        String providerName = "";

        if(mTelephonyManager!=null) {
            if(location==null) {
                gsmLocation = (GsmCellLocation) mTelephonyManager.getCellLocation();
            }
            else {
                gsmLocation = location;
            }
            if (gsmLocation!=null) {
               cid = gsmLocation.getCid();
               lac = gsmLocation.getLac();
                String networkOperator = mTelephonyManager.getNetworkOperator();
                if (!TextUtils.isEmpty(networkOperator)) {
                    mcc = Integer.parseInt(networkOperator.substring(0, 3));
                    mnc = Integer.parseInt(networkOperator.substring(3));
                    providerName = mTelephonyManager.getNetworkOperatorName();
                }
                else {
                    String simOperator = mTelephonyManager.getSimOperator();
                    if (!TextUtils.isEmpty(simOperator)) {
                        mcc = Integer.parseInt(simOperator.substring(0, 3));
                        mnc = Integer.parseInt(simOperator.substring(3));
                        providerName = mTelephonyManager.getSimOperatorName();
                    }
                }
                mNetworkType = GSM;
                mCurrentCellTower = new CellTower(cid, lac, mcc, mnc, networkType, providerName);
            }
        }
    }

    public boolean start() {
        Log.i(TAG, "start()");
        Intent anIntent = new Intent();
        if ((mTelephonyManager != null) && (mPhoneStateListener != null)) {

            mTelephonyManager.listen(mPhoneStateListener, mListenMask);
            anIntent.setAction(CELLTOWER_SERVICE_READY);
            mContext.sendBroadcast(anIntent);
            return true;
        } else {
            Toast.makeText(mContext,
                    "Failed to start CellTowers Scanner",
                    Toast.LENGTH_LONG).show();
            anIntent.setAction(CELLTOWER_SERVICE_ERROR);
            mContext.sendBroadcast(anIntent);
            return false;
        }
    }

    public void stop() {
        Log.i(TAG, "stop()");
        this.unregisterReceiver();
        if ((mTelephonyManager != null) && (mPhoneStateListener != null)) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null) { return; }

            switch(intent.getAction()) {
                case REQUEST_CURRENT_CELLTOWER:
                    Log.i(TAG, "REQUEST_CURRENT_CELLTOWER Message received");
                    setCurrentCellInfo(null);
                    Intent anIntent = new Intent();
                    anIntent.setAction(CELL_INFO_CHANGED);
                    anIntent.putExtra(CELL, mCurrentCellTower);
                    mContext.sendBroadcast(anIntent);
                    break;
            }
        }
    };

    private void beep0() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 15);
        toneG.startTone(ToneGenerator.TONE_DTMF_0, 15);
    }

    private void beep1() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 15);
        toneG.startTone(ToneGenerator.TONE_DTMF_1, 15);
    }

    private void beep2() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 15);
        toneG.startTone(ToneGenerator.TONE_DTMF_2, 15);
    }

    // Object responsible for Messaging
    private final class ServiceHandler extends Handler {

        private CellTowerService parent;

        public ServiceHandler(Looper looper, CellTowerService parent) {
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
}



