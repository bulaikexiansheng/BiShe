package com.example.algorithm.Main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.algorithm.Base.BaseActivity;
import com.example.algorithm.Bluetooth.BluetoothChatService;
import com.example.algorithm.Bluetooth.HeadAndBody;
import com.example.algorithm.Bluetooth.MessageHandler;
import com.example.algorithm.Bluetooth.PythonModel;
import com.example.algorithm.ControlCenter.ControllerCenter;
import com.example.algorithm.ControlCenter.OnControllerListener;
import com.example.algorithm.Main.UI.BluetoothListAdapter;
import com.example.algorithm.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends BaseActivity {
    // TODO: 组件声明区
    private LinearLayout test_Button; // 测试button
    private RecyclerView blueDevices_RecycleView;
    private Button toMainActivityButton ;

    // TODO: 变量声明区
    private BluetoothListAdapter blueDevicesAdapter;
    // 蓝牙功能模块
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter adapter;
    private BluetoothChatService chatService;
    private List<BluetoothDevice> boundedDevices;
    private final int REQUEST_PERMISSION_CODE = 1001;

    /**
     * 初始化Python环境
     */
    private void checkPythonEnvironment(){
        PythonModel.initPythonEnvironment(this) ;
        Thread thread = new Thread(new PythonModel.ResultRunnable(this)) ; // 动作监听
        thread.start();
    }

    /**
     * 检查危险权限是否打开
     */
    private void verifyDangerPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            List<String> mPermissionList = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mPermissionList.add(Manifest.permission.BLUETOOTH_SCAN);
                mPermissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
                mPermissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
            } else {
                mPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                mPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            ActivityCompat.requestPermissions(this, mPermissionList.toArray(new String[0]), REQUEST_PERMISSION_CODE);

        }
    }
    public static void actionStart(Context mContext){
        Intent intent = new Intent(mContext,BluetoothActivity.class) ;
        mContext.startActivity(intent);
    }
    /**
     * 获得手机已配对的蓝牙设备
     */
    @SuppressLint("MissingPermission")
    private void enableBluetooth() {
        verifyDangerPermissions(); // 申请蓝牙权限
        adapter.enable();
        boundedDevices = new ArrayList<>(adapter.getBondedDevices()) ;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponentView();
        initBlueTooth() ;
        registerOnClickEvents();
        checkPythonEnvironment();
        registerControllerCenter();
    }

    private void initBlueTooth() {
        // bluetooth
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = bluetoothManager.getAdapter() ;
        chatService = new BluetoothChatService(BluetoothActivity.this,new MessageHandler(BluetoothActivity.this)) ;
        enableBluetooth() ; // 启动蓝牙
        blueDevices_RecycleView.setLayoutManager(new LinearLayoutManager(this));
        blueDevicesAdapter = new BluetoothListAdapter(this,boundedDevices);
        blueDevicesAdapter.setOnItemClickListener(new BluetoothListAdapter.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(BluetoothActivity.this,"您想连接蓝牙 "+boundedDevices.get(position).getName(),Toast.LENGTH_SHORT).show();
                chatService.connect(boundedDevices.get(position),true); // 蓝牙连接
            }
        }) ;
        blueDevices_RecycleView.setAdapter(blueDevicesAdapter);
    }

    /**
     * 初始化组件布局
     */
    @Override
    protected void initComponentView() {
        test_Button = findViewById(R.id.test_Button) ;
        blueDevices_RecycleView = findViewById(R.id.bluetooth_RecycleView) ; // 蓝牙设备列表
        toMainActivityButton = findViewById(R.id.to_main_activity_button) ;
    }

    /**
     * 注册点击事件
     */
    @Override
    protected void registerOnClickEvents() {
        test_Button.setOnClickListener(this);
        toMainActivityButton.setOnClickListener(this);
    }

    @Override
    protected void registerControllerCenter() {
        center.register(this, new OnControllerListener() {
            @Override
            public Message onControl(int headCode, int bodyCode) {
                Message message = new Message() ;
                Bundle bundle = new Bundle() ;
                bundle.putString(ControllerCenter.TOAST, HeadAndBody.getHeadAction(headCode)+
                        "\n"+HeadAndBody.getBodyAction(bodyCode));
                message.setData(bundle);
                message.what = ControllerCenter.CALL_ON_TOAST ;
                return message ;
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.test_Button:{
                // TODO: 2023/1/17 测试函数
                break ;
            }
            case R.id.to_main_activity_button:{
                MainPaneActivity.actionStart(this);
                break ;
            }
            default:
                break ;
        }
    }
}