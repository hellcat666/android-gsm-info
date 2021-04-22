package com.catsoft.android_gsm_info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Rectangle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 13.04.2018.
 * Modified by HellCat:
 *  25.03.2019 - setCellTowersList(...): Disable (comment out) MapBound Testing.
 */

public class CellTowersMapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback {

    private static final String TAG = "CellTowersMapFragment";

    private static final String FRAG_CELLTOWERS_MAP = "frag-celltowers-map";
    private static final String FRAGMENT_MAP_READY = "fragment-map-ready";
    private static final String FRAGMENT = "fragment";

    private static final String CELL = "cell";
    private static final String CELL_INFO = "cell-info";
    private static final String CELL_INFO_CHANGED = "cell-info-changed";
    private static final String CELL_INFO_UPDATED = "cell-info-updated";
    private static final String CELL_DETECTED= "cell-detected";
    private static final String CELL_LOCATION_CHANGED = "cell-location-changed";
    private static final String GSM_SIGNAL_STRENGTH_CHANGED = "gsm-signal-strength-changed";
    private static final String CELLS = "cells";
    private static final String CELLTOWERS_LIST = "celltowers-list";

    private static final String GPS_LOCATION_DATA = GPSTrackingService.GPS_LOCATION_DATA;

    private static final String REFRESH = "refresh";


    private static final String REQUEST_CELLTOWERS_LIST = "request-celltowers-list";
    private static final String RX_REGISTERED = "rx-registered";
    private static final String NOT_AVAILABLE = "n.a";

    private View mView;
    private Context mContext;
    private Bundle mSavedInstanceState;

    private IntentFilter mAppFilter;
    private boolean mReceiverRegistered = false;

    private ArrayList<CellTower> mCellTowers;
    private ArrayList<MapCellTower> mMapCellTowers;
    private ArrayList<Marker> mMarkers;
    private MapCellTower mSelectedCellTower = null;
    private MapCellTower mCurrentCellTower = null;
    private MapCellTower mPreviousCellTower = null;

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap = null;

    private int mMapType = GoogleMap.MAP_TYPE_NORMAL;
    public int getMapType() { return mMapType; }
    public void setMapType(int mapType) {
        this.mMapType = mapType;
        if(mMap!=null) { mMap.setMapType(mMapType); }
    }

    private MapView mMapView = null;

    private Rectangle mMapRectangle = null;
    private Circle mMapCircle = null;
    private LatLngBounds mMapBounds = null;

    private LatLng mHomeLatLng = new LatLng(46.5411101, 6.5820545);
    private LatLng mUserLatLng = mHomeLatLng;

    private float mDefaultZoomLevel = 14.0f;
    public float getDefaultZoomLevel() { return mDefaultZoomLevel; }
    public void setDefaultZoomLevel(float defaultZoomLevel) {
        mDefaultZoomLevel = defaultZoomLevel;
        mCurrentZoomLevel = mDefaultZoomLevel;
    }
    static private float mCurrentZoomLevel = 0;

    private int mActiveCircleColor = 0x15600000;    // 0x15ff0000;
    private int mInactiveCircleColor = 0x15606060;  // 0x15808080;

    private Marker mHomeMarker = null;
    private Marker mUserMarker = null;
    private Marker mCurrentCellTowerMarker = null;
    private LatLng mCurrentCellTowerLatLng = null;

    private BitmapDescriptor mIconHome = null;
    private BitmapDescriptor mIconUser = null;
    private BitmapDescriptor mIconActiveBTS = null;
    private BitmapDescriptor mIconInactiveBTS = null;
    private BitmapDescriptor mIconInactiveSatBTS = null;

    private boolean mMapReady = false;


    public static interface OnCellTowersMapFragmentCompleteListener { public abstract void onCellTowersMapFragmentComplete(); }
    private OnCellTowersMapFragmentCompleteListener mListener;

