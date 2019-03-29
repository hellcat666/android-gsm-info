package com.catsoft.android_gsm_info;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 28.03.2018.
 *
 * Last Modifications: HellCat on 09/07/2018
 *      - Comments added
 */


/**
 * This class encapsulates the CellTower Database.
 * It provides the following Operations:
 *  - Creation (If doesn't already exist)
 *  - Open/Close
 *  - CRUD Operations
 */
public class CellTowerDB  extends SQLiteOpenHelper {

    private static final String TAG = "GSMInfo-CellTowerDB";

    // Constants
    private static final String DB_DIR = "/databases";
    private static String DB_PATH = "/data/data/" + BuildConfig.APPLICATION_ID + DB_DIR;
    private static final String DB_NAME = "celltowers.db";
    private static String DB_FULLPATH = DB_PATH + "/" + DB_NAME;
    private static final String CELLTOWERS_DB_READY = "celltowers-db-ready";
    private static final String EXIT = "exit";

    private static final String CELL = "cell";
    private static final String CELLS = "cells";
    private static final String CELL_INFO = "cell-info";
    private static final String CELLTOWERS_LIST = "celltowers-list";
    private static final String CELL_DETECTED = "cell-detected";
    private static final String TEST_CELL_DETECTED = "test-cell-detected";
    private static final String CELLTOWER_LOCATION = "celltower-location";
    private static final String CELLTOWER_SELECTED = "celltower-selected";
    private static final String REQUEST_CELLTOWER = "request-celltower";
    private static final String REQUEST_CELLTOWERS_LIST = "request-celltowers-list";
    private static final String REQUEST_CELLTOWER_LOCATION = "request-celltower-location";

    private static final String CHECK_SQLITE_VERSION = "SELECT SQLITE_VERSION() AS SQLITE_VERSION";
    private static final int DB_VERSION = 1;

    private static final String TABLE_CELLTOWER = "CELLTOWERS";
    private static final String COL_CID = "CID";
    private static final int COL_CID_IDX = 0;
    private static final String COL_LAC = "LAC";
    private static final int COL_LAC_IDX = 1;
    private static final String COL_MCC = "MCC";
    private static final int COL_MCC_IDX = 2;
    private static final String COL_MNC = "MNC";
    private static final int COL_MNC_IDX = 3;
    private static final String COL_NETWORK_TYPE = "NETWORK_TYPE";
    private static final int COL_NETWORK_TYPE_IDX = 4;
    private static final String COL_PROVIDER_NAME = "PROVIDER_NAME";
    private static final int COL_PROVIDER_NAME_IDX = 5;
    private static final String COL_LATITUDE = "LATITUDE";
    private static final int COL_LATITUDE_IDX = 6;
    private static final String COL_LONGITUDE = "LONGITUDE";
    private static final int COL_LONGITUDE_IDX = 7;
    private static final String COL_ALTITUDE = "ALTITUDE";
    private static final int COL_ALTITUDE_IDX = 8;
    private static final String COL_ACCURACY = "ACCURACY";
    private static final int COL_ACCURACY_IDX = 9;
    private static final String COL_ADDRESS = "ADDRESS";
    private static final int COL_ADDRESS_IDX = 10;

    // Table Creation SQL Code
    private static final String CREATE_TABLE_CELLTOWERS = "CREATE TABLE IF NOT EXISTS " + TABLE_CELLTOWER + " ("
            + COL_CID + " INT NOT NULL, "
            + COL_LAC + " INT NOT NULL, "
            + COL_MCC + " INT NOT NULL, "
            + COL_MNC + " INT NOT NULL, "
            + COL_NETWORK_TYPE + " VARCHAR(10) NULL, "
            + COL_PROVIDER_NAME + " VARCHAR(25) NULL, "
            + COL_LATITUDE + " FLOAT NULL, "
            + COL_LONGITUDE + " FLOAT NULL, "
            + COL_ALTITUDE + " FLOAT NULL, "
            + COL_ACCURACY + " INT NULL, "
            + COL_ADDRESS + " VARCHAR(128), "
            + "PRIMARY KEY ("
            + COL_CID + ", "
            + COL_LAC + ", "
            + COL_MCC + ", "
            + COL_MNC + ") )";

