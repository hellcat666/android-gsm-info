package com.catsoft.android_gsm_info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.Manifest.permission.READ_PHONE_STATE;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 15.04.2018.
 */

public class CellTowerInfoFragment extends android.support.v4.app.Fragment {

    private final static String TAG = "GSMInfo-CellTowerInfoFragment";

    private static final String FRAG_CELLTOWER_INFO = "frag-celltower-info";
    private static final String FRAGMENT_INFO_READY = "fragment-info-ready";
    private static final String FRAGMENT = "fragment";

    private static final String CELL = "cell";
    private static final String CELL_INFO = "cell-info";
    private static final String CELL_INFO_CHANGED = "cell-info-changed";
    private static final String CELL_INFO_UPDATED = "cell-info-updated";
    private static final String CELL_DETECTED= "cell-detected";
    private static final String CELL_SIGNAL_STRENGTH_CHANGED = "cell-signal-strength-changed";
    private static final String CELL_SIGNAL_STRENGTH = "cell-signal-strength";
    private static final String CELL_LOCATION_CHANGED = "cell-location-changed";
    private static final String REQUEST_CURRENT_CELLTOWER = "request-current-celltower";

    private static final String REFRESH = "refresh";

    private static final String RX_REGISTERED = "rx-registered";
    private static final String NOT_AVAILABLE = "n.a";

    private static View mView;
    private Context mContext;
    private Bundle mSavedInstanceState = null;

    private IntentFilter mAppFilter = null;
    private boolean mReceiverRegistered = false;

    private TextView mCellIdTextView;
    private TextView mLocationAreaCodeTextView;
    private TextView mMobileNetworkOperatorTextView;
    private TextView mSignalStrengthTextView;
    private TextView mNetworkTypeTextView;
    private TextView mOperatorNameTextView;

