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

import java.util.Arrays;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 02.04.2020.
 */
public class GPSTrackingSettingsFragment extends android.support.v4.app.Fragment {

    private final static String TAG = "GPSTrackingSettingsFrag";

    private final static int GPS_TRACKING_DISTANCE_MIN = 1;
    private final static int GPS_TRACKING_DISTANCE_MAX = 100;

    private TextView txtGPSTrackingInterval;
    private ImageButton imgGPSTrackingMinDistanceDec;
    private TextView txtGPSTrackingMinDistance;
    private ImageButton imgGPSTrackingMinDistanceInc;

    private View mView;
    private Context mContext;
    private Bundle mSavedInstanceState = null;


    public static interface OnGPSTrackingSettingsFragmentCompleteListener { public abstract void onGPSTrackingSettingsFragmentComplete(); }
    private GPSTrackingSettingsFragment.OnGPSTrackingSettingsFragmentCompleteListener mListener;

    private int mInterval;
    public int getInterval() {
        mInterval = Integer.valueOf(txtGPSTrackingInterval.getText().toString());
        return mInterval;
    }
    public void setInterval(int interval) {
        this.mInterval = interval;
        txtGPSTrackingInterval.setText(String.valueOf(mInterval));
    }

    private int mMinDistance;
    public int getMinDistance() {
        mMinDistance = Integer.valueOf(txtGPSTrackingMinDistance.getText().toString());
        return mMinDistance;
    }
    public void setMinDistance(int minDistance) {
        this.mMinDistance = minDistance;
        txtGPSTrackingMinDistance.setText(String.valueOf(mMinDistance));
    }


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
        mView = inflater.inflate(R.layout.frag_settings_gpstracking, container, false);
        return mView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        txtGPSTrackingInterval = mView.findViewById(R.id.txtGPSTrackingInterval);
        imgGPSTrackingMinDistanceDec = (ImageButton) mView.findViewById(R.id.btnGPSMinDistanceDec);
        imgGPSTrackingMinDistanceDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(txtGPSTrackingMinDistance.getText().toString());
                if(value>GPS_TRACKING_DISTANCE_MIN) { value--; }
                txtGPSTrackingMinDistance.setText(String.valueOf(value));
            }
        });
        txtGPSTrackingMinDistance = mView.findViewById(R.id.txtGPSTrackingMinDistance);
        imgGPSTrackingMinDistanceInc = (ImageButton) mView.findViewById(R.id.btnGPSMinDistanceInc);
        imgGPSTrackingMinDistanceInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(txtGPSTrackingMinDistance.getText().toString());
                if(value<GPS_TRACKING_DISTANCE_MAX) { value++; }
                txtGPSTrackingMinDistance.setText(String.valueOf(value));
            }
        });
        mSavedInstanceState = savedInstanceState;
        mListener.onGPSTrackingSettingsFragmentComplete();
    }


    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mListener = (GPSTrackingSettingsFragment.OnGPSTrackingSettingsFragmentCompleteListener)context;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnGPSTrackingSettingsFragmentCompleteListener");
        }
    }
}