package com.catsoft.android_gsm_info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
 */

public class CellTowersMapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback {

    private static final String TAG = "GSMInfo-FragCellTowersMap";

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
    private static final String REFRESH = "refresh";

    private static final String REQUEST_CELLTOWERS_LIST = "request-celltowers-list";

    private static final String RX_REGISTERED = "rx-registered";
    private static final String NOT_AVAILABLE = "n.a";

    private View mView;
    private Context mContext;
    private Bundle mSavedInstanceState;

    private IntentFilter mAppFilter;
    private boolean mReceiverRegistered = false;

    private ArrayList<MapCellTower> mMapCellTowers;
    private MapCellTower mSelectedCellTower = null;
    private MapCellTower mCurrentCellTower = null;

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap = null;
    private MapView mMapView = null;
    private Rectangle mMapRectangle = null;
    private LatLngBounds mMapBounds = null;

    private LatLng mHomeLatLng = new LatLng(46.5411101, 6.5820545);
    private float mDefaultZoomLevel = 13.0f;
    private int mActiveCircleColor = 0x15ff0000;
    private int mInactiveCircleColor = 0x15808080;

    private Marker mHomeMarker;
    private Marker mCurrentCellTowerMarker = null;
    private LatLng mCurrentCellTowerLatLng = null;

    private BitmapDescriptor mIconHome = null;
    private BitmapDescriptor mIconActiveBTS = null;
    private BitmapDescriptor mIconInactiveBTS = null;

    private boolean mMapReady = false;

    public CellTowersMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        mMapCellTowers = new ArrayList<MapCellTower>();

        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mMapFragment.getMapAsync(this);

        mSavedInstanceState = savedInstanceState;
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
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver();
    }
    @Override
    public void onResume() {
        super.onResume();
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


    private void registerReceiver() {
        if(mReceiverRegistered==false) {
            try {
                mAppFilter = new IntentFilter();
                mAppFilter.addAction(FRAGMENT_MAP_READY);
                mAppFilter.addAction(CELL_INFO_UPDATED);
//                mAppFilter.addAction(CELL_DETECTED);
                mAppFilter.addAction(CELL_LOCATION_CHANGED);
                mAppFilter.addAction(CELLTOWERS_LIST);
                mAppFilter.addAction(REFRESH);
                mContext.registerReceiver(mMessageReceiver, mAppFilter);
                mReceiverRegistered = true;
            } catch (Exception ex) {
            } finally {
            }
        }
    }

    private void unregisterReceiver() {
        if(mReceiverRegistered==true) {
            try {
                mContext.unregisterReceiver(mMessageReceiver);
                mReceiverRegistered = false;
            } catch (Exception ex) {
            } finally {
            }
        }
    }

    private void requestCellTowersList() {
        Intent anIntent = new Intent();
        anIntent.setAction(REQUEST_CELLTOWERS_LIST);
        mContext.sendBroadcast(anIntent);
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
    Intent anIntent = null;

            if (intent == null) { return; }

            switch(intent.getAction()) {
                case FRAGMENT_MAP_READY:
                    Log.i(TAG, "FRAGMENT_MAP_READY");
                    requestCellTowersList();
                    break;
                case CELL_INFO_UPDATED:
                    Log.i(TAG, "CELL_INFO_UPDATED");
                    mCurrentCellTower = null;
                    mCurrentCellTower = new MapCellTower((CellTower)intent.getExtras().get(CELL));
                    anIntent = new Intent();
                    anIntent.setAction(REQUEST_CELLTOWERS_LIST);
                    mContext.sendBroadcast(anIntent);
                    break;
                case CELLTOWERS_LIST:
                    Log.i(TAG, "CELLTOWERS_LIST");
                    setCellTowersList(intent);
                    refreshCellTowersMap();
                    break;
                case REFRESH:
                    Log.i(TAG, "REFRESH");
                    refreshCellTowersMap();
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

        mMap = googleMap;
        mMapReady = true;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                Log.i(TAG, "onMapClick() Listener invoked ...");
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentCellTower.getLatLong(), mDefaultZoomLevel));
            }
        });

//        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
//            @Override
//            public void onCameraIdle() {
//                Log.i(TAG, "CameraIdleListener invoked ...");
//            }
//        });

