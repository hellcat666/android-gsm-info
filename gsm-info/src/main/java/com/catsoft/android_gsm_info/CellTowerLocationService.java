package com.catsoft.android_gsm_info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 03.04.2018.
 */

public class CellTowerLocationService {

    private static final String TAG = "GSMInfo-CellTowerLocationService";

    private static final String LOCATION_SERVICE_READY = "location-service-ready";
    private static final String REQUEST_CELLTOWER_LOCATION = "request-celltower-location";
    private static final String CELLTOWER_LOCATION = "celltower-location";
    private static final String CELL = "cell";
    private static final String LOCATION = "location";

    private static final String GSM = "gsm";
    private static final String UTF8 = "utf-8";

    private static final String mServiceUrl = "https://eu1.unwiredlabs.com/v2/process.php";

    private Context mContext;

    private HttpClient mHttpClient;
    private HttpPost mHttpPost;

    private String mToken;
    private String mServiceType;
    private String mResponseData;
    private CellTowerLocation mCellTowerLocation;

    private IntentFilter mAppFilter;

    public CellTowerLocationService(Context context, String token) {
        mContext = context;
        mToken = token;
        mServiceType = GSM;
        mHttpClient = new DefaultHttpClient();
        mHttpPost = new HttpPost(mServiceUrl);
        mAppFilter = new IntentFilter();
        mAppFilter.addAction(REQUEST_CELLTOWER_LOCATION);
        mContext.registerReceiver(mMessageReceiver, mAppFilter);

        Intent anIntent = new Intent();
        anIntent.setAction(LOCATION_SERVICE_READY);
        mContext.sendBroadcast(anIntent);


    }

    public void getCellData(int cid, int lac, int mcc, int mnc) {

        mResponseData = null;
        try {
            String data = "{\"token\": \"" + mToken + "\"," +
                    "\"radio\": \"" + mServiceType + "\"," +
                    "\"mcc\": " + String.valueOf(mcc) + "," +
                    "\"mnc\": " + String.valueOf(mnc) + "," +
                    "\"cells\": [{\"lac\": " + String.valueOf(lac) + ",\"cid\": " + String.valueOf(cid) + "}]," +
                    "\"address\": 1}";

//            String test_data = "{\"token\": \"" + mToken + "\"," +
//                    "\"radio\": \"" + mServiceType + "\"," +
//                    "\"mcc\": 228," +
//                    "\"mnc\": 2," +
//                    "\"cells\": [{\"lac\": 17000,\"cid\": 31721}]," +
//                    "\"address\": 1}";

            Toast.makeText(mContext, "Request location for Cell " + String.valueOf(cid), Toast.LENGTH_LONG);


            mHttpPost.setEntity(new StringEntity(data, UTF8));
            HttpResponse response = mHttpClient.execute(mHttpPost);
            mResponseData = EntityUtils.toString(response.getEntity(), UTF8);
            mCellTowerLocation = new CellTowerLocation(mResponseData);

            Intent anIntent = new Intent();
            anIntent.setAction(CELLTOWER_LOCATION);
            anIntent.putExtra(LOCATION, mCellTowerLocation);
            mContext.sendBroadcast(anIntent);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        } finally {
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null) { return; }

            switch(intent.getAction()) {
                case REQUEST_CELLTOWER_LOCATION:
                    CellTower cell = (CellTower) intent.getExtras().get(CELL);
                    if(cell!=null) {
                        getCellData(cell.getCId(), cell.getLac(), cell.getMCC(), cell.getMNC());
                    }
                    break;
            }
        }
    };


}
