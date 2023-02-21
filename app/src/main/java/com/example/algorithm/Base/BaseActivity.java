package com.example.algorithm.Base;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.example.algorithm.ControlCenter.ControllerCenter;
import com.example.algorithm.ControlCenter.OnControllerListener;
import com.example.algorithm.R;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    protected ControllerCenter center ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        center = ControllerCenter.getInstance() ;
    }

    /**
     * 初始化组件的布局
     */
    protected abstract void initComponentView() ;

    /**
     * 注册点击事件
     */
    protected abstract void registerOnClickEvents() ;

    /**
     * 注册控制中心
     */
    protected  abstract void registerControllerCenter() ;
}