    // Test Data INSERT SQL Code
    private static final String INSERT_TABLE_CELLTOWER_TEST_DATA = "INSERT OR REPLACE INTO " + TABLE_CELLTOWER + "("
            + COL_CID + ", "
            + COL_LAC + ", "
            + COL_MCC + ", "
            + COL_MNC + ", "
            + COL_NETWORK_TYPE + ", "
            + COL_PROVIDER_NAME + ", "
            + COL_LATITUDE + ", "
            + COL_LONGITUDE + ", "
            + COL_ALTITUDE + ", "
            + COL_ACCURACY + ", "
            + COL_ADDRESS + ") "
            + "VALUES ("
            + "31073, "
            + "6000, "
            + "228, "
            + "2, "
            + "'GSM' ,"
            + "'Sunrise Switzerland', "
            + "46.539507, "
            + "6.580606, "
            + "NULL, "
            + "898, "
            + "'Avenue du 14-Avril, Renens-Village, Renens, District de l''Ouest lausannois, Vaud, 1020, Switzerland')";

    // Attributes
    Context mContext;                           // Application Context

    private String mDBPath = null;              // Database File Path
    private String mDBName = null;              // Database Filename
    private String mDBFullName = null;          // Database Full Filename (Path & Name)
    private SQLiteDatabase mDatabase;           // A Database Instance

    private IntentFilter mAppFilter = null;     // Message Filter
    private boolean mReceiverRegistered = false;

    private ArrayList<CellTower> mCellTowers;   // List of the CellTowers already defined in database
    private CellTower mCellTower;

    /**
     * Constructor #1
     *
     * @param context   // Context used for Message exchange
     * @param dbname    // The name of the Database (Filename only, without the path)
     * @param factory   // SQLiteDatabase.CursorFactory to use for creating cursor objects, or null for the default
     * @param version   // Database Version (default 1)
     *
     */
    public CellTowerDB(Context context, String dbname, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dbname, factory, version);

        // Set Context
        mContext = context;

        mDBPath = DB_PATH;              // mDBPath.substring(0, mDBPath.lastIndexOf("/")) + "/databases";
        mDBName = DB_NAME;              // dbname;
        mDBFullName = DB_FULLPATH;      // mDBPath + "/" + mDBName;

        // Setup Database File according to the one in "assets" Folder
        setDBFile();

