package com.catsoft.android_gsm_info;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Project: android-simcard-info
 * Package: com.catsoft.android_simcard_info
 * File:
 * Created by HellCat on 27.02.2018.
 */

public class DialogInfoOk extends Dialog {

    private Context mContext;
    private String mMessage;

    private TextView mMessageTextView;
    private Button mBtnOk;

    private IntentFilter mAppFilter = null;

    public DialogInfoOk(@NonNull Context context) {
        super(context);
        mContext = context;

        this.setTitle("SIMCard Info");
        setContentView(R.layout.dialog_info_ok);

        setCancelable(false);

        mMessageTextView = findViewById(R.id.txtMessage);

        mBtnOk = findViewById(R.id.btnOK);
        mBtnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent anIntent = new Intent();
                anIntent.setAction("dialog-ok-answer");
                mContext.sendBroadcast(anIntent);
            }
        });

        mAppFilter = new IntentFilter();
        mAppFilter.addAction("dlgbox-message");
        getContext().registerReceiver(mMessageReceiver, mAppFilter);
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if("dlgbox-message".equals(intent.getAction())) {
                mMessage = (String)intent.getExtras().get("message");
                mMessageTextView.setText(mMessage);
                Intent anIntent = new Intent();
                anIntent.setAction("dialog-info-ok-ready");
                getContext().sendBroadcast(anIntent);
            }
        }
    };
}