//        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
//            @Override
//            public void onCameraMove() {
//                Log.i(TAG, "CameraMoveListener invoked ...");
//                Intent anIntent = new Intent();
//                anIntent.setAction(REQUEST_CELLTOWERS_LIST);
//                mContext.sendBroadcast(anIntent);
//            }
//        });

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
                Log.i(TAG, "MapLoadedCallback invoked ...");
                mMapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                Intent anIntent = new Intent();
                anIntent.setAction(FRAGMENT_MAP_READY);
                anIntent.putExtra(FRAGMENT, FRAG_CELLTOWERS_MAP);
                mContext.sendBroadcast(anIntent);
                refreshCellTowersMap();
            }
        });

        mIconHome = BitmapDescriptorFactory.fromResource(R.drawable.icon_home16);
        mIconActiveBTS = BitmapDescriptorFactory.fromResource(R.drawable.active_bts);
        mIconInactiveBTS = BitmapDescriptorFactory.fromResource(R.drawable.inactive_bts);

        if(mSavedInstanceState!=null) {
            mCurrentCellTower = mSavedInstanceState.getParcelable(CELL_INFO);
            mMapCellTowers = mSavedInstanceState.getParcelableArrayList(CELLS);
        }
    }

    private void setCellTowersList(Intent intent) {
    MapCellTower mapCellTower = null;

        mMapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        mMapCellTowers.clear();
        ArrayList<CellTower> cellTowers = intent.getParcelableArrayListExtra(CELLS);
        for(int idx=0; idx<cellTowers.size(); idx++) {
            CellTower cell = cellTowers.get(idx);
            mapCellTower = (new MapCellTower(cell));
            if ((mMapBounds != null) && mMapBounds.contains(mapCellTower.getLatLong())) {
                if (mCurrentCellTower != null) {
                    if (mCurrentCellTower.equals(cell)) {
                        Log.i(TAG, "Cells match with CIds: " + String.valueOf(mCurrentCellTower.getCId()) + " / " + String.valueOf(cell.getCId()));
                        mapCellTower.setTitle("Current BTS Info");
                        mapCellTower.setIcon(mIconActiveBTS);
                        mapCellTower.setCircleColor(mActiveCircleColor);
                    } else {
                        mapCellTower.setTitle("BTS Info");
                        mapCellTower.setIcon(mIconInactiveBTS);
                        mapCellTower.setCircleColor(mInactiveCircleColor);
                    }
                    mMapCellTowers.add(mapCellTower);
                }
            }
        }
    }

    protected void setHomeLocationMarker() {
        mHomeMarker = mMap.addMarker(new MarkerOptions()
                .position(mHomeLatLng)
                .title("Home")
                .icon(mIconHome));
        mHomeMarker.setTag(null);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mHomeLatLng, mDefaultZoomLevel));
     }

//    protected void setCurrentCellTowerMarker() {
//        Log.i(TAG, "setCurrentCellTowerMarker()");
//        if(mCurrentCellTower!=null) {
//            if (mCurrentCellTower.hasLocation()) {
//                mCurrentCellTowerLatLng = new LatLng(mCurrentCellTower.getLocation().getLatitude(), mCurrentCellTower.getLocation().getLongitude());
//                mCurrentCellTowerMarker = mMap.addMarker(new MarkerOptions()
//                        .position(mCurrentCellTowerLatLng)
//                        .title("Current BTS")
//                        .icon(mCurrentCellTower.getIcon()));
//                CircleOptions circleOptions = new CircleOptions()
//                        .center(mCurrentCellTowerLatLng)
//                        .radius(mCurrentCellTower.getLocation().getAccuracy())
//                        .strokeWidth(0f)
//                        .fillColor(mActiveCircleColor);
//                mMap.addCircle(circleOptions);
//                CellTowerInfoWindow infoWindow = new CellTowerInfoWindow(mContext);
//                mMap.setInfoWindowAdapter(infoWindow);
//                mCurrentCellTowerMarker.setTag(mCurrentCellTower);
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentCellTowerLatLng, mDefaultZoomLevel));
//            } else {
////                showToast("Active BTS has no Location");
//            }
//        }
//    }

    protected void setCellTowerMarker(MapCellTower cell) {
//        Log.i(TAG, "setInactiveCellTowerMarker(CellTower cell)");
        if(cell!=null) {
            if (cell.hasLocation()) {
                LatLng latLng = new LatLng(cell.getLocation().getLatitude(), cell.getLocation().getLongitude());
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(cell.getLatLong())
                        .title(cell.getTitle())
                        .icon(cell.getIcon()));
                CellTowerInfoWindow infoWindow = new CellTowerInfoWindow(mContext);
                mMap.setInfoWindowAdapter(infoWindow);
                marker.setTag(cell);
                CircleOptions circleOptions = new CircleOptions()
                        .center(cell.getLatLong())
                        .radius(cell.getLocation().getAccuracy())
                        .strokeWidth(0f)
                        .fillColor(cell.getCircleColor());
                mMap.addCircle(circleOptions);
            }
            else {
//                showToast("Inactive BTS has no Location");
            }
        }
    }

    protected void setCellTowerMarkers() {
    LatLng coord = null;
        Log.i(TAG, "setCellTowerMarkers()");
        if(mMapCellTowers.size()>0) {
            for (MapCellTower cell : mMapCellTowers) {
//                if ((mMapBounds != null) && mMapBounds.contains(cell.getLatLong())) {
                    setCellTowerMarker(cell);
//                }
            }
        }
    }

    protected void refresh() {
        Intent anIntent = new Intent();
        anIntent.setAction(REFRESH);
        mContext.sendBroadcast(anIntent);
    }

    protected void refreshCellTowersMap() {
        mMap.clear();
        setHomeLocationMarker();
        setCellTowerMarkers();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i(TAG, "onMarkerClick() invoked ...");
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
