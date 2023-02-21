package com.example.algorithm.Bluetooth;


import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageHandler extends Handler {
    private final WeakReference<Activity> mWeakReference;
    private final BlockingQueue<String> blockingQueue ;
    private ExecutorService singleThreadPool ;
    private PredictRunnable predictRunnable ;
    public MessageHandler(Activity activity) {
        mWeakReference = new WeakReference<>(activity);
        singleThreadPool = Executors.newSingleThreadExecutor();
        blockingQueue = new ArrayBlockingQueue<>(1) ;
        predictRunnable = new PredictRunnable(blockingQueue) ;
        singleThreadPool.submit(predictRunnable) ;
    }
    @Override
    public void handleMessage(Message msg) {
        final Activity activity = mWeakReference.get();
        if (activity != null) {
            // TODO: 2021/8/2 test
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            break;
                        case BluetoothChatService.STATE_CONNECTING:{
                            msg.what = Constants.MESSAGE_WRITE ;
                            break;
                        }
                        case BluetoothChatService.STATE_LISTEN:
                            break ;
                        case BluetoothChatService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    // 这里获得蓝牙传输的原始字符串
                    blockingQueue.offer(Message.obtain(msg).getData().getString("BTdata")) ;
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (activity != null) {
                        Toast.makeText(activity,"Connected to"+mConnectedDeviceName,Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity,msg.getData().getString(Constants.TOAST),Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

        }
    }

}
