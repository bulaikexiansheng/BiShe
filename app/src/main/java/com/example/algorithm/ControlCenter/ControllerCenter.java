package com.example.algorithm.ControlCenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.example.algorithm.CommonUtils.ToastUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ControllerCenter implements Controller{
    public static final int CALL_ON_TOAST = 1 ;
    public static final String TOAST = "TOAST" ;
    private Context currentContext ;
    private static ControllerCenter instance ;
    private static Map<Context, OnControllerListener> contextMap ;
    private static  Handler controlHandler ;
    private ControllerCenter(){
        contextMap = new HashMap<>() ;
        controlHandler = new Handler(Looper.myLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case CALL_ON_TOAST:{
                        Bundle data = msg.getData();
                        String str = data.getString(TOAST) ;
                        ToastUtil.showShortMessage(currentContext,str);
                        break ;
                    }
                }
            }
        } ;
    }

    /**
     * single mode
     * @return  control center
     */
    public static ControllerCenter getInstance(){
        if (instance == null){
            synchronized (ControllerCenter.class){
                if (instance == null){
                    instance = new ControllerCenter() ;
                }
            }
        }
        return instance ;
    }

    /**
     * register the activity here
     * @param context activity
     * @param listener the action when get the predict result
     */
    public void register(Context context,OnControllerListener listener){
        contextMap.put(context,listener) ;
        currentContext = context ;
    }

    @Override
    public void control(int headCode,int bodyCode) {
        Message resMessage = Objects.requireNonNull(contextMap.get(currentContext)).onControl(headCode,bodyCode);
        if (resMessage != null)
            controlHandler.sendMessage(resMessage) ;

    }

}
