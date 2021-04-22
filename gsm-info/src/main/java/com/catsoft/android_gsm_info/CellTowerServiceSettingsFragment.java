package com.catsoft.android_gsm_info;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 02.04.2020.
 */
public class CellTowerServiceSettingsFragment extends android.support.v4.app.Fragment {

    private final static String TAG = "CellTowerSvSettingsFrag";

    TextView txtCellTowerServiceNetworkType;

    private View mView;
    private Context mContext;
    private Bundle mSavedInstanceState = null;


    private int mNetworkType;
    public int getNetworkType() { return mNetworkType; }
    public void setNetworkType(int networkType) { this.mNetworkType = networkType; }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        mContext = this.getContext();
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_settings_cell_tower_service, container, false);
        return mView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        txtCellTowerServiceNetworkType = mView.findViewById(R.id.txtNetworkType);
        mSavedInstanceState = savedInstanceState;
    }
}
