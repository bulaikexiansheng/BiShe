package com.example.algorithm.ControlCenter;

import android.os.Message;

public abstract class OnControllerListener {
    int requestCode;
    public OnControllerListener(){
    }
   public abstract Message onControl(int headCode, int bodyCode) ;
}
