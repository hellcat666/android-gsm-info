package com.catsoft.android_gsm_info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 15.04.2018.
 */

/**
 * This Activity is the Main Activity.
 * It is organized as follows:
 *  - CellTowerInfo Fragment
 *    This is where are displayed the information related to the current CellTower (the one we are connected to)
 *
 *  - CellTowersMap Fragment
 *    This is where are displayed the CellTowers currently defined in the Database
 *    Each CellTower is displayed as an icon on the Google Map layer
 *    A touch of a particular CellTower icon causes a popup window to open,
 *    displaying related information such as:
 *    . CId
 *    . Lac
 *    . MCC
 *    . MNC
 *    . Latitude
 *    . Longitude
 *    . Full Address
 *
 */
public class MapActivity  extends AppCompatActivity implements CellTowerInfoFragment.OnCellTowerInfoFragmentCompleteListener, CellTowersMapFragment.OnCellTowersMapFragmentCompleteListener {

    private static final String TAG = "MapActivity";

    private static final String MAP_ACTIVITY_READY = "map-activity-ready";
    private static final String ACTIVITY_READY = "activity-ready";
    private static final String EXIT = StartUpActivity.EXIT;

    private Context mContext;
    private Intent mIntent;

    Toolbar mToolbar = null;

    private AppSettings mAppSettings = null;

    private ImageButton mBtnBack = null;
    private ImageButton mBtnExit = null;

    private CellTowerInfoFragment mCellTowerInfoFragment = null;

    private CellTowersMapFragment mCellTowersMapFragment = null;

    private Thread mFragmentThread = null;

    private IntentFilter mAppFilter = null;

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "MapActivity.onCreate()");
        super.onCreate(savedInstanceState);
        switch(this.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setContentView(R.layout.activity_map);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setContentView(R.layout.activity_map);
                break;
        }

        mContext = this.getApplicationContext();

        mIntent = getIntent();


        initToolBar();
        initButtons();
        initFragments();
        initSettings();
     }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.app_bar);    // Attaching the layout to the toolbar object
        mToolbar.setTitle(R.string.title_appbar_gsm_info);
        setSupportActionBar(mToolbar);                              // Setting toolbar as the ActionBar with setSupportActionBar() call
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        mToolbar.inflateMenu(R.menu.app_menu);
        if (mToolbar != null) {
            mToolbar.setVisibility(View.VISIBLE);
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.menu_settings:
                            Log.i(TAG, "AppSettings MenuItem clicked");
                            startSettings();
                            break;
                        case R.id.menu_exit:
                            Log.i(TAG, "Exit MenuItem clicked");
                            exit();
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onCellTowerInfoFragmentComplete() {
        if(mAppSettings!=null)
            mCellTowerInfoFragment.setNetworkType(mAppSettings.getCellTowerLocationServiceNetworkType());
    }

    @Override
    public void onCellTowersMapFragmentComplete() {
        if(mAppSettings!=null) {
            mCellTowersMapFragment.setMapType((int) mAppSettings.getGoogleMapMapType());
            mCellTowersMapFragment.setDefaultZoomLevel(mAppSettings.getGoogleMapDefaultZoom());
        }
    }

    private void initButtons() {
        /*
        mBtnBack = findViewById(R.id.btn_back);
        if (mBtnBack != null) {
            mBtnBack.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        mBtnBack.setVisibility(View.INVISIBLE);

        mBtnExit = findViewById(R.id.btnExit);
        if (mBtnExit != null) {
            mBtnExit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    exit();
                }
            });
        }
        mBtnExit.setVisibility(View.VISIBLE);
        */
    }

    private void initFragments() {
        mCellTowerInfoFragment = new CellTowerInfoFragment();
        if(mCellTowerInfoFragment!=null) {
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.celltower_info_short, mCellTowerInfoFragment, "CELLTOWER_INFO_FRAGMENT").
                    commit();
        }
        mCellTowersMapFragment = new CellTowersMapFragment();
        if(mCellTowersMapFragment!=null) {
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.celltowers_map, mCellTowersMapFragment, "CELLTOWERS_MAP_FRAGMENT").
                    commit();
        }
    }

    private void initSettings() {
        if(mIntent!=null)
            if(mIntent.getExtras()!=null)
                mAppSettings = (AppSettings)mIntent.getExtras().get(SettingsActivity.APP_SETTINGS);
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
                case ACTIVITY_READY:
                    if(mFragmentThread.isAlive()) { mFragmentThread.interrupt(); }
                    Intent anIntent = new Intent();
                    anIntent.setAction(MAP_ACTIVITY_READY);
                    mContext.sendBroadcast(anIntent);
                    break;
                default:
                    break;
            }
        }
    };

    private void startSettings() {
        Intent anIntent = new Intent(this, SettingsActivity.class);
        anIntent.putExtra(SettingsActivity.APP_SETTINGS, mAppSettings);
        anIntent.putExtra("parent", this.getClass());
        this.startActivity(anIntent);
    }

    private void exit() {
        Intent anIntent = new Intent();
        anIntent.setAction(EXIT);
        mContext.sendBroadcast(anIntent);
        finish();
    }

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

}
