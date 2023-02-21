package com.example.algorithm.Bluetooth;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.algorithm.ControlCenter.ControllerCenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PythonModel {
    private Context mContext ;
    private static PythonModel model ;
    private static Python python ;
    // Objects
    private static PyObject  predictObject ;
    private static PyObject predictThreadObject;
    public static final int TOAST_TEXT = 1;
    private static final Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case TOAST_TEXT:{

                    Toast.makeText(model.getContext(), msg.getData().getString("tip"),Toast.LENGTH_SHORT).show(); ;
                    break ;
                }
                default: break ;
            }
        }
    } ;

    public Context getContext() {
        return mContext;
    }

    private PythonModel(Context mContext){
        this.mContext = mContext ;
        checkPythonEnvironment();
        initPythonBaseObjects() ;
    }

    /**
     * 初始化一些必备的基础类
     */
    private void initPythonBaseObjects() {
        predictObject = python.getModule("predict") ;
        predictThreadObject = predictObject.callAttr("PredictThread") ;
        predictThreadObject.callAttr("start") ;
    }

    /**
     * 初始化Python的运行环境
     * @param mContext 上下文
     */
    public static void initPythonEnvironment(Context mContext) {
        if (model == null)
            model = new PythonModel(mContext);
    }
    /**
     * 检查Python的环境是否开启
     */
    private void checkPythonEnvironment(){
        if (!Python.isStarted()){
            Python.start(new AndroidPlatform(mContext));
            python = Python.getInstance() ;
        }
    }
    public static void predict(String raw){
        predictThreadObject.callAttr("predict",raw) ;
    }

    /**
     * 负责监听有动作的答复
     */
    public static class ResultRunnable implements Runnable{
        private String dateString = null ;
        private Context mContext = null ;

        public ResultRunnable(Context mContext){
            this.mContext = mContext ;
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00")) ;
            calendar.setTime(new Date());
            int year = calendar.get(Calendar.YEAR) ;
            int month = calendar.get(Calendar.MONTH) + 1  ;
            Log.d(TAG, "ResultRunnable: month "+month);

            int date = calendar.get(Calendar.DATE) ;
            Log.d(TAG, "ResultRunnable: date "+date);

            int hour = calendar.get(Calendar.HOUR) ;
            int minute = calendar.get(Calendar.MINUTE) ;
            int second = calendar.get(Calendar.SECOND) ;
            this.dateString = "" + year + "_" + month + "_" + date
                                + "_" + hour + "_" + minute + "_" + second;
        }
        @Override
        public void run() {
            Log.d(TAG, "run: 开始监听结果");
            while(true){
                PyObject res = predictObject.callAttr("getResult") ;
                Float []result = res.toJava(Float[].class) ;
                Log.d(TAG, "run: "+ Arrays.toString(result));
                int headCode = Math.round(result[0]) ;
                int bodyCode = Math.round(result[1]) ;
                ControllerCenter.getInstance().control(headCode,bodyCode) ;
            }
        }
    }
    public static class ResultRecordRunnable implements Runnable{
        private final String fileName ;
        private final String []content ;
        private Context mContext ;
        public ResultRecordRunnable(String fileName ,Context mContext, String ...row){
            this.content = row ;
            this.mContext = mContext ;
            this.fileName = fileName + ".txt" ;
        }
        @Override
        public void run() {
            saveData(); // 保存数据
        }
        private void saveData(){
            try {
                // 创建包名
                File directoryToStore;
                directoryToStore = mContext.getExternalFilesDir(null);
                if (!directoryToStore.exists()) {
                    directoryToStore.mkdir() ; //directory is created;
                }
                // 创建文件
                File file = new File(directoryToStore.getPath()+File.separator+fileName) ;
                if (!file.exists())
                    file.createNewFile() ;
                FileOutputStream fileOutputStream = new FileOutputStream(file,true) ;
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream)) ;
                for (String i:content){
                    writer.write(i);
                    writer.write(" ");
                }
                writer.write("\n");
                writer.flush();
                writer.close();
                Log.d(TAG, "saveData: "+"文件已保存");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