    private  CellTower mCurrentCellTower;
    private int mSignalStrength = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getContext();
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_cell_tower_info, container, false);
        return mView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCellIdTextView = getActivity().findViewById(R.id.txtCellId);
        mLocationAreaCodeTextView = getActivity().findViewById(R.id.txtLocationAreaCode);
        mMobileNetworkOperatorTextView = getActivity().findViewById(R.id.txtMobileNetworkOperator);
        mSignalStrengthTextView = getActivity().findViewById(R.id.txtSignalStrength);
        mNetworkTypeTextView = getActivity().findViewById(R.id.txtNetworkType);
        mOperatorNameTextView = getActivity().findViewById(R.id.txtOperatorName);

        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(RX_REGISTERED, mReceiverRegistered);
        outState.putParcelable(CELL_INFO, mCurrentCellTower);
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver();
        Intent anIntent = new Intent();
        anIntent.setAction(FRAGMENT_INFO_READY);
        anIntent.putExtra(FRAGMENT, FRAG_CELLTOWER_INFO);
        mContext.sendBroadcast(anIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mSavedInstanceState!=null) {
            mReceiverRegistered = mSavedInstanceState.getBoolean(RX_REGISTERED);
            mCurrentCellTower = mSavedInstanceState.getParcelable(CELL_INFO);
            if(mCurrentCellTower!=null) {
                refreshCellTowerInfo();
            }
            else {
                requestCurrentCellTower();
            }
        }
        else {
            requestCurrentCellTower();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{READ_PHONE_STATE},
                    1);
            return false;
        }
        return true;
    }

    private void registerReceiver() {
        if (!mReceiverRegistered) {
            try {
                mAppFilter = new IntentFilter();
                mAppFilter.addAction(FRAGMENT_INFO_READY);
                mAppFilter.addAction(CELL_INFO_CHANGED);
                mAppFilter.addAction(CELL_DETECTED);
                mAppFilter.addAction(CELL_SIGNAL_STRENGTH_CHANGED);
                mAppFilter.addAction(CELL_LOCATION_CHANGED);
                mContext.registerReceiver(mMessageReceiver, mAppFilter);
                mReceiverRegistered = true;
            }
            catch(Exception ex) {}
            finally {}
        }
    }

    private void unregisterReceiver() {
        if(mReceiverRegistered) {
            try {
                mContext.unregisterReceiver(mMessageReceiver);
                mReceiverRegistered = false;
            } catch (Exception ex) {
            } finally {
            }
        }
    }

    private void requestCurrentCellTower() {
        Log.i(TAG, "requestCurrentCellTower()");
        Intent anIntent = new Intent();
        anIntent.setAction(REQUEST_CURRENT_CELLTOWER);
        mContext.sendBroadcast(anIntent);
    }

    private void getCellTowerInfo() {

        mCurrentCellTower = new CellTower();
    }

    protected void refresh() {
        refreshCellTowerInfo();
        Intent anIntent = new Intent();
        if(mCurrentCellTower!=null) {
            anIntent.setAction(CELL_INFO_UPDATED);
            anIntent.putExtra(CELL, mCurrentCellTower);
            mContext.sendBroadcast(anIntent);
        }
    }

    protected void refreshCellTowerInfo() {
        if (mCurrentCellTower != null) {
            mCellIdTextView.setText(String.valueOf(mCurrentCellTower.getCId()));
            mLocationAreaCodeTextView.setText(String.valueOf(mCurrentCellTower.getLac()));
            mMobileNetworkOperatorTextView.setText(String.valueOf(mCurrentCellTower.getMCC() + " / " + mCurrentCellTower.getMNC()));
            mSignalStrengthTextView.setText(String.valueOf(mSignalStrength));
            if(mSignalStrength==-1) {
                mSignalStrengthTextView.setText(NOT_AVAILABLE);
            }
            else {
                if (mSignalStrength < -109) {
                    mSignalStrengthTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkRed));
                } else if ((mSignalStrength > -109) && (mSignalStrength < -53)) {
                    mSignalStrengthTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.blue));
                } else if (mSignalStrength > -53) {
                    mSignalStrengthTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkGreen));
                }
            }
            mNetworkTypeTextView.setText(mCurrentCellTower.getNetworkType());
            mOperatorNameTextView.setText(mCurrentCellTower.getProviderName());
        }
        else {
            mCellIdTextView.setText(NOT_AVAILABLE);
            mLocationAreaCodeTextView.setText(NOT_AVAILABLE);
            mMobileNetworkOperatorTextView.setText(NOT_AVAILABLE + " / " + NOT_AVAILABLE);
            mSignalStrengthTextView.setText(NOT_AVAILABLE);
            mNetworkTypeTextView.setText(NOT_AVAILABLE);
            mOperatorNameTextView.setText(NOT_AVAILABLE);
        }
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "mMessageReceiver.onReceive()");

            if (intent == null) { return; }

            switch(intent.getAction()) {
                case FRAGMENT_INFO_READY:
                    Log.i(TAG, "mMessageReceiver.onReceive('fragment-info-ready')");
                    requestCurrentCellTower();
                    break;
                case CELL_INFO_CHANGED:
                    Log.i(TAG, "mMessageReceiver.onReceive('cell-info-changed')");
                    mCurrentCellTower = (CellTower) intent.getExtras().get(CELL);
                    refresh();
                    break;
                case CELL_SIGNAL_STRENGTH_CHANGED:
                    Log.i(TAG, "mMessageReceiver.onReceive('cell-signal-strength-changed)");
                    mSignalStrength = (int) intent.getExtras().get(CELL_SIGNAL_STRENGTH);
                    refresh();
                    break;
                case CELL_DETECTED:
                    mCurrentCellTower = (CellTower) intent.getExtras().get(CELL);
                    refresh();
                    break;
                case CELL_LOCATION_CHANGED:
                    mCurrentCellTower = (CellTower) intent.getExtras().get(CELL);
                    refresh();
                    break;
                default:
                    refresh();
                    break;
            }
        }
    };
}
