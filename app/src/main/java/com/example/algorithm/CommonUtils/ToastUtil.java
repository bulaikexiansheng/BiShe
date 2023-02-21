package com.example.algorithm.CommonUtils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private static Toast mToast ;
    public static void showShortMessage(Context mContext,String message){
        if (mToast == null)
            mToast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT) ;
        else
            mToast.setText(message);
        mToast.show() ;
    }
    public static void showLongMessage(Context mContext,String message){
        if (mToast == null)
            mToast = Toast.makeText(mContext, message, Toast.LENGTH_LONG) ;
        else
            mToast.setText(message);
        mToast.show() ;
    }
}
