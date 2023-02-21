package com.example.algorithm.Music;

import android.content.Context;
import android.media.AudioManager;

public class VolumeManager {
    private AudioManager manager ;
    private static VolumeManager instance ;
    private Context mContext ;
    public final int VOLUME_CHANGE_STRIDE = 5;
    private VolumeManager(Context mContext){
        this.mContext = mContext ;
        manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }
    public static VolumeManager getInstance(Context context){
        if (instance == null){
            synchronized (VolumeManager.class){
                if (instance == null){
                   instance = new VolumeManager(context) ;
                }
            }
        }
        return instance ;
    }

    //获取多媒体最大音量
    public int getMediaMaxVolume(){
        return manager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
    }

    //获取多媒体音量
    public int getMediaVolume(){
        return manager.getStreamVolume( AudioManager.STREAM_MUSIC );
    }

    /**
     * 设置多媒体音量
     */
    public void setMediaVolume(int volume){
        if (volume > 100) volume = 100 ;
        else if (volume < 0) volume = 0 ;
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, //音量类型
                volume,
                AudioManager.FLAG_PLAY_SOUND
                        | AudioManager.FLAG_SHOW_UI);
    }

}
