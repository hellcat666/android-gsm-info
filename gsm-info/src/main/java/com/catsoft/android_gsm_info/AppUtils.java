package com.catsoft.android_gsm_info;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * Project: android-gsm-info
 * Package: com.catsoft.android_gsm_info
 * File:
 * Created by HellCat on 26.03.2020.
 */
public class AppUtils {

    public static void Exit(Context context) {
        ((Activity)context).finish();
    }

    /**
     * Icon (Marker) bitmap resize tool. This will create a sized bitmap to apply in markers based on input drawable.
     */
    static Bitmap resizeIcon(Context context, int drawable, int size) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) context.getResources().getDrawable(drawable);
        Bitmap bitmap = bitmapDrawable.getBitmap();
        // Change expectedWidth's value to your desired one.
        final int expectedWidth = size;
        return Bitmap.createScaledBitmap(bitmap, expectedWidth, (bitmap.getHeight() * expectedWidth) / (bitmap.getWidth()), false);
    }
}