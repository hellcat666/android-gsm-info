package com.catsoft.android_gsm_info;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 27/03/2018.
 *
 * Last Modification: HellCat on 09/07/2018
 *      - Comments added
 */

/**
 *  This class encapsulates CellTower data
 *  It provides attributes accessors (getter/setter) as well as helper functions
 *  It implements Parcelable interface, allowing data serialization (Bundle)
 */
public class CellTower implements Parcelable {

    private static final String TAG = "GSMInfo-CellTower";

    private static final String NOT_AVAILABLE = "n.a";

    // Attributes
    protected int mCId = -1;                  // CellTower ID
    protected int mLac = -1;                  // Location Area Code
    protected int mMCC = -1;                  // Mobile Country Code
    protected int mMNC = -1;                  // Mobile Network Code
    protected String mNetworkType = NOT_AVAILABLE;
    protected String mProviderName = NOT_AVAILABLE;

    protected CellTowerLocation mLocation;    // Not yet used

    protected String mInfo = "";              // Information

//    protected boolean mActive = false;

    /**
     * CellTower default Constructor
     *
     */
    public CellTower() {

    }

    /**
     * CellTower Contructor #1
     *
     * @param cid           // CellTower Id
     * @param lac           // Location Area Code
     * @param mcc           // Mobile Country Code
     * @param mnc           // Mobile Network Code
     * @param networkType   // Network Type (e.g UMTS)
     * @param providerName  // Provider Name (e.g Sunrise)
     */
    public CellTower(int cid, int lac, int mcc, int mnc, String networkType, String providerName) {
        setCId(cid);
        setLac(lac);
        setMCC(mcc);
        setMNC(mnc);
        setNetworkType(networkType);
        setProviderName(providerName);
        mLocation = new CellTowerLocation();
    }

    /**
     * CellTower Constructor #2
     * Same as Constructor #1 but with additional Location Info
     *
     * @param cid           // CellTower Id
     * @param lac           // Location Area Code
     * @param mcc           // Mobile Country Code
     * @param mnc           // Mobile Network Code
     * @param networkType   // Network Type (e.g UMTS)
     * @param providerName  // Provider Name (e.g Sunrise)
     * @param latitude      // Latitude
     * @param longitude     // Longitude
     * @param altitude      // Altitude
     * @param accuracy      // Accuracy
     * @param address       // Address (human readable ;) )
     */
    public CellTower(int cid, int lac, int mcc, int mnc, String networkType, String providerName, float latitude, float longitude, float altitude, int accuracy, String address) {
        setCId(cid);
        setLac(lac);
        setMCC(mcc);
        setMNC(mnc);
        setNetworkType(networkType);
        setProviderName(providerName);
        mLocation = new CellTowerLocation(latitude, longitude, altitude, accuracy, address);
    }

    public CellTower(int cid, int lac, int mcc, int mnc, String networkType, String providerName, CellTowerLocation location) {
        setCId(cid);
        setLac(lac);
        setMCC(mcc);
        setMNC(mnc);
        setNetworkType(networkType);
        setProviderName(providerName);
        if(location!=null) { mLocation = new CellTowerLocation(location); }
    }

    /**
     * Compare this CellTower with the given one
     *
     * @param   celltower       Given CellTower to compare with
     * @return                  True / False
     */
    public boolean equals(CellTower celltower) {
        CellTower cell = celltower;
        if(cell!=null) {
            boolean bRet = (this.mCId == cell.getCId()) && (this.mLac == cell.getLac()) && (this.mMCC == cell.getMCC()) && (this.mMNC == cell.getMNC());
            return bRet == true;
        }
        else {
            return false;
        }
    }

    /**
     * This Helper function checks if current CellTower Location is defined
     *
     * @return  True / False
     */
    public boolean hasLocation() {
        return ((mLocation!=null) && mLocation.getLatitude() != 0.000000f) && (mLocation.getLongitude() != 0.000000f) && (mLocation.getAccuracy() != 0);
    }

    /**
     * This Helper function returns CellTower Data as a single formatted String
     *
     * @return  CellTower Info String
     */
    public String getInfo() {
        mInfo =   "cid: " + String.valueOf(getCId()) + " / lac: " + String.valueOf(getLac()) + "\n"
                + "mcc: " + String.valueOf(getMCC()) + " / mnc: " + String.valueOf(getMNC()) + "\n"
                + "address:\n " + mLocation.getAddress();
        return mInfo;
    }

    /**
     * Parcelable Implementation
     */
    public static final Creator<CellTower> CREATOR = new Creator<CellTower>() {
        @Override
        public CellTower createFromParcel(Parcel in) {
            return new CellTower(in);
        }

        @Override
        public CellTower[] newArray(int size) {
            return new CellTower[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getCId());
        dest.writeInt(getLac());
        dest.writeInt(getMCC());
        dest.writeInt(getMNC());
        dest.writeString(getNetworkType());
        dest.writeString(getProviderName());
        dest.writeParcelable(mLocation, 0);
//        dest.writeByte(isActive() ? (byte)1 : (byte)0);
    }

    protected CellTower(Parcel in) {
        this.setCId(in.readInt());
        this.setLac(in.readInt());
        this.setMCC(in.readInt());
        this.setMNC(in.readInt());
        this.setNetworkType(in.readString());
        this.setProviderName(in.readString());
        mLocation = in.readParcelable(CellTowerLocation.class.getClassLoader());
//        this.setActive(in.readByte()>0 ? true : false);
    }


    /**
     * CellTower Attributes Getter / Setter
     *
     */
    public int getCId() {
        return mCId;
    }
    public void setCId(int cid) {
        this.mCId = cid;
    }

    public int getLac() {
        return mLac;
    }
    public void setLac(int lac) {
        this.mLac = lac;
    }

    public int getMCC() {
        return mMCC;
    }
    public void setMCC(int mcc) {
        this.mMCC = mcc;
    }

    public int getMNC() {
        return mMNC;
    }
    public void setMNC(int mnc) {
        this.mMNC = mnc;
    }

    public String getNetworkType() {
        return mNetworkType;
    }
    public void setNetworkType(String networkType) {
        this.mNetworkType = networkType;
    }

    public String getProviderName() {
        return mProviderName;
    }
    public void setProviderName(String providerName) {
        this.mProviderName = providerName;
    }

    public CellTowerLocation getLocation() { return mLocation; }
    public void setLocation(CellTowerLocation mLocation) { this.mLocation = mLocation; }
}
