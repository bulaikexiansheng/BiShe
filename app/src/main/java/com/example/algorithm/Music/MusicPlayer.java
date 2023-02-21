package com.example.algorithm.Music;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.TimedMetaData;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.algorithm.CommonUtils.ToastUtil;
import com.example.algorithm.Music.Entrity.Song;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * 封装音乐播放器
 */
public class MusicPlayer {
    /**
     * 播放模式
     */
    enum PlayMode{
        RANDOM, ORDER, LIST_LOOP, SINGLE_LOOP
    }
    private static PlayMode currentMode = PlayMode.ORDER ;
    public static final boolean NO_PLAYING = false ;
    private static MusicPlayer instance ;
    private MediaPlayer mediaPlayer ;
    private boolean isNotPrepared;
    private boolean isPlaying ;
    private static List<Song> songList ;
    private static int currentSongIndex = 0;
    private Context mContext ;
    private VolumeManager volumeManager ;
    private MusicPlayer(Context mContext){
        this.mContext = mContext ;
        isNotPrepared = true ;
        isPlaying = false ;
        mediaPlayer = new MediaPlayer() ;
        // 设置自动播放
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                getNextSongIndex();
                mediaPlayer.reset();
                isNotPrepared = true ;
                try {
                    if(currentSongIndex != -1)
                        playSong();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        volumeManager = VolumeManager.getInstance(mContext) ;
    }

    /**
     * 设置播放模式
     */
    public PlayMode changeCurrentMode() {
        if (currentMode == PlayMode.LIST_LOOP)
            currentMode = PlayMode.ORDER ;
        else if (currentMode == PlayMode.ORDER)
            currentMode = PlayMode.RANDOM;
        else if (currentMode == PlayMode.RANDOM)
            currentMode = PlayMode.SINGLE_LOOP;
        else if (currentMode == PlayMode.SINGLE_LOOP)
            currentMode = PlayMode.LIST_LOOP;
        ToastUtil.showShortMessage(mContext,currentMode.name());
        return currentMode ;
    }

    /**
     * 获得一个音乐播放器
     * @param mContext 上下文
     * @return 实例
     */
    public static MusicPlayer getInstance(Context mContext){
        if (instance == null){
            synchronized (MusicPlayer.class){
                if (instance == null) {
                    instance = new MusicPlayer(mContext) ;
                }
            }
        }
        return instance ;
    }
    /**
     * 导入音乐列表
     * @param list 音乐列表
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void importMusicList(List<Song> list){
        songList = list ;
    }

    // TODO: 2023/2/15 下一首歌 
    public int nextSong() throws IOException {
        mediaPlayer.reset();
        isNotPrepared = true ;
        currentSongIndex += 1 ;
        currentSongIndex %= songList.size() ;
        playSong();
        return currentSongIndex ;
    }

    // TODO: 2023/2/15 上一首歌 
    public int lastSong() throws IOException {
        mediaPlayer.reset();
        isNotPrepared = true ;
        currentSongIndex -= 1 ;
        if (currentSongIndex == -1) currentSongIndex = songList.size() - 1 ;
        playSong();
        return currentSongIndex ;

    }

    // TODO: 2023/2/15 暂停 
    public int pauseSong(){
        mediaPlayer.pause();
        isPlaying = false ;
        return currentSongIndex ;
    }

    // TODO: 2023/2/15 播放歌曲
    public int
    playSong() throws IOException {
        if (isNotPrepared){
            mediaPlayer.setDataSource(songList.get(currentSongIndex).savePath);
            mediaPlayer.prepare();
            isNotPrepared = false ;
        }
        mediaPlayer.start();
        isPlaying = true ;
        return currentSongIndex ;
    }

    /**
     * 获得播放状态
     * @return 获得播放状态
     */
    public boolean getState(){
        return isPlaying ;
    }
    // TODO: 2023/2/15 音量++
    public void volumeUp(int stride){
        volumeManager.setMediaVolume(volumeManager.getMediaVolume() + stride);
    }

    // TODO: 2023/2/15 音量--
    public void volumeDown(int stride){
        volumeManager.setMediaVolume(volumeManager.getMediaVolume() - stride);
    }

    // TODO: 2023/2/15 喜欢这首歌
    public void loveSong(){

    }

    /**
     * 根据播放模式选择播放的歌曲的
     */
    private void getNextSongIndex(){
        switch (currentMode){
            case SINGLE_LOOP: break ;
            case LIST_LOOP: {
                currentSongIndex += 1;
                currentSongIndex %= songList.size() ;
                break ;
            }
            case RANDOM:{
                currentSongIndex = new Random().nextInt(songList.size()) ;
                break ;
            }
            case ORDER:{
                currentSongIndex += 1;
                if (currentSongIndex > songList.size()) currentSongIndex = -1 ;
                break ;
            }
        }

    }
}
