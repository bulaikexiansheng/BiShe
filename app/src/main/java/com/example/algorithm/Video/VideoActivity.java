package com.example.algorithm.Video;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import com.example.algorithm.Base.BaseActivity;
import com.example.algorithm.CommonUtils.ToastUtil;
import com.example.algorithm.ControlCenter.Controller;
import com.example.algorithm.ControlCenter.OnControllerListener;
import com.example.algorithm.Music.VolumeManager;
import com.example.algorithm.R;
import com.example.algorithm.Video.ui.CusVideoView;
import com.example.algorithm.Video.ui.CustomLayoutManager;
import com.example.algorithm.Video.ui.OnPageSlideListener;
import com.example.algorithm.Video.ui.TiktokAdapter;

public class VideoActivity extends BaseActivity {
    private static final String TAG = VideoActivity.class.getSimpleName();
    private RecyclerView mRecycler;
    private CustomLayoutManager mLayoutManager;
    private final Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case CustomLayoutManager.PAGE_CHANGE:{
                    Bundle bundle = msg.getData() ;
                    mLayoutManager.pageChange(mRecycler,bundle.getBoolean("type")); // 翻页
                    ToastUtil.showShortMessage(VideoActivity.this,"翻页");
                    break ;
                }
                case CustomLayoutManager.VIDEO_PAUSE:{
                    View itemView = mRecycler.getChildAt(0) ;
                    final CusVideoView mVideoView = itemView.findViewById(R.id.mVideoView) ;
                    final ImageView mPlay = itemView.findViewById(R.id.mPlay) ;
                    mPlay.animate().alpha(1F).start();
                    mVideoView.pause();
                    ToastUtil.showShortMessage(VideoActivity.this,"暂停");
                    break ;
                }
                case CustomLayoutManager.VIDEO_PLAYING:{
                    View itemView = mRecycler.getChildAt(0) ;
                    final CusVideoView mVideoView = itemView.findViewById(R.id.mVideoView) ;
                    final ImageView mPlay = itemView.findViewById(R.id.mPlay) ;
                    mPlay.animate().alpha(0F).start();
                    mVideoView.start();
                    ToastUtil.showShortMessage(VideoActivity.this,"开播");
                    break ;
                }
            }
        }
    } ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initComponentView();
        registerOnClickEvents();
        registerControllerCenter();
        initListener() ;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initListener();
    }

    /**
     * 开视频
     * @param mContext 上下文
     */
    public static void actionStart(Context mContext){
        Intent intent = new Intent(mContext,VideoActivity.class) ;
        mContext.startActivity(intent);

    }
    /**
     * 播放当前页面下的视频
     */
    private void playVideo() {
        View itemView = mRecycler.getChildAt(0) ;
        final CusVideoView mVideoView = itemView.findViewById(R.id.mVideoView) ;
        final ImageView mPlay = itemView.findViewById(R.id.mPlay) ;
        final ImageView mThumb = itemView.findViewById(R.id.mThumb);
        final MediaPlayer[] mMediaPlayer = new MediaPlayer[1];
        mVideoView.start();
        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                mMediaPlayer[0] = mp;
                mp.setLooping(true);
                mThumb.animate().alpha(0).setDuration(200).start();
                return false;
            }
        });

        //暂停控制
        mPlay.setOnClickListener(new View.OnClickListener() {
            boolean isPlaying = true;
            @Override
            public void onClick(View v) {
                if (mVideoView.isPlaying()) {
                    mPlay.animate().alpha(1f).start();
                    mVideoView.pause();
                    isPlaying = false;
                } else {
                    mPlay.animate().alpha(0f).start();
                    mVideoView.start();
                    isPlaying = true;
                }
            }
        });
    }

    /**
     * 释放视频
     * @param index id
     */
    private void releaseVideo(int index) {
        View itemView = mRecycler.getChildAt(index);
        final CusVideoView mVideoView = itemView.findViewById(R.id.mVideoView);
        final ImageView mThumb = itemView.findViewById(R.id.mThumb);
        final ImageView mPlay = itemView.findViewById(R.id.mPlay);
        mVideoView.stopPlayback();
        mThumb.animate().alpha(1).start();
        mPlay.animate().alpha(0f).start();
    }

    //初始化监听
    private void initListener() {
        mLayoutManager.setOnPageSlideListener(new OnPageSlideListener() {
            @Override
            public void onPageRelease(boolean isNext, int position) {
                int index;
                if (isNext) {
                    index = 0;
                } else {
                    index = 1;
                }
                releaseVideo(index);
            }
            @Override
            public void onPageSelected(int position, boolean isBottom) {
                playVideo();
            }
        });
    }

    @Override
    protected void initComponentView() {
        mRecycler = findViewById(R.id.mRecycler);
        mLayoutManager = new CustomLayoutManager(this, OrientationHelper.VERTICAL, false);
        TiktokAdapter mAdapter = new TiktokAdapter(this);
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    protected void registerOnClickEvents() {

    }

    @Override
    protected void registerControllerCenter() {
        center.register(this, new OnControllerListener() {
            @Override
            public Message onControl(int headCode, int bodyCode) {
                switch(headCode){
                    case Controller.HEAD_LEAN_LEFT:{
                        volumeDown();
                        break ;
                    }
                    case Controller.HEAD_LEAN_RIGHT:{
                        volumeUp();
                        break ;
                    }
                    case Controller.HEAD_DOWN:{
                        // TODO: 2023/2/20 视频暂停/播放
                        Message message = new Message() ;
                        CusVideoView view = mRecycler.getChildAt(0).findViewById(R.id.mVideoView) ;
                        if (view.isPlaying()){
                            message.what = CustomLayoutManager.VIDEO_PAUSE ;
                        } else {
                            message.what = CustomLayoutManager.VIDEO_PLAYING ;
                        }
                        handler.sendMessage(message) ;
                        break ;
                    }
                    case Controller.HEAD_LEFT:{
                        // TODO: 2023/2/20 观看该作者的所有作品
                        break ;
                    }
                    case Controller.HEAD_RIGHT:{
                        // TODO: 2023/2/20 个人主页

                    }
                    case Controller.HEAD_DOWN_TWICE:{
                        // TODO: 2023/2/20 下拉一个视频
                        Message message = new Message() ;
                        message.what = CustomLayoutManager.PAGE_CHANGE ;
                        Bundle bundle = new Bundle() ;
                        bundle.putBoolean("type",true);
                        message.setData(bundle);
                        handler.sendMessage(message) ;
                        break ;
                    }
                    case Controller.HEAD_UP:{
                        // TODO: 2023/2/20  上移一个视频
                        Message message = new Message() ;
                        message.what = CustomLayoutManager.PAGE_CHANGE ;
                        Bundle bundle = new Bundle() ;
                        bundle.putBoolean("type",false);
                        message.setData(bundle);
                        handler.sendMessage(message) ;
                        break ;
                    }
                    case Controller.HEAD_UP_TWICE:{
                        // TODO: 2023/2/20 喜欢这个视频
                    }
                    case Controller.HEAD_LEFT_RIGHT:{

                    }
                    case Controller.HEAD_RIGHT_LEFT:{
                        // TODO: 2023/2/20 不喜欢这个视频
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * increase the volume
     */
    private void volumeUp(){
        VolumeManager instance = VolumeManager.getInstance(this) ;
        int total = instance.getMediaMaxVolume() ;
        total += 3 ;
        if (total > 100) total = 100 ;
        instance.setMediaVolume(total);
    }

    /**
     * decrease the volume
     */
    private void volumeDown(){
        VolumeManager instance = VolumeManager.getInstance(this) ;
        int total = instance.getMediaMaxVolume() ;
        total -= 3 ;
        if (total < 0) total = 0 ;
        instance.setMediaVolume(total);
    }
}