package com.catsoft.android_gsm_info;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 01.04.2020.
 */
public class GoogleMapSettingsFragment extends android.support.v4.app.Fragment {

    private final static String TAG = "GoogleMapSettingsFrag";

    private final static int GOOGLE_MAP_ZOOM_MIN = 0;
    private final static int GOOGLE_MAP_ZOOM_MAX = 18;


    private TextView txtGoogleMapAPIKey;

    private Spinner spGoogleMapTypes;
//    private List<String> mGoogleMapTypesList;
    private ImageButton imgGoogleMapZoomDec;
    private boolean mDecrementing;

    private TextView txtGoogleMapDefaultZoom;
    private ImageButton imgGoogleMapZoomInc;
    private boolean mIncrementing;

    private View mView;
    private Context mContext;
    private Bundle mSavedInstanceState = null;


    public static interface OnGoogleMapSettingsFragmentCompleteListener { public abstract void onGoogleMapSettingsFragmentComplete(); }
    private GoogleMapSettingsFragment.OnGoogleMapSettingsFragmentCompleteListener mListener;

    private String mAPIKey;
    public String getAPIKey() {
        mAPIKey = txtGoogleMapAPIKey.getText().toString();
        return mAPIKey;
    }
    public void setAPIKey(String apiKey) {
        mAPIKey = apiKey;
        txtGoogleMapAPIKey.setText(mAPIKey);
    }

    private long mMapType;
    public long getMapType() {
        return mMapType;
    }
    public void setMapType(long mapType) {
        mMapType = mapType;
        if(spGoogleMapTypes!=null) spGoogleMapTypes.setSelection((int)mapType);
    }

    private int mDefaultZoom;
    public int getDefaultZoom() {
        mDefaultZoom = Integer.valueOf(txtGoogleMapDefaultZoom.getText().toString());
        return mDefaultZoom;
    }
    public void setDefaultZoom(int defaultZoom) {
        mDefaultZoom = defaultZoom;
        txtGoogleMapDefaultZoom.setText(String.valueOf(mDefaultZoom));
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
        mView = inflater.inflate(R.layout.frag_settings_googlemap, container, false);
        return mView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        txtGoogleMapAPIKey = mView.findViewById(R.id.txtGoogleMapKey);
        spGoogleMapTypes = mView.findViewById(R.id.spinGoogleMapTypes);
        spGoogleMapTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                    Log.i(TAG, item.toString() + " selected");
                    setMapType(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing TODO
            }
        });

        spGoogleMapTypes.setSelection((int)mMapType);
//        mGoogleMapTypesList = Arrays.asList(getResources().getStringArray(R.array.googleMapTypesValues));
        imgGoogleMapZoomDec = (ImageButton) mView.findViewById(R.id.btnGoogleMapZoomDec);
        imgGoogleMapZoomDec.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(txtGoogleMapDefaultZoom.getText().toString());
                if(value>GOOGLE_MAP_ZOOM_MIN) { value--; }
                txtGoogleMapDefaultZoom.setText(String.valueOf(value));
            }
        });
        txtGoogleMapDefaultZoom = getActivity().findViewById(R.id.txtGoogleMapDefaultZoom);
        imgGoogleMapZoomInc = (ImageButton) mView.findViewById(R.id.btnGoogleMapZoomInc);
        imgGoogleMapZoomInc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(txtGoogleMapDefaultZoom.getText().toString());
                if(value<GOOGLE_MAP_ZOOM_MAX) { value++; }
                txtGoogleMapDefaultZoom.setText(String.valueOf(value));
            }
        });

        mSavedInstanceState = savedInstanceState;
        mListener.onGoogleMapSettingsFragmentComplete();
    }


    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mListener = (GoogleMapSettingsFragment.OnGoogleMapSettingsFragmentCompleteListener)context;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnGoogleMapSettingsFragmentCompleteListener");
        }
    }
}