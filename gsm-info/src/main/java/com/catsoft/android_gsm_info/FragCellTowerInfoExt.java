package com.catsoft.android_gsm_info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
 * Created by HellCat on 29.03.2018.
 */

public class FragCellTowerInfoExt extends android.support.v4.app.Fragment {

    private static final String TAG = "GSMInfo-FragCellTowerInfoExt";

    private static final String FRAG_CELLTOWER_INFO_EXT = "frag-celltower-info-ext";
    private static final String FRAGMENT_READY = "fragment-ready";
    private static final String FRAGMENT = "fragment";

    private static final String CELL = "cell";
    private static final String CELL_INFO = "cell-info";
    private static final String CELL_DETECTED = "cell-detected";

    private static final String NOT_AVAILABLE = "n.a";

    private Context mContext;
    private IntentFilter mAppFilter;

    private CellTower mCellTower;

    private TextView mCellIdTextView;
    private TextView mLocationAreaCodeTextView;
    private TextView mMobileCountryCodeTextView;
    private TextView mMobileNetworkCodeTextView;
    private TextView mNetworkTypeTextView;
    private TextView mOperatorNameTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mAltitudeTextView;
    private TextView mAccuracyTextView;
    private TextView mAddressTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_cell_tower_info_ext, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCellIdTextView = getActivity().findViewById(R.id.txtCellId);
        mLocationAreaCodeTextView = getActivity().findViewById(R.id.txtLocationAreaCode);
        mMobileCountryCodeTextView = getActivity().findViewById(R.id.txtMobileCountryCode);
        mMobileNetworkCodeTextView = getActivity().findViewById(R.id.txtMobileNetworkCode);
        mNetworkTypeTextView = getActivity().findViewById(R.id.txtNetworkType);
        mOperatorNameTextView = getActivity().findViewById(R.id.txtOperatorName);
        mLatitudeTextView = getActivity().findViewById(R.id.txtLatitude);
        mLongitudeTextView = getActivity().findViewById(R.id.txtLongitude);
        mAltitudeTextView = getActivity().findViewById(R.id.txtAltitude);
        mAccuracyTextView = getActivity().findViewById(R.id.txtAccuracy);
        mAddressTextView = getActivity().findViewById(R.id.txtAddress);

        mAppFilter = new IntentFilter();
        mAppFilter.addAction(CELL_DETECTED);
        mContext.registerReceiver(mMessageReceiver, mAppFilter);
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent anIntent = new Intent();
        anIntent.setAction(FRAGMENT_READY);
        anIntent.putExtra(FRAGMENT, FRAG_CELLTOWER_INFO_EXT);
        getContext().sendBroadcast(anIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAppFilter = new IntentFilter();
        mAppFilter.addAction(CELL_INFO);
        mContext.registerReceiver(mMessageReceiver, mAppFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mMessageReceiver);
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

    private void getCellTowerInfo() {

        mCellTower = new CellTower();
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "mMessageReceiver.onReceive()");

            if ("cell-info".equalsIgnoreCase(intent.getAction())) {
                mCellTower = (CellTower) intent.getExtras().get(CELL);
                if (mCellTower != null) {
                    mCellIdTextView.setText(String.valueOf(mCellTower.getCId()));
                    mLocationAreaCodeTextView.setText(String.valueOf(mCellTower.getLac()));
                    mMobileCountryCodeTextView.setText(String.valueOf(mCellTower.getMCC()));
                    mMobileNetworkCodeTextView.setText(String.valueOf(mCellTower.getMNC()));
                    mNetworkTypeTextView.setText(mCellTower.getNetworkType());
                    mOperatorNameTextView.setText(mCellTower.getProviderName());
                    mLatitudeTextView.setText(String.valueOf(mCellTower.getLocation().getLatitude()));
                    mLongitudeTextView.setText(String.valueOf(mCellTower.getLocation().getLongitude()));
                    mAltitudeTextView.setText(NOT_AVAILABLE);
                    mAccuracyTextView.setText(String.valueOf(mCellTower.getLocation().getAccuracy()));
                    mAddressTextView.setText(NOT_AVAILABLE);
                }
                else {
                    mCellIdTextView.setText(String.valueOf(-1));
                    mLocationAreaCodeTextView.setText(String.valueOf(-1));
                    mMobileCountryCodeTextView.setText(String.valueOf(-1));
                    mMobileNetworkCodeTextView.setText(String.valueOf(-1));
                    mNetworkTypeTextView.setText(NOT_AVAILABLE);
                    mOperatorNameTextView.setText(NOT_AVAILABLE);
                    mLatitudeTextView.setText(String.valueOf(0.000000f));
                    mLongitudeTextView.setText(String.valueOf(0.000000f));
                    mAltitudeTextView.setText(NOT_AVAILABLE);
                    mAccuracyTextView.setText(String.valueOf(0));
                    mAddressTextView.setText(NOT_AVAILABLE);
                }
            }
        }
    };
}
