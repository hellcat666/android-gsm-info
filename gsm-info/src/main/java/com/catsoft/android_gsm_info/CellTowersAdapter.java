package com.catsoft.android_gsm_info;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Project: android-gsm-info
 * Package: com.itds.sms.ping
 * File:
 * Created by HellCat on 25.01.2018.
 */

public class CellTowersAdapter extends ArrayAdapter<CellTower> {

    private TextView tvCellTowerId;
    private TextView tvCellTowerLac;
    private TextView tvCellTowerMCC;
    private TextView tvCellTowerMNC;

    public CellTowersAdapter(Context context, ArrayList<CellTower> CellTowers) {
        super(context, 0, CellTowers);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        CellTower CellTower = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_celltower_info, parent, false);
        }
        // Lookup view for data population
        TextView tvCellTowerId = convertView.findViewById(R.id.tvCellTowerId);
        TextView tvCellTowerLac = convertView.findViewById(R.id.tvCellTowerLac);
        TextView tvCellTowerMCC = convertView.findViewById(R.id.tvCellTowerMCC);
        TextView tvCellTowerMNC = convertView.findViewById(R.id.tvCellTowerMNC);

        // Populate the data into the template view using object's data
        tvCellTowerId.setText(String.valueOf(CellTower.getCId()));
        tvCellTowerLac.setText(String.valueOf(CellTower.getLac()));
        tvCellTowerMCC.setText(String.valueOf(CellTower.getMCC()));
        tvCellTowerMNC.setText(String.valueOf(CellTower.getMNC()));

        // Return the completed view to render on screen
        return convertView;
    }

}