        // Open the Database
        if(this.open()==true) {
            // Initialize Message Exchange Receiver
            registerReceiver();

            // Send Database Ready Notification Message
            Intent anIntent = new Intent();
            anIntent.setAction(CELLTOWERS_DB_READY);
            mContext.sendBroadcast(anIntent);
        }
        else {
            Toast.makeText(mContext, "Failed to open Database. Exiting ...", Toast.LENGTH_LONG).show();
            Intent anIntent = new Intent();
            anIntent.setAction(EXIT);
            mContext.sendBroadcast(anIntent);
        }
    }

    @Override
    public void finalize() throws Throwable {
        mDatabase.close();
        unregisterReceiver();
        super.finalize();
    }

    /**
     * Helper function that returns the current Version of the SQLite Database
     *
     * @return  SQLite Database Version as a String
     */
    public String getSQLiteVersion() {
        String query = CHECK_SQLITE_VERSION;
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(":memory:", null);
        Cursor cursor = db.rawQuery(query, null);
        String sqliteVersion = "";
        if (cursor.moveToNext()) {
            sqliteVersion = cursor.getString(0);
        }
        return sqliteVersion;
    }

    /**
     * Set Database File.
     * Check if DB File already exists in App "databases" directory
     * If not (1st App. runs) copy DB File from "assets" directory into App "databases"+ directory
     *
     */
    private boolean dbFileExists() {
        if(!FileHelper.fileExists(mDBFullName)) {
            return false;
        }
        return true;
    }

    private void setDBFile() {

        if(!dbFileExists()) {
            Log.i(TAG, "DB File doesn\'t exist.");
            InputStream in = null;
            OutputStream out = null;
            FileHelper.createDir(DB_PATH);
            FileHelper.createFile(mDBFullName);
            AssetManager assetManager = mContext.getAssets();
            try {
                in = assetManager.open(DB_NAME);
                out = new FileOutputStream(mDBFullName);
                FileHelper.copyFile(in, out);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Log.i(TAG, "DB File already exists.");
        }
    }

    /**
     * Initialize the Database Object
     * Open the Database
     * Create CellTowers Table if needed
     *
     */
    public boolean open() {
        mDatabase = this.getWritableDatabase().openDatabase(mDBFullName, null, SQLiteDatabase.CREATE_IF_NECESSARY);

        if((mDatabase!=null) && (mDatabase.isOpen())) {
            if((this.select()!=null) && (mCellTowers.size()>0)) {
                Intent aListIntent = new Intent();
                aListIntent.setAction(CELLTOWERS_LIST);
                aListIntent.putParcelableArrayListExtra(CELLS, mCellTowers);
                mContext.sendBroadcast(aListIntent);
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Close the Database
     */
    public void close() {
        if(this.mDatabase.isOpen()) { mDatabase.close(); }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(CREATE_TABLE_CELLTOWERS);
//        db.execSQL(INSERT_TABLE_CELLTOWER_TEST_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Helper function that returns the SQLite Database Object
     *
     * @return      The SQLite Database Object
     */
    public SQLiteDatabase getDB() {
        return mDatabase;
    }

    private void registerReceiver() {
        if(mReceiverRegistered==false) {
            try {
                mAppFilter = new IntentFilter();
                mAppFilter.addAction(CELL_DETECTED);
                mAppFilter.addAction(TEST_CELL_DETECTED);
                mAppFilter.addAction(CELLTOWER_LOCATION);
                mAppFilter.addAction(CELLTOWER_SELECTED);
                mAppFilter.addAction(REQUEST_CELLTOWER);
                mAppFilter.addAction(REQUEST_CELLTOWERS_LIST);
                mContext.registerReceiver(mMessageReceiver, mAppFilter);
                mReceiverRegistered = true;
            }
            catch (Exception ex) { Log.e(TAG, ex.getMessage()); }
            finally { }
        }
    }

    private void unregisterReceiver() {
        if(mReceiverRegistered==true) {
            try {
                mContext.unregisterReceiver(mMessageReceiver);
                mReceiverRegistered = false;
            }
            catch (Exception ex) { Log.e(TAG, ex.getMessage()); }
            finally { }
        }
    }



    /**
     * INSERT the given CellTower's Data into the Database
     *
     * @param celltower     A new CellTower Object to add
     * @return              Execution Result Code (New or existing Row ID, -1 if failed)
     */
    public long insert(CellTower celltower) {

        mCellTower = celltower;
        ContentValues values = new ContentValues();

        values.put(COL_CID, mCellTower.getCId());
        values.put(COL_LAC, mCellTower.getLac());
        values.put(COL_MCC, mCellTower.getMCC());
        values.put(COL_MNC, mCellTower.getMNC());
        values.put(COL_NETWORK_TYPE, mCellTower.getNetworkType());
        values.put(COL_PROVIDER_NAME, mCellTower.getProviderName());
        values.put(COL_LATITUDE, mCellTower.getLocation().getLatitude());
        values.put(COL_LONGITUDE, mCellTower.getLocation().getLongitude());
        values.put(COL_ALTITUDE, mCellTower.getLocation().getAltitude());
        values.put(COL_ACCURACY, mCellTower.getLocation().getAccuracy());
        values.put(COL_ADDRESS, mCellTower.getLocation().getAddress());

        return mDatabase.insertWithOnConflict(TABLE_CELLTOWER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Update the given CellTower Data
     *
     * @param celltower     The modified CellTower Object to update
     *
     * @return              Execution Result Code (New or existing Row ID, -1 if failed)
     */
    public int update(CellTower celltower) {

        mCellTower = celltower;
        ContentValues values = new ContentValues();

//        values.put(COL_CID, mCellTower.getCId());
//        values.put(COL_LAC, mCellTower.getLac());
//        values.put(COL_MCC, mCellTower.getMCC());
//        values.put(COL_MNC, mCellTower.getMNC());
        values.put(COL_NETWORK_TYPE, mCellTower.getNetworkType());
        values.put(COL_PROVIDER_NAME, mCellTower.getProviderName());
        values.put(COL_LATITUDE, mCellTower.getLocation().getLatitude());
        values.put(COL_LONGITUDE, mCellTower.getLocation().getLongitude());
        values.put(COL_ALTITUDE, mCellTower.getLocation().getAltitude());
        values.put(COL_ACCURACY, mCellTower.getLocation().getAccuracy());
        values.put(COL_ADDRESS, mCellTower.getLocation().getAddress());
        String where = COL_CID + " = " + mCellTower.getCId()
                + " AND " + COL_LAC + " = " + mCellTower.getLac()
                + " AND " + COL_MCC + " = " + mCellTower.getMCC()
                + " AND " + COL_MNC + " = " + mCellTower.getMNC();

        return mDatabase.update(TABLE_CELLTOWER, values, where, null);
    }

    /**
     * Delete the given CellTower Object
     * @param celltower     The CellTower Object to delete
     *
     * @return              Execution Result Code (New or existing Row ID, -1 if failed)
     */
    public int delete(CellTower celltower) {

        mCellTower = celltower;

        String where = COL_CID + " = " + mCellTower.getCId()
                + " AND " + COL_LAC + " = " + mCellTower.getLac()
                + " AND " + COL_MCC + " = " + mCellTower.getMCC()
                + " AND " + COL_MNC + " = " + mCellTower.getMNC();

        return mDatabase.delete(TABLE_CELLTOWER, where, null);
    }

    /**
     * Check if the given CellTower already exists in Database
     *
     * @param cell          The CellTower Object to check
     *
     * @return              Execution Result Code (True if exists, false otherwise)
     */
    public boolean exists(CellTower cell) {
    boolean ret = false;
        if(cell!=null) {
            ret = exists(cell.getCId(), cell.getLac(), cell.getMCC(), cell.getMNC());
        }
        return ret;
    }

    /**
     * Check if a CellTower already exists in Database,
     * being given its cid, lac, mcc and mnc.
     *
     * @param cid           CellTower's Cell Id
     * @param lac           CellTower's Location Area Code
     * @param mcc           CellTower's Mobile Country Code
     * @param mnc           CellTower's Mobile Network Code
     *
     * @return              Execution Result Code (True if exists, false otherwise)
     */
    public boolean exists(int cid, int lac, int mcc, int mnc) {
    boolean ret = false;
        // Only CId & Lac are currently used as PK for search
        String query = "SELECT COUNT(*) FROM " + TABLE_CELLTOWER
                + " WHERE " +  COL_CID + " = " + cid
                + " AND " + COL_LAC + " = " + lac
//                + " AND " + COL_MCC + " = " + mcc
//                + " AND " + COL_MNC + " = " + mnc
                ;
        Cursor cur = mDatabase.rawQuery(query, null);
        cur.moveToFirst();
        ret = cur.getInt(0) == 1;
        cur.close();
        return ret;
    }

    /**
     * Extract the CellTowers from the DB Table CELLTOWER
     * and fill an ArrayList.
     *
     * @return              An ArrayList of CellTowers
     */
    public ArrayList<CellTower> select() {
        Log.i(TAG, "Executing ArrayList<CellTower> SELECT() ...");

        String orderBy = "ORDER BY COL_LATITUDE, COL_LONGITUDE";

        if(mCellTowers==null) {
            mCellTowers = new ArrayList<CellTower>();
        }
        else {
            mCellTowers.clear();
        }

        Cursor cur = mDatabase.query(TABLE_CELLTOWER,
                new String[] {COL_CID,
                              COL_LAC,
                              COL_MCC,
                              COL_MNC,
                              COL_NETWORK_TYPE,
                              COL_PROVIDER_NAME,
                              COL_LATITUDE,
                              COL_LONGITUDE,
                              COL_ALTITUDE,
                              COL_ACCURACY,
                              COL_ADDRESS},
                null,
                null,
                null,
                null,
                 orderBy);

        cur.moveToFirst();

        for (int idx=0; idx<cur.getCount(); idx++) {
            CellTower cell = new CellTower(cur.getInt(COL_CID_IDX),
                                           cur.getInt(COL_LAC_IDX),
                                           cur.getInt(COL_MCC_IDX),
                                           cur.getInt(COL_MNC_IDX),
                                           cur.getString(COL_NETWORK_TYPE_IDX),
                                           cur.getString(COL_PROVIDER_NAME_IDX),
                                           cur.getFloat(COL_LATITUDE_IDX),
                                           cur.getFloat(COL_LONGITUDE_IDX),
                                           cur.getFloat(COL_ALTITUDE_IDX),
                                           cur.getInt(COL_ACCURACY_IDX),
                                           cur.getString(COL_ADDRESS_IDX));
//            if(mCellTower!=null) {
//                if(cell.getCId()==mCellTower.getCId()) {
//                    Log.i(TAG, "Cell Id / Current Cell Id: " + cell.getCId() + " / " + mCellTower.getCId());
//                }
//            }
            mCellTowers.add(cell);
            cur.moveToNext();
        }
        return mCellTowers;
    }

    /**
     * Fetch the current CellTower's Data in DB Table CELLTOWER
     *
     * @param cell          The CellTower Object to fetch
     *
     * @return              The fetched CellTower if exists (null otherwise)
     */
    public CellTower select(CellTower cell) {
        Log.i(TAG, "Executing SELECT(CellTower cell) ...");

        mCellTower = cell;

        String where = COL_CID + " = " + cell.getCId()
                + " AND " + COL_LAC + " = " + cell.getLac()
                + " AND " + COL_MCC + " = " + cell.getMCC()
                + " AND " + COL_MNC + " = " + cell.getMNC();

        String limit = "1";

        Cursor cur = mDatabase.query(TABLE_CELLTOWER,
                new String[] {COL_CID,
                              COL_LAC,
                              COL_MCC,
                              COL_MNC,
                              COL_NETWORK_TYPE,
                              COL_PROVIDER_NAME,
                              COL_LATITUDE,
                              COL_LONGITUDE,
                              COL_ALTITUDE,
                              COL_ACCURACY,
                              COL_ADDRESS},
                where,
                null,
                null,
                null,
                limit);
        cur.moveToFirst();
        if(cur.getCount()>0) {
            mCellTower = new CellTower(cur.getInt(COL_CID_IDX),
                    cur.getInt(COL_LAC_IDX),
                    cur.getInt(COL_MCC_IDX),
                    cur.getInt(COL_MNC_IDX),
                    cur.getString(COL_NETWORK_TYPE_IDX),
                    cur.getString(COL_PROVIDER_NAME_IDX),
                    cur.getFloat(COL_LATITUDE_IDX),
                    cur.getFloat(COL_LONGITUDE_IDX),
                    cur.getFloat(COL_ALTITUDE_IDX),
                    cur.getInt(COL_ACCURACY_IDX),
                    cur.getString(COL_ADDRESS_IDX));
        }
        return mCellTower;
    }

    /**
     * Fetch a CellTower in DB Table CELLTOWER,
     * being given its cid, lac, mcc and mnc.

     * @param cid           CellTower's Cell Id
     * @param lac           CellTower's Location Area Code
     * @param mcc           CellTower's Mobile Country Code
     * @param mnc           CellTower's Mobile Network Code
     *
     * @return              The fetched CellTower if exists (null otherwise)
     */
    public CellTower select(int cid, int lac, int mcc, int mnc) {

        String where = COL_CID + " = " + cid
                + " AND " + COL_LAC + " = " + lac
                + " AND " + COL_MCC + " = " + mcc
                + " AND " + COL_MNC + " = " + mnc;

        String limit = "1";

        Cursor cur = mDatabase.query(TABLE_CELLTOWER,
                                       new String[] {COL_CID,
                                                     COL_LAC,
                                                     COL_MCC,
                                                     COL_MNC,
                                                     COL_NETWORK_TYPE,
                                                     COL_PROVIDER_NAME,
                                                     COL_LATITUDE,
                                                     COL_LONGITUDE,
                                                     COL_ALTITUDE,
                                                     COL_ACCURACY,
                                                     COL_ADDRESS},
                                                where,
                                                null,
                                                null,
                                                null,
                                                limit);
        cur.moveToFirst();
        if(cur.getCount()>0) {
            mCellTower = new CellTower(cur.getInt(COL_CID_IDX),
                    cur.getInt(COL_LAC_IDX),
                    cur.getInt(COL_MCC_IDX),
                    cur.getInt(COL_MNC_IDX),
                    cur.getString(COL_NETWORK_TYPE_IDX),
                    cur.getString(COL_PROVIDER_NAME_IDX),
                    cur.getFloat(COL_LATITUDE_IDX),
                    cur.getFloat(COL_LONGITUDE_IDX),
                    cur.getFloat(COL_ALTITUDE_IDX),
                    cur.getInt(COL_ACCURACY_IDX),
                    cur.getString(COL_ADDRESS_IDX));
            return mCellTower;
        }
        else {
            return  null;
        }
    }


    /**
     * MessageReceiver Class used to process incoming 'cell-detected' message
     * sent by the CellTowersScanner each time a CellTower is detected.
     *
     *
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        CellTower cell = null;
        Intent anInfoIntent = null;
        Intent aListIntent = null;
        Intent aLocationIntent = null;

            // Check incoming Intent and returns if null
            if (intent == null) { return; }

            switch(intent.getAction()) {
                // CellTower detected ...
                case CELL_DETECTED:
                    Log.i(TAG, "CELL_DETECTED Received ...");
                    // Retrieve CellTower and check if already exists in DB
                    cell = (CellTower) intent.getExtras().get(CELL);
                    // Exists in DB, set as current CellTower
                    if(exists(cell)) {
                        mCellTower = select(cell);
                    }
                    // Doesn't exists in DB, create/add it ...
                    else {
                        if(insert(cell)<0) {
                            Toast.makeText(mContext, "ERROR\n Failed to add currently detected Cell in DB.", Toast.LENGTH_LONG).show();
                        }
                    }

                    // Check CellTower for Location data.
                    // DEFINED: Send CELL_INFO & CELLTOWERS_LIST Messages to UI
                    if(!mCellTower.hasLocation()) {
                        // Refresh the List of Towers
                        select();
                        // Send CELL_INFO Message
                        anInfoIntent = new Intent();
                        anInfoIntent.setAction(CELL_INFO);
                        anInfoIntent.putExtra(CELL, mCellTower);
                        mContext.sendBroadcast(anInfoIntent);
                        // Send CELLTOWERS_LIST Message
                        aListIntent = new Intent();
                        aListIntent.setAction(CELLTOWERS_LIST);
                        aListIntent.putParcelableArrayListExtra(CELLS, mCellTowers);
                        mContext.sendBroadcast(aListIntent);
                    }
                    // UNDEFINED: Send message to CellTowerLocationService to request location
                    else {
                        // Request Cell Tower Location (Send REQUEST_CELLTOWER_LOCATION Message)
                        aLocationIntent = new Intent();
                        aLocationIntent.setAction(REQUEST_CELLTOWER_LOCATION);
                        aLocationIntent.putExtra(CELL, mCellTower);
                        mContext.sendBroadcast(aLocationIntent);
                    }
                    break;
                // CellTower location ...
                case CELLTOWER_LOCATION:
                    Log.i(TAG, "CELLTOWER_LOCATION Received ...");
                    // Update current CellTower with Location data
                    CellTowerLocation location = (CellTowerLocation)intent.getExtras().get("location");
                    if(mCellTower!=null) {
                        if(location!=null) {
                            mCellTower.setLocation(location);
                            update(mCellTower);
                            // Refresh the List of Towers
                            select();
                            // Send CELL_INFO Message
                            anInfoIntent = new Intent();
                            anInfoIntent.setAction(CELL_INFO);
                            anInfoIntent.putExtra(CELL, mCellTower);
                            mContext.sendBroadcast(anInfoIntent);
                            // Send CELLTOWERS_LIST Message
                            aListIntent = new Intent();
                            aListIntent.setAction(CELLTOWERS_LIST);
                            aListIntent.putParcelableArrayListExtra(CELLS, mCellTowers);
                            mContext.sendBroadcast(aListIntent);
                        }
                    }
                    break;
                case REQUEST_CELLTOWER:
                    Log.i(TAG, "REQUEST_CELLTOWER Received ...");
                    cell = (CellTower) intent.getExtras().get(CELL);
                    mCellTower = select(cell);
                    if(mCellTower!=null) { }
                    else { mCellTower = cell; }
                    aListIntent = new Intent();
                    aListIntent.setAction(CELL_INFO);
                    aListIntent.putExtra(CELL, mCellTower);
                    mContext.sendBroadcast(aListIntent);
                    break;
                case REQUEST_CELLTOWERS_LIST:
                    Log.i(TAG, "REQUEST_CELLTOWERS_LIST Received ...");
                    aListIntent = new Intent();
                    aListIntent.setAction(CELLTOWERS_LIST);
                    aListIntent.putParcelableArrayListExtra(CELLS, mCellTowers);
                    mContext.sendBroadcast(aListIntent);
                    break;
                case CELLTOWER_SELECTED:
                    Log.i(TAG, "CELLTOWER_SELECTED Received ...");
                    String toastMsg;
                    mCellTower = (CellTower) intent.getExtras().get(CELL);
                    if(mCellTower!=null) {
                        toastMsg = "CellTower Info\n" +
                                   "CID           " + String.valueOf(mCellTower.getCId() + "\n" +
                                   "LAC           " + String.valueOf(mCellTower.getLac()) + "\n" +
                                   "MCC           " + String.valueOf(mCellTower.getMCC()) + "\n" +
                                   "MNC           " + String.valueOf(mCellTower.getMNC()) + "\n" +
                                   "Network Type  " + mCellTower.getNetworkType() + "\n" +
                                   "Operator Name " + mCellTower.getProviderName() + "\n" +
                                   "Latitude      " + String.valueOf(mCellTower.getLocation().getLatitude())) + "\n" +
                                   "Longitude     " + String.valueOf(mCellTower.getLocation().getLongitude()) + "\n" +
                                   "Address       " + mCellTower.getLocation().getAddress();
                        Toast.makeText(mContext, toastMsg, Toast.LENGTH_LONG).show();
                    }
                    else {
                        toastMsg = "CellTower Info\n" +
                                   "CellTower Information is UNAVAILABLE";
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
