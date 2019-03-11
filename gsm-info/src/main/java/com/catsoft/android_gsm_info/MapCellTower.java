package com.catsoft.android_gsm_info;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 06.02.2019.
 */
public class MapCellTower extends CellTower {

    private LatLng mLatLng;
    public LatLng getLatLong() { return mLatLng; }
    public void setLatLong(LatLng mLatLng) { this.mLatLng = mLatLng; }

    private String mTitle;
    public String getTitle() { return mTitle; }
    public void setTitle(String mTitle) { this.mTitle = mTitle; }

    private BitmapDescriptor mIcon;
    public BitmapDescriptor getIcon() { return mIcon; }
    public void setIcon(BitmapDescriptor mIcon) { this.mIcon = mIcon; }

    private int mCircleColor;
    public int getCircleColor() { return mCircleColor; }
    public void setCircleColor(int mCircleColor) { this.mCircleColor = mCircleColor; }

    public MapCellTower() {
        super();
    }

    public MapCellTower(int cid, int lac, int mcc, int mnc, String networkType, String providerName, float latitude, float longitude, float altitude, int accuracy, String address) {
        super(cid, lac, mcc, mnc, networkType, providerName, latitude, longitude, altitude, accuracy, address);
        this.mLatLng = new LatLng(latitude, longitude);
    }

    public MapCellTower(CellTower cell) {
        super(cell.getCId(), cell.getLac(), cell.getMCC(), cell.getMNC(), cell.getNetworkType(), cell.getProviderName(), cell.getLocation());
        if(this.getLocation()!=null) {
            this.mLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        }
    }
}