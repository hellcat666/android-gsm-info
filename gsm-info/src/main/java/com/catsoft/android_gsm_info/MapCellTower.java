package com.catsoft.android_gsm_info;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 06.02.2019.
 */
public class MapCellTower extends CellTower implements Parcelable {

    private static final String TAG = "GSMInfo-MapCellTower";

    private static final String mActiveTitle = "Current BTS Info";
    private static final String mInactiveTitle = "BTS Info";

    private static final int mActiveCircleColor = 0x15600000;    // 0x15ff0000;
    private static final int mInactiveCircleColor = 0x15606060;  // 0x15808080;

    private String mTitle;
    public String getTitle() { return mTitle; }
    public void setTitle(String title) { this.mTitle = title; }
    private void setTitle() {
//        Log.i(TAG, "MapCellTower.setTitle() - CId:" + String.valueOf(this.getCId()));
        if(mActive)
            mTitle = mActiveTitle;
        else
            mTitle = mInactiveTitle;
    }

    private BitmapDescriptor mIcon;
    public BitmapDescriptor getIcon() { return mIcon; }
    public void setIcon(BitmapDescriptor icon) { this.mIcon = icon; }

    private void setIcon() {
//        Log.i(TAG, "MapCellTower.setIcon() - CId:" + String.valueOf(this.getCId()));
        if(mActive)
            mIcon = mIconActiveBTS;
        else
            mIcon = mIconInactiveBTS;
    }

    private BitmapDescriptor mIconActiveBTS = BitmapDescriptorFactory.fromResource(R.drawable.active_bts);
    private BitmapDescriptor mIconInactiveBTS = BitmapDescriptorFactory.fromResource(R.drawable.inactive_bts);

    private LatLng mLatLng;
    public LatLng getLatLong() { return mLatLng; }
    public void setLatLong(LatLng latLng) { this.mLatLng = latLng; }

    private int mCircleColor = mInactiveCircleColor;
    public int getCircleColor() { return mCircleColor; }
    private void setCircleColor() {
//        Log.i(TAG, "MapCellTower.setCircleColor() - CId:" + String.valueOf(this.getCId()));
        if (mActive)
            mCircleColor = mActiveCircleColor;
        else
            mCircleColor = mInactiveCircleColor;
    }

    private void setCircleColor(int color) {
        mCircleColor = color;
    }

    private boolean mActive;
    public boolean isActive() { return mActive; }
    public void setActive(boolean active) {
//        Log.i(TAG, "MapCellTower.setActive() - CId:" + String.valueOf(this.getCId()));
        this.mActive = active;
        setAttributes();
    }

    public MapCellTower() {
        super();
        setAttributes();
    }

    public MapCellTower(int cid, int lac, int mcc, int mnc, String networkType, String providerName, float latitude, float longitude, float altitude, int accuracy, String address) {
        super(cid, lac, mcc, mnc, networkType, providerName, latitude, longitude, altitude, accuracy, address);
        this.mLatLng = new LatLng(latitude, longitude);
        setAttributes();
    }

    public MapCellTower(CellTower cell) {
        super(cell.getCId(), cell.getLac(), cell.getMCC(), cell.getMNC(), cell.getNetworkType(), cell.getProviderName(), cell.getLocation());
        if(this.getLocation()!=null) {
            this.mLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        }
        setAttributes();
    }

    private void setAttributes() {
        setTitle();
        setIcon();
        setCircleColor();
    }

    /**
     * Parcelable Implementation
     */
    public static final Creator<MapCellTower> CREATOR = new Creator<MapCellTower>() {
        @Override
        public MapCellTower createFromParcel(Parcel in) {
            return new MapCellTower(in);
        }

        @Override
        public MapCellTower[] newArray(int size) {
            return new MapCellTower[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getTitle());
        dest.writeParcelable(mLatLng, 0);
        dest.writeInt(getCircleColor());
        dest.writeByte(isActive() ? (byte)1 : (byte)0);
    }

    protected MapCellTower(Parcel in) {
        this.setTitle(in.readString());
        this.setLatLong((LatLng)in.readParcelable(LatLng.class.getClassLoader()));
        this.setCircleColor(in.readInt());
        this.setActive(in.readByte()>0 ? true : false);
    }
}