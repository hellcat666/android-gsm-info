package com.catsoft.android_gsm_info;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 01.04.2020.
 */
public class UnwiredLabsSettingsFragment extends android.support.v4.app.Fragment {

    private final static String TAG = "UnwiredLabsSettingsFrag";

    private TextView txtUnwiredLabsUrl;
    private String mUrl;
    public String getUrl() {
        mUrl = txtUnwiredLabsUrl.getText().toString();
        return mUrl;
    }
    public void setUrl(String url) {
        mUrl = url;
        txtUnwiredLabsUrl.setText(mUrl);
    }

    private TextView txtUnwiredLabsToken;
    private String mToken;
    public String getToken() {
        mToken = txtUnwiredLabsToken.getText().toString();
        return mToken;
    }
    public void setToken(String token) {
        mToken = token;
        txtUnwiredLabsToken.setText(mToken);
    }

    private View mView;
    private Context mContext;
    private Bundle mSavedInstanceState = null;
    private IntentFilter mAppFilter = null;

    public static interface OnUnwiredLabsSettingsFragmentCompleteListener { public abstract void onUnwiredLabsSettingsFragmentComplete(); }
    private OnUnwiredLabsSettingsFragmentCompleteListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"onCreate()");
        super.onCreate(savedInstanceState);
        mContext = this.getContext();
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_settings_unwiredlabs, container, false);
        return mView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        txtUnwiredLabsUrl = getActivity().findViewById(R.id.txtUnwiredLabsUrl);
        txtUnwiredLabsToken = getActivity().findViewById(R.id.txtUnwiredLabsToken);
        mSavedInstanceState = savedInstanceState;
        mListener.onUnwiredLabsSettingsFragmentComplete();
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mListener = (OnUnwiredLabsSettingsFragmentCompleteListener)context;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnUnwiredLabsSettingsFragmentCompleteListener");
        }
    }
}