    public CellTowersMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        mContext = this.getContext();
        MapsInitializer.initialize(mContext);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_celltowers_map, container, false);
        return mView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCurrentCellTower = null;
        initMap();
        mSavedInstanceState = savedInstanceState;
        mListener.onCellTowersMapFragmentComplete();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(RX_REGISTERED, mReceiverRegistered);
        outState.putParcelable(CELL_INFO, mCurrentCellTower);
        outState.putParcelableArrayList(CELLS, mMapCellTowers);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            this.mListener = (OnCellTowersMapFragmentCompleteListener)context;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCellTowersMapFragmentCompleteListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
        registerReceiver();
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        Intent anIntent = new Intent();
        anIntent.setAction(FRAGMENT_MAP_READY);
        anIntent.putExtra(FRAGMENT, FRAG_CELLTOWERS_MAP);
        mContext.sendBroadcast(anIntent);
        /*
        if(mSavedInstanceState!=null) {
            mReceiverRegistered = mSavedInstanceState.getBoolean("rx-registered");
            mCurrentCellTower = mSavedInstanceState.getParcelable(CELL_INFO);
            mMapCellTowers = mSavedInstanceState.getParcelableArrayList(CELLS);
            if(mMapCellTowers!=null) {
                refreshCellTowersMap();
            }
        }
        else {
            requestCellTowersList();
        }
        */
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initMap() {
        Log.i(TAG, "initMap()");
        mMapCellTowers = new ArrayList<MapCellTower>();
        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
    }

    private void registerReceiver() {
        Log.i(TAG, "registerReceiver()");
        if(mReceiverRegistered==false) {
            try {
                mAppFilter = new IntentFilter();
                mAppFilter.addAction(FRAGMENT_MAP_READY);
                mAppFilter.addAction(CELL_INFO_UPDATED);
                mAppFilter.addAction(CELL_DETECTED);
                mAppFilter.addAction(CELL_LOCATION_CHANGED);
                mAppFilter.addAction(CELLTOWERS_LIST);
                mAppFilter.addAction(REFRESH);
                mAppFilter.addAction(GPS_LOCATION_DATA);
                mContext.registerReceiver(mMessageReceiver, mAppFilter);
                mReceiverRegistered = true;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            } finally {
            }
        }
    }

    private void unregisterReceiver() {
        Log.i(TAG, "unregisterReceiver()");
        if(mReceiverRegistered==true) {
            try {
                mContext.unregisterReceiver(mMessageReceiver);
                mReceiverRegistered = false;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            } finally {
            }
        }
    }


    private void requestCellTowersList() {
        Log.i(TAG, "requestCellTowersList()");
        Intent anIntent = new Intent();
        anIntent.setAction(REQUEST_CELLTOWERS_LIST);
        mContext.sendBroadcast(anIntent);
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent anIntent = null;
            MapCellTower cell;

            if (intent == null) { return; }

            switch(intent.getAction()) {
                case FRAGMENT_MAP_READY:
                    Log.i(TAG, "FRAGMENT_MAP_READY");
                    setHomeLocationMarker();
                    requestCellTowersList();
                    break;

                case CELLTOWERS_LIST:
                    Log.i(TAG, "CELLTOWERS_LIST");
                    setCellTowersList(intent);
                    setMapCellTowersList();
                    setMapCellTowerMarkers();
                    refreshCellTowersMap();
                    break;

                case CELL_INFO_UPDATED:
                    Log.i(TAG, "CELL_INFO_UPDATED");
                    if(mCurrentCellTower!=null) mPreviousCellTower = mCurrentCellTower;
                    mCurrentCellTower = new MapCellTower(mContext, (CellTower) intent.getExtras().get(CELL));
                    if(mCurrentCellTower.equals(mPreviousCellTower)) {
                        Log.i(TAG, "CELL_INFO_UPDATED -- SAME CellTower...");
                        refreshCellTowersMap();
                    }
                    else {
                        Log.i(TAG, "CELL_INFO_UPDATED -- CellTower CHANGED");
                        requestCellTowersList();
                    }
                    break;

                case CELL_INFO_CHANGED:
                    Log.i(TAG, "CELL_INFO_CHANGED");
                    break;

                case CELL_DETECTED:
                    Log.i(TAG, "CELL_DETECTED");
                    break;

                case CELL_LOCATION_CHANGED:
                    Log.i(TAG, "CELL_LOCATION_CHANGED");
                    /*
                    mPreviousCellTower = mCurrentCellTower;
                    mCurrentCellTower = new MapCellTower((CellTower) intent.getExtras().get(CELL));
                    if(mCurrentCellTower.equals(mPreviousCellTower)) {
                        Log.i(TAG, "CELL_LOCATION_CHANGED -- SAME CellTower...");
                        if((mMapReady) && (mCellTowers!=null)) { refreshCellTowersMap(); }
                    }
                    else {
                        Log.i(TAG, "CELL_LOCATION_CHANGED -- CellTower CHANGED");
                        requestCellTowersList();
                    }
                    */
                    break;
                case GPS_LOCATION_DATA:
                    Log.i(TAG, "GPS_LOCATION_DATA");
                    Location location = (Location)intent.getExtras().get("location");
                    mUserLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    setUserLocationMarker();
                    break;
                case REFRESH:
                    Log.i(TAG, "REFRESH");
                    if((mMapReady) && (mCellTowers!=null)) {
//                        refreshCellTowersMap();
                    }
                    break;
            }
        }
    };

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
    Log.i(TAG, "onMapReady()");
        mMap = googleMap;
        mMap.setMapType(mMapType);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMapReady = true;

        /*
         * UNUSED
         */
