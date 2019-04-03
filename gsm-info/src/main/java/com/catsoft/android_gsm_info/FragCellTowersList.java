package com.catsoft.android_gsm_info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Project: android-simcard-info
 * Package: com.catsoft.android_simcard_info
 * File:
 * Created by HellCat on 16.02.2018.
 */

public class FragCellTowersList extends android.support.v4.app.Fragment {

    private static final String TAG = "GSMInfo-FragCellTowersList";

    private static final String FRAG_CELLTOWERS_LIST = "frag-celltowers-list";
    private static final String FRAGMENT_READY = "fragment-ready";
    private static final String FRAGMENT = "fragment";

    private static final String CELL = "cell";
    private static final String CELLS = "cells";
    private static final String CELLS_LIST = "cells-list";
    private static final String CELL_SELECTED = "cell-selected";

//    private static CellTowerDB mCellTowersDB = null;

    Context mContext = null;

    private TextView mCellTowersListTextView;
    private ListView mCellTowersListView;
    private CellTowersAdapter mCellTowersArrayAdapter;
    private ArrayList<CellTower> mArrayOfCellTowers;
    private CellTower mSelectedCellTower = null;

    boolean mConnected = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_celltowers_list, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCellTowersListTextView = getActivity().findViewById(R.id.lblCellTowersList);
        mCellTowersListView = getActivity().findViewById(R.id.lvCellTowers);
        mCellTowersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int itemIdx, long mylng) {
                mSelectedCellTower = (CellTower) (mCellTowersListView.getItemAtPosition(itemIdx));
                Intent anIntent = new Intent();
                anIntent.setAction(CELL_SELECTED);
                anIntent.putExtra(CELL, mSelectedCellTower);
                getContext().sendBroadcast(anIntent);
            }
        });
        mArrayOfCellTowers = new ArrayList<CellTower>();
        mCellTowersArrayAdapter = new CellTowersAdapter(this.getContext(), mArrayOfCellTowers);
        mCellTowersListView.setAdapter(mCellTowersArrayAdapter);

//        IntentFilter appFilter = new IntentFilter();
//        appFilter.addAction("cell-towers-list-updated");
//        appFilter.addAction(CELLS_LIST);
//        mContext.registerReceiver(mMessageReceiver, appFilter);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter appFilter = new IntentFilter();
        appFilter.addAction(CELLS_LIST);
        mContext.registerReceiver(mMessageReceiver, appFilter);

        Intent anIntent = new Intent();
        anIntent.setAction(FRAGMENT_READY);
        anIntent.putExtra(FRAGMENT, FRAG_CELLTOWERS_LIST);
        getContext().sendBroadcast(anIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.i(TAG, "mMessageReceiver.onReceive()");

            switch(intent.getAction()) {
                case CELLS_LIST:
                    mArrayOfCellTowers.clear();
                    mArrayOfCellTowers = intent.getParcelableArrayListExtra(CELLS);
                    mCellTowersArrayAdapter = new CellTowersAdapter(mContext, mArrayOfCellTowers);
                    mCellTowersListView.setAdapter(mCellTowersArrayAdapter);
                    mCellTowersListTextView.setText("Cell Towers (" + String.format("%03d", mArrayOfCellTowers.size()) + ")");
                    break;
            }
        }
    };
}
