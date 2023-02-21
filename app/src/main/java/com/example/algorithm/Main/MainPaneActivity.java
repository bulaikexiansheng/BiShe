package com.example.algorithm.Main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.example.algorithm.Base.BaseActivity;
import com.example.algorithm.Bluetooth.HeadAndBody;
import com.example.algorithm.ControlCenter.ControllerCenter;
import com.example.algorithm.ControlCenter.OnControllerListener;
import com.example.algorithm.Music.MusicActivity;
import com.example.algorithm.R;
import com.example.algorithm.Video.VideoActivity;

public class MainPaneActivity extends BaseActivity {
    private Button toMusicButton ;
    private Button toVideoButton ;
    private Button toBluetoothButton ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pane);
        registerControllerCenter();
        initComponentView();
        registerOnClickEvents();
    }

    @Override
    protected void initComponentView() {
        toMusicButton = findViewById(R.id.to_music_activity_button) ;
        toVideoButton = findViewById(R.id.to_video_activity_button) ;
        toBluetoothButton = findViewById(R.id.to_bluetooth_button) ;
    }

    @Override
    protected void registerOnClickEvents() {
        toMusicButton.setOnClickListener(this);
        toVideoButton.setOnClickListener(this);
        toBluetoothButton.setOnClickListener(this);
    }

    @Override
    protected void registerControllerCenter() {
        center.register(this, new OnControllerListener() {
            @Override
            public Message onControl(int headCode, int bodyCode) {
                Message message = new Message() ;
                Bundle bundle = new Bundle() ;
                bundle.putString(ControllerCenter.TOAST,HeadAndBody.getHeadAction(headCode)+
                        "\n"+HeadAndBody.getBodyAction(bodyCode));
                message.setData(bundle);
                message.what = ControllerCenter.CALL_ON_TOAST ;
                return message ;
            }
        });
    }

    public static void actionStart(Context mContext){
        Intent intent = new Intent(mContext,MainPaneActivity.class) ;
        mContext.startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.to_music_activity_button:{
                MusicActivity.actionStart(this);
                break ;
            }
            case R.id.to_video_activity_button:{
                VideoActivity.actionStart(this);
                break ;
            }
            case R.id.to_bluetooth_button:{
                BluetoothActivity.actionStart(this);
                break ;
            }
        }
    }
}