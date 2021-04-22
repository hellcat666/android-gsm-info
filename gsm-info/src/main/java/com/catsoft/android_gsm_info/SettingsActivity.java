package com.catsoft.android_gsm_info;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 01.04.2020.
 */
public class SettingsActivity extends AppCompatActivity implements UnwiredLabsSettingsFragment.OnUnwiredLabsSettingsFragmentCompleteListener,
        GoogleMapSettingsFragment.OnGoogleMapSettingsFragmentCompleteListener,
        GPSTrackingSettingsFragment.OnGPSTrackingSettingsFragmentCompleteListener {

    private static final String TAG = "SettingsActivity";

    public static final String APP_SETTINGS = "app_settings";
    private static final String ACTIVITY_READY = "activity-ready";
    public static final String SETTINGS_READY = "settings-ready";

    private Context mContext;
    private Intent mIntent = null;

    private Toolbar mToolbar = null;

    private AppSettings mAppSettings = null;

    private UnwiredLabsSettingsFragment mUnwiredLabsSettingsFragment = null;

    private GoogleMapSettingsFragment mGoogleMapSettingsFragment = null;

    private GPSTrackingSettingsFragment mGPSTrackingSettingsFragment = null;

    private CellTowerServiceSettingsFragment mCellTowerServiceSettingsFragment = null;

    private Button btnSaveSettings = null;

    private Thread mFragmentThread = null;

    private IntentFilter mAppFilter = null;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch(this.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setContentView(R.layout.activity_settings);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setContentView(R.layout.activity_settings);
                break;
        }

        mContext = this.getApplicationContext();
        mIntent = getIntent();

        initToolBar();
        initFragments();
        initControls();
        initSettings();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        unregisterReceiver();
        super.onPause();
    }

    @Override
    protected void onDestroy() { super.onDestroy(); }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.app_bar);    // Attaching the layout to the toolbar object
        mToolbar.setTitle(R.string.title_appbar_gsm_info);
        setSupportActionBar(mToolbar);                      // Setting toolbar as the ActionBar with setSupportActionBar() call
        if(mIntent.getExtras().get("parent").equals(StartUpActivity.class)) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
        else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    private void initFragments() {
        mUnwiredLabsSettingsFragment = new UnwiredLabsSettingsFragment();
        if(mUnwiredLabsSettingsFragment!=null) {
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.settings_unwiredlabs, mUnwiredLabsSettingsFragment, "UNWIREDLABS_SETTINGS_FRAGMENT").
                    commit();
        }

        mGoogleMapSettingsFragment = new GoogleMapSettingsFragment();
        if(mGoogleMapSettingsFragment!=null) {
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.settings_googlemap, mGoogleMapSettingsFragment, "GOOGLEMAP_SETTINGS_FRAGMENT").
                    commit();
        }

        mGPSTrackingSettingsFragment = new GPSTrackingSettingsFragment();
        if(mGoogleMapSettingsFragment!=null) {
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.settings_gpstracking, mGPSTrackingSettingsFragment, "GPSTRACKING_SETTINGS_FRAGMENT").
                    commit();
        }

        mCellTowerServiceSettingsFragment = new CellTowerServiceSettingsFragment();
        if(mGoogleMapSettingsFragment!=null) {
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.settings_cell_tower_service, mCellTowerServiceSettingsFragment, "GPSTRACKING_SETTINGS_FRAGMENT").
                    commit();
        }

    }

    private void initControls() {
        btnSaveSettings = this.findViewById(R.id.btnSaveSettings);
        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Saving AppSettings ...");
                Intent anIntent;
                mAppSettings.setUnwiredLabsUrl(mUnwiredLabsSettingsFragment.getUrl());
                mAppSettings.setUnwiredLabsToken(mUnwiredLabsSettingsFragment.getToken());
                mAppSettings.setGoogleMapAPIKey(mGoogleMapSettingsFragment.getAPIKey());
                mAppSettings.setGoogleMapMapType(mGoogleMapSettingsFragment.getMapType());
                mAppSettings.setGoogleMapDefaultZoom(mGoogleMapSettingsFragment.getDefaultZoom());
                mAppSettings.setGPSTrackingInterval(mGPSTrackingSettingsFragment.getInterval());
                mAppSettings.setGPStrackingMinDistance(mGPSTrackingSettingsFragment.getMinDistance());
                mAppSettings.setCellTowerLocationServiceNetworkType(mCellTowerServiceSettingsFragment.getNetworkType());
                mAppSettings.save();
                if(mIntent.getExtras().get("parent").equals(StartUpActivity.class)) {
                    anIntent = new Intent(getApplicationContext(), StartUpActivity.class);
                }
                else {
                    anIntent = new Intent(getApplicationContext(), MapActivity.class);
                }
                anIntent.putExtra(APP_SETTINGS, mAppSettings);
                startActivity(anIntent);
            }
        });
    }

    private void initSettings() {
        // App. Settings not already registered
        if((mIntent!=null) && (mIntent.getExtras()!=null)) {
            mAppSettings = (AppSettings)mIntent.getExtras().get("app_settings");
        }
        // App. Settings already registered
        else {
            mAppSettings = new AppSettings(mContext);
        }
//        mCellTowerServiceSettingsFragment.setNetworkType(mAppSettings.getCellTowerLocationServiceNetworkType());
    }

    @Override
    public void onUnwiredLabsSettingsFragmentComplete() {
        if(mAppSettings!=null) {
            mUnwiredLabsSettingsFragment.setUrl(mAppSettings.getUnwiredLabsUrl());
            mUnwiredLabsSettingsFragment.setToken(mAppSettings.getUnwiredLabsToken());
        }
    }

    @Override
    public void onGoogleMapSettingsFragmentComplete() {
        if(mAppSettings!=null) {
            mGoogleMapSettingsFragment.setAPIKey(mAppSettings.getGoogleMapAPIKey());
            mGoogleMapSettingsFragment.setMapType(mAppSettings.getGoogleMapMapType());
            mGoogleMapSettingsFragment.setDefaultZoom(mAppSettings.getGoogleMapDefaultZoom());
        }
    }

    @Override
    public void onGPSTrackingSettingsFragmentComplete() {
        if(mAppSettings!=null) {
            mGPSTrackingSettingsFragment.setInterval(mAppSettings.getGPSTrackingInterval());
            mGPSTrackingSettingsFragment.setMinDistance(mAppSettings.getGPStrackingMinDistance());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
                break;
            case KeyEvent.KEYCODE_HOME:
                break;
        }
        return false;
    }

    private void registerReceiver() {
//        Log.i(TAG, "registerReceiver()");
        mAppFilter = new IntentFilter();
        mAppFilter.addAction(ACTIVITY_READY);
        mContext.registerReceiver(mMessageReceiver, mAppFilter);
    }

    private void unregisterReceiver() {
//        Log.i(TAG, "unregisterReceiver()");
        mContext.unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null) { return; }

            switch(intent.getAction()) {
                default:
                    break;
            }
        }
    };
}
