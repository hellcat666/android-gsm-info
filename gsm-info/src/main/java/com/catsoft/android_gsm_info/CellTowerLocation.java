package com.catsoft.android_gsm_info;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 27.03.2018.
 */

public class CellTowerLocation implements Parcelable {

    private static final String TAG="GSMInfo-CellTowerLocation";

    private static final String NOT_AVAILABLE = "n.a";

    private Map<String, String> mHashMap = new HashMap<String, String>();
    private String mRawLocation = null;

    private float mLatitude = -1;
    private float mLongitude = -1;
    private float mAltitude = -1;
    private int mAccuracy = -1;
    private String mAddress = null;

    public CellTowerLocation() {
        mLatitude = 0.000000f;
        mLongitude = 0.000000f;
        mAltitude = 0.000000f;
        mAccuracy = 0;
        mAddress = null;
    }

    public CellTowerLocation(String locationInfo) {
        mRawLocation = locationInfo.substring(1,locationInfo.length()-1).replace("\"", "");
        extract();
    }

    public CellTowerLocation(float latitude, float longitude, float altitude, int accuracy, String address) {
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setAltitude(altitude);
        this.setAccuracy(accuracy);
        this.setAddress(address);
    }

    public CellTowerLocation(CellTowerLocation location) {
        if(location!=null) {
            this.setLatitude(location.getLatitude());
            this.setLongitude(location.getLongitude());
            this.setAltitude(location.getAltitude());
            this.setAccuracy(location.getAccuracy());
            this.setAddress(location.getAddress());
        }
    }

    // The following method is based upon Location Web Service supplied by unwiredlabs.com
    protected void extract() {
    int i, j;
        mHashMap = new HashMap<String, String>();
        // split on ','
        String[] items = mRawLocation.split(",");

        // Get all the information but the address
        for (i=0; i<5; i++) {
            // For each item, split on ':'
            String[] parts = items[i].split(":");
            mHashMap.put(parts[0], parts[1]);
            if(parts[1].equals("error")) {
                this.setLatitude(0.000000f);
                this.setLongitude(0.000000f);
                this.setAccuracy(0);
                this.setAddress("n.a");
                return;
            }
        }

        // Get the 1st part of the address
        String[] parts = items[i++].split(":");

        // Add the rest of the address
        for(j=i; j<items.length; j++) {
            if(j<items.length-1) {
                parts[1] = parts[1].concat(items[j]).concat(", ");
            }
            else {
                parts[1] = parts[1].concat(items[j]);
            }
        }
        mHashMap.put(parts[0], parts[1]);

        // Set Latitude, Longitude, Accuracy and Address
        this.setLatitude(Float.valueOf(mHashMap.get("lat")));
        this.setLongitude(Float.valueOf(mHashMap.get("lon")));
        this.setAccuracy(Integer.valueOf(mHashMap.get("accuracy")));
        this.setAddress(mHashMap.get("address"));
    }

    public static final Creator<CellTowerLocation> CREATOR = new Creator<CellTowerLocation>() {
        @Override
        public CellTowerLocation createFromParcel(Parcel in) {
            return new CellTowerLocation(in);
        }

        @Override
        public CellTowerLocation[] newArray(int size) {
            return new CellTowerLocation[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(getLatitude());
        dest.writeFloat(getLongitude());
        dest.writeFloat(getAltitude());
        dest.writeInt(getAccuracy());
        dest.writeString(getAddress());
    }

    protected CellTowerLocation(Parcel in) {
        this.setLatitude(in.readFloat());
        this.setLongitude(in.readFloat());
        this.setAltitude(in.readFloat());
        this.setAccuracy(in.readInt());
        this.setAddress(in.readString());
    }

    public float getLatitude() {
        return mLatitude;
    }

    public void setLatitude(float latitude) {
        this.mLatitude = latitude;
    }

    public float getLongitude() {
        return mLongitude;
    }

    public void setLongitude(float longitude) {
        this.mLongitude = longitude;
    }

    public float getAltitude() {
        return mAltitude;
    }

    public void setAltitude(float altitude) {
        this.mAltitude = altitude;
    }

    public int getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(int accuracy) {
        this.mAccuracy = accuracy;
    }

    public String getAddress() {
        return mAddress;
    }

    /**
     * This Helper function converts unicode character codes of the given Address String
     * with human readable characters
     *
     * @param address       // String to convert
     */
    public void setAddress(String address) {
        if((address!=null) && (!address.isEmpty())) {
            this.mAddress = address.replace("\\u00c9", "É")
                    .replace("\\u00e0", "à")
                    .replace("\\u00e1", "á")
                    .replace("\\u00e2",	"â")
                    .replace("\\u00e3", "ã")
                    .replace("\\u00e4", "ä")
                    .replace("\\u00e5", "å")
                    .replace("\\u00e6", "æ")
                    .replace("\\u00e7", "ç")
                    .replace("\\u00e8", "è")
                    .replace("\\u00e9", "é")
                    .replace("\\u00ea", "ê")
                    .replace("\\u00eb", "ë")
                    .replace("\\u00ec", "ì")
                    .replace("\\u00ed", "í")
                    .replace("\\u00ee", "î")
                    .replace("\\u00ef", "ï")
                    .replace("\\u00f0", "ð")
                    .replace("\\u00f1", "ñ")
                    .replace("\\u00f2", "ò")
                    .replace("\\u00f3", "ó")
                    .replace("\\u00f4", "ô")
                    .replace("\\u00f5", "õ")
                    .replace("\\u00f6", "ö")
                    .replace("\\u00f8", "ø")
                    .replace("\\u00f9", "ù")
                    .replace("\\u00fa", "ú")
                    .replace("\\u00fb", "û")
                    .replace("\\u00fc", "ü")
                    .replace("\\u00fd", "ý")
                    .replace("\\u00fe", "þ")
                    .replace("\\u00ff", "ÿ");
        }
        else {
            this.mAddress = NOT_AVAILABLE;
        }
    }
}