//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng point) {
//                Log.i(TAG, "onMapClick() Listener invoked ...");
//                if(mCurrentCellTower!=null) {
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentCellTower.getLatLong(), mCurrentZoomLevel));
//                }
//            }
//        });

        /*
         * UNUSED
         */
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Log.i(TAG, "onCameraIdle()");
                mCurrentZoomLevel = mMap.getCameraPosition().zoom;
            }
        });

        /*
         * UNUSED
         */
//        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
//            @Override
//            public void onCameraMove() {
//                Log.i(TAG, "CameraMoveListener invoked ...");
//                Intent anIntent = new Intent();
//                anIntent.setAction(REQUEST_CELLTOWERS_LIST);
//                mContext.sendBroadcast(anIntent);
//            }
//        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                Log.i(TAG, "onMarkerClick() invoked ...");
                if((marker!=null) && (marker.getTag()!=null) && (marker.getTag().getClass().getName().equals(CellTower.class.getName()))) {
                    marker.showInfoWindow();
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(!marker.isInfoWindowShown()) {
                    marker.showInfoWindow(); }
                else {
                    marker.hideInfoWindow(); }
            }
        });

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
//                Log.i(TAG, "MapLoadedCallback invoked ...");
                mMapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                Intent anIntent = new Intent();
                anIntent.setAction(FRAGMENT_MAP_READY);
                anIntent.putExtra(FRAGMENT, FRAG_CELLTOWERS_MAP);
                mContext.sendBroadcast(anIntent);
            }
        });
        /*
         * UNUSED
         */
//        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
//           public void onCameraChange(CameraPosition camPosition) {
//               mCurrentZoomLevel = camPosition.zoom;
//           }
//        });

        mIconHome = BitmapDescriptorFactory.fromResource(R.drawable.icon_home16);
        mIconUser = BitmapDescriptorFactory.fromResource(R.drawable.user_location160);
        mIconActiveBTS = BitmapDescriptorFactory.fromResource(R.drawable.red_active_bts160);
        mIconInactiveBTS = BitmapDescriptorFactory.fromResource(R.drawable.inactive_bts160);
        mIconInactiveSatBTS = BitmapDescriptorFactory.fromResource(R.drawable.inactive_sat_bts160);

        /*
         * UNUSED
         */
