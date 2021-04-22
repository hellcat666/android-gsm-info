package com.catsoft.android_gsm_info;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;
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

public class UnwiredLabsService extends Service {

    private static final String TAG = "UnwiredLabsService";

    public static final String URL = "url";
    public static final String TOKEN = "token";
    public static final String UNWIREDLABS_SERVICE_READY = "unwiredlabs-service-ready";
    public static final String UNWIREDLABS_SERVICE_ERROR = "unwiredlabs-service-error";
    private static final String REQUEST_CELLTOWER_LOCATION = "request-celltower-location";
    private static final String CELLTOWER_LOCATION = "celltower-location";
    private static final String CELL = "cell";
    private static final String LOCATION = "location";

    private static final String GSM = "gsm";
    private static final String UTF8 = "utf-8";

    private static final String mServiceUrl = "https://eu1.unwiredlabs.com/v2/process.php";

    private Context mContext;

    private Looper mServiceLooper = null;               // Service Looper
    private UnwiredLabsService.ServiceHandler mServiceHandler = null;      // Service Handler
    private HandlerThread mHandlerThread = null;        // Handler Thread

    private HttpClient mHttpClient;
    private HttpPost mHttpPost;

    private String mToken;
    private String mServiceType;
    private String mResponseData;
    private CellTowerLocation mCellTowerLocation;

    private IntentFilter mAppFilter = null;
    private boolean mReceiverRegistered = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "UnwiredLabsService.onCreate()");
        this.mContext = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // To avoid cpu-blocking, we create a background handler to run our service
        mHandlerThread = new HandlerThread("LocationService", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        this.mServiceLooper = mHandlerThread.getLooper();
        // start the service using the background handler
        this.mServiceHandler = new UnwiredLabsService.ServiceHandler(this.mServiceLooper, this);

        // Unwiredlabs Communication AppSettings
        mToken = null;
        mServiceType = GSM;
        mHttpClient = new DefaultHttpClient();
        mHttpPost = new HttpPost(mServiceUrl);

        registerReceiver();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Start UnwiredLabsService");

        // Get the passed UnwiredLabs Access Token
        mToken = (String) intent.getExtras().get(TOKEN);
        // call a new service handler. The service ID can be used to identify the service

        Message message = this.mServiceHandler.obtainMessage();
        message.arg1 = startId;
        this.mServiceHandler.sendMessage(message);
        start();
        return START_NOT_STICKY;        // START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroy/Stop UnwiredLabsService");
        super.onDestroy();
    }

    private void registerReceiver() {
        Log.i(TAG, "registerReceiver()");
        if((mAppFilter==null) || (!mReceiverRegistered)) {
            mAppFilter = new IntentFilter();
            mAppFilter.addAction(REQUEST_CELLTOWER_LOCATION);
            mContext.registerReceiver(mMessageReceiver, mAppFilter);
            mReceiverRegistered = true;
        }
    }

    private void unregisterReceiver() {
        Log.i(TAG, "unregisterReceiver()");
        if (mReceiverRegistered) {
            this.unregisterReceiver(mMessageReceiver);
            mReceiverRegistered = false;
        }
    }

    private void start() {
        Log.i(TAG, "UnwiredLabsService.start()");
        Intent anIntent = new Intent();
        anIntent.setAction(UNWIREDLABS_SERVICE_READY);
        mContext.sendBroadcast(anIntent);
    }

    private void stop() {
        Log.i(TAG, "UnwiredLabsService.stop()");
    }

    public void getCellData(int cid, int lac, int mcc, int mnc) {
        Log.i(TAG, "getCellData(cid, lac, mcc, mnc)");
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

    // Object responsible for Messaging
    private final class ServiceHandler extends Handler {

        private UnwiredLabsService parent;

        public ServiceHandler(Looper looper, UnwiredLabsService parent) {
            super(looper);
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {
            // Well calling mServiceHandler.sendMessage(message); from onStartCommand,
            // this method will be called.

            // Add your cpu-blocking activity here
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null) { return; }

            switch(intent.getAction()) {
                case REQUEST_CELLTOWER_LOCATION:
                    Log.i(TAG, "REQUEST_CELLTOWER_LOCATION");
                    CellTower celltower = (CellTower) intent.getExtras().get(CELL);
                    if(celltower!=null) {
                        getCellData(celltower.getCId(), celltower.getLac(), celltower.getMCC(), celltower.getMNC());
                    }
                    break;
            }
        }
    };
}
