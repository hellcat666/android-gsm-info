package com.catsoft.android_gsm_info;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 03.04.2020.
 */
public class AppSettings implements Parcelable {

    private static final String PREFS_NAME = "AppSettings";
    private static final int  MODE_PRIVATE = 0;


    private static SharedPreferences mSharedPreferences = null;
    public SharedPreferences getSharedPreferences() { return mSharedPreferences; }

    private static final String UNWIREDLABS_URL = "service_url";
    private static final String UNWIREDLABS_TOKEN = "token";
    private static final String GOOGLEMAP_API_KEY = "googlemap_api_key";
    private static final String GOOGLEMAP_MAP_TYPE = "googlemap_map_type";
    private static final String GOOGLEMAP_DEFAULT_ZOOM = "googlemap_default_zoom";
    private static final String GPSTRACKING_INTERVAL = "gpstracking_interval";
    private static final String GPSTRACKING_MIN_DISTANCE = "gpstracking_min_distance";
    private static final String CELLTOWER_LOCATION_SERVICE_NETWORK_TYPE = "celltower_service_network_type";

    Context mContext;

    // UnwiredLabs Service Settings
    private String mUnwiredLabsUrl;
    public String getUnwiredLabsUrl() { return mUnwiredLabsUrl; }
    public void setUnwiredLabsUrl(String mUnwiredLabsUrl) { this.mUnwiredLabsUrl = mUnwiredLabsUrl; }

    private String mUnwiredLabsToken;
    public String getUnwiredLabsToken() { return mUnwiredLabsToken; }
    public void setUnwiredLabsToken(String mUnwiredLabsToken) { this.mUnwiredLabsToken = mUnwiredLabsToken; }


    // GoogleMap Settings
    private String mGoogleMapAPIKey;
    public String getGoogleMapAPIKey() { return mGoogleMapAPIKey; }
    public void setGoogleMapAPIKey(String mGoogleMapAPIKey) { this.mGoogleMapAPIKey = mGoogleMapAPIKey; }

    private long mGoogleMapMapType;
    public long getGoogleMapMapType() { return mGoogleMapMapType; }
    public void setGoogleMapMapType(long mGoogleMapMapType) { this.mGoogleMapMapType = mGoogleMapMapType; }

    private int mGoogleMapDefaultZoom;
    public int getGoogleMapDefaultZoom() { return mGoogleMapDefaultZoom; }
    public void setGoogleMapDefaultZoom(int mGoogleMapDefaultZoom) { this.mGoogleMapDefaultZoom = mGoogleMapDefaultZoom; }


    // GSPTracking Service
    private int mGPSTrackingInterval;
    public int getGPSTrackingInterval() { return mGPSTrackingInterval; }
    public void setGPSTrackingInterval(int mGPSTrackingInterval) { this.mGPSTrackingInterval = mGPSTrackingInterval; }

    private int mGPStrackingMinDistance;
    public int getGPStrackingMinDistance() { return mGPStrackingMinDistance; }
    public void setGPStrackingMinDistance(int mGPStrackingMinDistance) { this.mGPStrackingMinDistance = mGPStrackingMinDistance; }


    // CellTower Location Service
    private int mCellTowerLocationServiceNetworkType;
    public int getCellTowerLocationServiceNetworkType() { return mCellTowerLocationServiceNetworkType; }
    public void setCellTowerLocationServiceNetworkType(int mCellTowerLocationServiceNetworkType) { this.mCellTowerLocationServiceNetworkType = mCellTowerLocationServiceNetworkType; }


    // Default Constructor
    public AppSettings() {}

    public AppSettings(Context context) {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        setUnwiredLabsUrl(mSharedPreferences.getString(UNWIREDLABS_URL, "https://eu1.unwiredlabs.com/v2/process.php"));
        setUnwiredLabsToken(mSharedPreferences.getString(UNWIREDLABS_TOKEN, "9798a2800e6bf9"));
        setGoogleMapAPIKey(mSharedPreferences.getString(GOOGLEMAP_API_KEY, "AIzaSyA76tFsdnLY5RGAbfJY_n8oJ18X2iYjnOI"));
        setGoogleMapMapType(mSharedPreferences.getLong(GOOGLEMAP_MAP_TYPE, 3));
        setGoogleMapDefaultZoom(mSharedPreferences.getInt(GOOGLEMAP_DEFAULT_ZOOM, 13));
        setGPSTrackingInterval(mSharedPreferences.getInt(GPSTRACKING_INTERVAL, 5000));
        setGPStrackingMinDistance(mSharedPreferences.getInt(GPSTRACKING_MIN_DISTANCE, 1));
        setCellTowerLocationServiceNetworkType(mSharedPreferences.getInt(CELLTOWER_LOCATION_SERVICE_NETWORK_TYPE, 0));
    }

    public boolean isAlreadyDefined() {
        return !mSharedPreferences.getAll().isEmpty();
    }

    public void save() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.putString(UNWIREDLABS_URL, getUnwiredLabsUrl());
        editor.putString(UNWIREDLABS_TOKEN, getUnwiredLabsToken());
        editor.putString(GOOGLEMAP_API_KEY, getGoogleMapAPIKey());
        editor.putLong(GOOGLEMAP_MAP_TYPE, getGoogleMapMapType());
        editor.putInt(GOOGLEMAP_DEFAULT_ZOOM, getGoogleMapDefaultZoom());
        editor.putInt(GPSTRACKING_INTERVAL, getGPSTrackingInterval());
        editor.putInt(GPSTRACKING_MIN_DISTANCE, getGPStrackingMinDistance());
        editor.putInt(CELLTOWER_LOCATION_SERVICE_NETWORK_TYPE,getCellTowerLocationServiceNetworkType());
        editor.commit();
    }

    /**
     * Parcelable Implementation
     */
    public static final Creator<AppSettings> CREATOR = new Creator<AppSettings>() {
        @Override
        public AppSettings createFromParcel(Parcel in) {
            return new AppSettings(in);
        }

        @Override
        public AppSettings[] newArray(int size) {
            return new AppSettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getUnwiredLabsUrl());
        dest.writeString(getUnwiredLabsToken());
        dest.writeString(getGoogleMapAPIKey());
        dest.writeLong(getGoogleMapMapType());
        dest.writeInt(getGoogleMapDefaultZoom());
        dest.writeInt(getGPSTrackingInterval());
        dest.writeInt(getGPStrackingMinDistance());
        dest.writeInt(getCellTowerLocationServiceNetworkType());
    }

    protected AppSettings(Parcel in) {
        this.setUnwiredLabsUrl(in.readString());
        this.setUnwiredLabsToken(in.readString());
        this.setGoogleMapAPIKey(in.readString());
        this.setGoogleMapMapType(in.readLong());
        this.setGoogleMapDefaultZoom(in.readInt());
        this.setGPSTrackingInterval(in.readInt());
        this.setGPStrackingMinDistance(in.readInt());
        this.setCellTowerLocationServiceNetworkType(in.readInt());
    }
}