//        if(mSavedInstanceState!=null) {
//            mCurrentCellTower = mSavedInstanceState.getParcelable(CELL_INFO);
//            mMapCellTowers = mSavedInstanceState.getParcelableArrayList(CELLS);
//        }

    }

    /**
     * void setCellTowersList(Intent intent)
     * Set the list of the CellTowers retrieved from  the Database
     *
     * @param intent    The list of CellTowers from the Database
     *
     */
    private void setCellTowersList(Intent intent) {
        mCellTowers = intent.getParcelableArrayListExtra(CELLS);
        Log.i(TAG, "setCellTowersList: size = " + String.valueOf(mCellTowers.size()));
    }

    /**
     * void setMapCellTowersList()
     * Set the array of MapCellTower Objects
     *
     */
    private void setMapCellTowersList() {
        MapCellTower mapCellTower = null;
        mMapCellTowers.clear();
        mCurrentZoomLevel = mMap.getCameraPosition().zoom;
        for(int idx=0; idx<mCellTowers.size(); idx++) {
            CellTower cell = mCellTowers.get(idx);
            mapCellTower = (new MapCellTower(mContext, cell));
            mMapCellTowers.add(mapCellTower);
        }
    }

    protected void setActiveCellTower() {
        MapCellTower cell;
        int prevID=0;
        int curID=0;
        int mrkID=0;
        Log.i(TAG, "setActiveCellTower()");
        if(mMarkers!=null) {
            for (Marker marker : mMarkers) {
                cell = (MapCellTower) marker.getTag();
                if (cell != null) {
                    mrkID = cell.getCId();
//                    if (mrkID == 5121290) {
//                        Log.i(TAG, "Cell 5121290 In List");
//                    }
                    if (mPreviousCellTower != null) {
                        prevID = mPreviousCellTower.getCId();
                        if (mrkID == prevID) {
                            //                        Log.i(TAG, "prevID/curID/mrkID for the PREVIOUS CellTower found: " + String.valueOf(prevID) + "/" + String.valueOf(curID) + "/" + String.valueOf(mrkID));
                            cell.setActive(false);
                            marker.setIcon(cell.getIcon((int) (mCurrentZoomLevel * 8.00f)));
                            if (mMapCircle != null) mMapCircle.remove();
                        }
                    }
                    if (mCurrentCellTower != null) {
                        curID = mCurrentCellTower.getCId();
                        if (mrkID == curID) {
                            //                        Log.i(TAG, "prevID/curID/mrkID for the CURRENT CellTower found: " + String.valueOf(prevID) + "/" + String.valueOf(curID) + "/" + String.valueOf(mrkID));
                            cell.setActive(true);
                            mCurrentCellTowerLatLng = cell.getLatLong();
                            marker.setIcon(cell.getIcon((int) (mCurrentZoomLevel * 15.00f) ));
                            if (mMapCircle != null) mMapCircle.remove();
                            mMapCircle = mMap.addCircle(new CircleOptions()
                                    .center(cell.getLatLong())
                                    .radius(cell.getLocation().getAccuracy())
                                    .strokeWidth(0f)
                                    .fillColor(cell.getCircleColor()));
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentCellTowerLatLng, mMap.getCameraPosition().zoom));
                        }
                    }
                }
            }
        }
    }

    /**
     * void setHomeLocationMarker()
     * Set the Marker for the Home Position (e.g. My Home)
     *
     */
    protected void setHomeLocationMarker() {
        if(mHomeMarker!=null) { mHomeMarker.remove(); }
        mHomeMarker = mMap.addMarker(new MarkerOptions()
                .position(mHomeLatLng)
                .title("Home")
                .icon(mIconHome));
        mHomeMarker.setTag(null);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mHomeLatLng, mCurrentZoomLevel));
    }

    protected void setUserLocationMarker() {
        Log.i(TAG, "setUserLocationMarker()");
        if(mUserMarker!=null) { mUserMarker.remove(); }
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .position(mUserLatLng)
                .title("User")
                .icon(BitmapDescriptorFactory.fromBitmap(AppUtils.resizeIcon(mContext, R.drawable.user_location160, 160))));
        mUserMarker.setTag(null);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mUserLatLng,  mMap.getCameraPosition().zoom));
    }


    /**
     * void setMapCellTowerMarkers()
     * Set Map CellTowers Markers
     *
     */
    protected void setMapCellTowerMarkers() {
        Log.i(TAG, "setCellTowerMarkers()");
        mMap.clear();
        if(mMarkers==null) {
            mMarkers = new ArrayList<Marker>();
        }
        mMarkers.clear();
        if(mMapCellTowers.size()>0) {
            for (MapCellTower mapCellTower : mMapCellTowers) {
                Marker marker = setCellTowerMarker(mapCellTower);
                if (marker != null) { mMarkers.add(marker); }
            }
        }
    }

    /**
     * Marker setCellTowerMarker(MapCellTower cell)
     * Create a Marker for the given CellTower
     *
     * @param cell the CellTower Object we want to add a Marker to.
     * @return The marker Object related to the CellTower
     *
     */
    protected Marker setCellTowerMarker(MapCellTower cell) {
        Marker marker = null;
        MarkerOptions markerOptions = null;
        if(cell!=null) {
            if (cell.hasLocation()) {
                LatLng latLng = new LatLng(cell.getLocation().getLatitude(), cell.getLocation().getLongitude());
                switch(mMap.getMapType()) {
                    case 0:
                    case 1:
                        markerOptions = new MarkerOptions()
                                .position(cell.getLatLong())
                                .title(cell.getTitle())
                                .icon(cell.getIcon((int) (mCurrentZoomLevel * 8.00f)));
                        break;
                    case 2:
                    case 3:
                    case 4:
                        markerOptions = new MarkerOptions()
                                .position(cell.getLatLong())
                                .title(cell.getTitle())
                                .icon(cell.getLightIcon((int) (mCurrentZoomLevel * 8.00f)));
                        break;
                    default:
                        markerOptions = new MarkerOptions()
                                .position(cell.getLatLong())
                                .title(cell.getTitle())
                                .icon(cell.getIcon((int) (mCurrentZoomLevel * 8.00f)));
                        break;
                }
                marker = mMap.addMarker(markerOptions);
                CellTowerInfoWindow infoWindow = new CellTowerInfoWindow(mContext);
                mMap.setInfoWindowAdapter(infoWindow);

                /*
                 * UNUSED - We don't want to draw Circles for inactive CellTowers
                 */
//                CircleOptions circleOptions = new CircleOptions()
//                        .center(cell.getLatLong())
//                        .radius(cell.getLocation().getAccuracy())
//                        .strokeWidth(0f)
//                        .fillColor(cell.getCircleColor());
//                Circle circle = mMap.addCircle(circleOptions);

                marker.setTag(cell);
            }
            else {
//                showToast("Inactive BTS has no Location");
            }
        }
        return marker;
    }

    protected void refresh() {
        Intent anIntent = new Intent();
        anIntent.setAction(REFRESH);
        mContext.sendBroadcast(anIntent);
    }

    protected void refreshCellTowersMap() {
        setUserLocationMarker();
        setActiveCellTower();
    }

    private void showToast(final String msg){
        //gets the main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // run this code in the main thread
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
