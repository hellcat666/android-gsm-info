package com.catsoft.android_gsm_info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.StrictMode;
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

public class CellTowersScanner {

    public static final String TAG = "GSMInfo-CellTowersScan";

    private static final String CELLTOWER_DETECTOR_READY = "celltower-detector-ready";
    private static final String CELLTOWER_DETECTOR_ERROR = "celltower-detector-error";
    private static final String REQUEST_CURRENT_CELLTOWER = "request-current-celltower";


    private static final String CELL = "cell";
    private static final String CELL_INFO = "cell-info";
    private static final String CELL_DETECTED= "cell-detected";
    private static final String CELL_INFO_CHANGED = "cell-info-changed";
    private static final String CELL_LOCATION_CHANGED = "cell-location-changed";

    private static final String CELL_SIGNAL_STRENGTH_CHANGED = "cell-signal-strength-changed";
    private static final String CELL_SIGNAL_STRENGTH = "cell-signal-strength";

    private static final String GSM = "GSM";
//    private static final String PROVIDER = "Sunrise Switzerland";

    private Context mContext;

    private TelephonyManager mTelephonyManager;
    private int mListenMask;

    private IntentFilter mAppFilter;

    private CellInfo mCellInfo = null;
    private CellTower mCurrentCellTower = null;
    private int mGSMSignalStrengthDbm = -1;

    private Thread mTestThread = null;

    public CellTowersScanner(Context context) {

        mContext = context;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mListenMask = PhoneStateListener.LISTEN_CELL_INFO +
                      PhoneStateListener.LISTEN_CELL_LOCATION +
                      PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;

        mAppFilter = new IntentFilter();
        mAppFilter.addAction(REQUEST_CURRENT_CELLTOWER);
        mContext.registerReceiver(mMessageReceiver, mAppFilter);
    }



    protected PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

        public void onCellInfoChanged (CellInfo cellInfo) {
            Log.i(TAG, "onCellInfoChanged()");
            beep();
            mCellInfo = cellInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Intent anIntent = new Intent();
                anIntent.setAction(CELL_DETECTED);
                anIntent.putExtra(CELL, mCellInfo);
                mContext.sendBroadcast(anIntent);
            }
        }

        public void onSignalStrengthsChanged (SignalStrength signalStrength) {
            Log.i(TAG, "onSignalStrengthsChanged()");
            beep();
            mGSMSignalStrengthDbm = (2 * signalStrength.getGsmSignalStrength()) - 113;
            Intent anIntent = new Intent();
            anIntent.setAction(CELL_SIGNAL_STRENGTH_CHANGED);
            anIntent.putExtra(CELL, mCurrentCellTower);
            anIntent.putExtra(CELL_SIGNAL_STRENGTH, mGSMSignalStrengthDbm);
            mContext.sendBroadcast(anIntent);
        }

        public void onCellLocationChanged(CellLocation location) {
        GsmCellLocation gsmLocation = null;
            Log.i(TAG, "onCellLocationChanged()");
            beep();
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
            Intent anIntent = new Intent();
            anIntent.setAction(CELL_LOCATION_CHANGED);
            anIntent.putExtra(CELL, mCurrentCellTower);
            mContext.sendBroadcast(anIntent);
        }
    };

    private void setCurrentCellInfo(GsmCellLocation location) {
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
                networkType = GSM;
                mCurrentCellTower = new CellTower(cid, lac, mcc, mnc, networkType, providerName);
            }
        }
    }

    public boolean start() {
        Intent anIntent = new Intent();
        if ((mTelephonyManager != null) && (mPhoneStateListener != null)) {

            mTelephonyManager.listen(mPhoneStateListener, mListenMask);
            anIntent.setAction(CELLTOWER_DETECTOR_READY);
            mContext.sendBroadcast(anIntent);
            return true;
        } else {
            Toast.makeText(mContext,
                    "Failed to start CellTowers Scanner",
                    Toast.LENGTH_LONG).show();
            anIntent.setAction(CELLTOWER_DETECTOR_ERROR);
            mContext.sendBroadcast(anIntent);
            return false;
        }
    }

    public void stop() {
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
                    Log.i(TAG, "REQUEST_CURRENT_CELLTOWER");
                    setCurrentCellInfo(null);
                    Intent anIntent = new Intent();
                    anIntent.setAction(CELL_INFO_CHANGED);
                    anIntent.putExtra(CELL, mCurrentCellTower);
                    mContext.sendBroadcast(anIntent);
                    break;
            }
        }
    };

    private void beep() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 25);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 25);
    }
}



