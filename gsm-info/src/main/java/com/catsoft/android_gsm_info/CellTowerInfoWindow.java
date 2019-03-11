package com.catsoft.android_gsm_info;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 19.04.2018.
 */

public class CellTowerInfoWindow  implements GoogleMap.InfoWindowAdapter  {

    private static final String TAG = "GSMInfo-CellTowerInfoWindow";

    private static final String NOT_AVAILABLE = "n.a";


    private Context mContext;
    private View mView;
    private TextView mTitle;

    public CellTowerInfoWindow(Context context) {
        mContext = context;
        mView = ((Activity) mContext).getLayoutInflater()
                .inflate(R.layout.cell_tower_info_window, null);
        mTitle = mView.findViewById(R.id.lblCellTowerInfo);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        TextView txtCid = mView.findViewById(R.id.txtCid);
        TextView txtLac = mView.findViewById(R.id.txtLac);
        TextView txtMcc = mView.findViewById(R.id.txtMcc);
        TextView txtMnc = mView.findViewById(R.id.txtMnc);
        TextView txtLat = mView.findViewById(R.id.txtLat);
        TextView txtLng = mView.findViewById(R.id.txtLng);
        TextView txtAddress = mView.findViewById(R.id.txtAddress);
        CellTower cellTower = (CellTower) marker.getTag();
        mTitle.setText(marker.getTitle());

        if(cellTower!=null) {
            txtCid.setText(String.valueOf(cellTower.getCId()));
            txtLac.setText(String.valueOf(cellTower.getLac()));
            txtMcc.setText(String.valueOf(cellTower.getMCC()));
            txtMnc.setText(String.valueOf(cellTower.getMNC()));
            txtLat.setText(String.valueOf(cellTower.getLocation().getLatitude()));
            txtLng.setText(String.valueOf(cellTower.getLocation().getLongitude()));
            txtAddress.setText(cellTower.getLocation().getAddress());
        }
        else {
            txtCid.setText(NOT_AVAILABLE);
            txtLac.setText(NOT_AVAILABLE);
            txtMcc.setText(NOT_AVAILABLE);
            txtMnc.setText(NOT_AVAILABLE);
            txtLat.setText(NOT_AVAILABLE);
            txtLat.setText(NOT_AVAILABLE);
            txtAddress.setText(NOT_AVAILABLE);
        }

        return mView;
    }
}
