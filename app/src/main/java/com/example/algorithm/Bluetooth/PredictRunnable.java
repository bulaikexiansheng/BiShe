package com.example.algorithm.Bluetooth;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.util.concurrent.BlockingQueue;

public class PredictRunnable implements Runnable{
    private final BlockingQueue<String> blockingQueue ;
    public PredictRunnable(BlockingQueue<String> blockingQueue){
        this.blockingQueue = blockingQueue ;
    }
    @Override
    public void run() {
        while(true){
            try {
                String data = blockingQueue.take() ; // 从阻塞队列中取出数据
                PythonModel.predict(data) ;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
