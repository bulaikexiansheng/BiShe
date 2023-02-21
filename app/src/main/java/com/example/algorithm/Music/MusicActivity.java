package com.example.algorithm.Music;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.algorithm.Base.BaseActivity;
import com.example.algorithm.CommonUtils.ToastUtil;
import com.example.algorithm.ControlCenter.Controller;
import com.example.algorithm.ControlCenter.ControllerCenter;
import com.example.algorithm.ControlCenter.OnControllerListener;
import com.example.algorithm.DataBase.MyDataBase;
import com.example.algorithm.Music.Entrity.Song;
import com.example.algorithm.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MusicActivity extends BaseActivity {
    public static final int CHANGE_SONG_MESSAGE = 4 ;
    private boolean currentLove = false;
    // UI组件
    private CircleImageView albumCircleImage ;
    private ImageButton musicStartStopButton ;
    private ImageButton nextSongButton ;
    private ImageButton lastSongButton ;
    private ImageButton musicModeButton ;
    private ImageButton loveSongButton ;
    private TextView artistNameTextView ;
    private TextView  songNameTextView ;

    // 功能区
    private MusicPlayer musicPlayer ;
    private MyDataBase myDataBase ;
    private static List<Song> songList = null ;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        initComponentView();
        verityNeedPermissions();
        registerControllerCenter() ;
        initDataBase();
        initMusicPlayer() ;
        registerOnClickEvents();
    }

    /**
     * 获得专辑界面
     * @param id albumId
     */
    private void getImage(int id) {
        int album_id = id;
        String albumArt = getAlbumArt(album_id);
        Bitmap bm = null;
        if (albumArt == null) {
            albumCircleImage.setBackgroundResource(R.drawable.zxm);
        } else {
            bm = BitmapFactory.decodeFile(albumArt);
            BitmapDrawable bmpDraw = new BitmapDrawable(bm);
            albumCircleImage.setImageDrawable(bmpDraw);
        }
    }

    private String getAlbumArt(int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[] { "album_art" };
        Cursor cur = this.getContentResolver().query(  Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),  projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;
        return album_art;
    }
    /**
     * 申请必须权限
     */
    private void verityNeedPermissions () {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            List<String> mPermissionList = new ArrayList<>();
            mPermissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE) ;
            mPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE) ;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                mPermissionList.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE) ;
            }
            ActivityCompat.requestPermissions(this, mPermissionList.toArray(new String[0]), 666);
        }
    }

    /**
     * 初始化数据库,如果歌单为空，就从数据库导入；
     * 若导入为空，说明此时数据库为空，则搜索本地音乐文件，再将音乐导入数据库
     */
    private void initDataBase() {
        myDataBase = MyDataBase.getInstance(this) ;
    }

    /**
     * 向handler发送改变歌曲消息的命令
     * @param song
     */
    private void changeSongMessage(Song song){
        Message changeSongMessage = new Message() ;
        changeSongMessage.what = CHANGE_SONG_MESSAGE ;
        Bundle data = new Bundle() ;
        data.putString("songName",song.songName);
        data.putString("artist",song.singer);
        data.putInt("album",song.albumId);
        changeSongMessage.setData(data);
        handler.sendMessage(changeSongMessage) ;
    }

    /**
     * 搜索本地音乐
     */
    private void searchNativeMusicFiles() {
        Runnable musicSearch = () -> {
            songList = myDataBase.songDao().getSongList() ;
            if (songList != null && songList.size() != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    musicPlayer.importMusicList(songList); // 导入音乐文件
                    changeSongMessage(songList.get(0));
                }
                return ;
            }
            try (Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null, null,null,MediaStore.Audio.Media.IS_MUSIC)
            ){
                if (cursor!=null){
                    while(cursor.moveToNext()) {
                        long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)) ;
                        if (size / 8 / 1024 > 1500) {
                            //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                            String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                            Log.d(TAG, "searchNativeMusicFiles: "+url);
                            if (url.endsWith(".ogg") || url.endsWith(".mp3")){
                                //歌曲名
                                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                                Log.d(TAG, "searchNativeMusicFiles: "+name);
                                //歌曲编号
                                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                                //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                                int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                                //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                                //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
                                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                                songList.add(new Song(id, name, artist, url, duration, albumId));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (Song s:songList){
                myDataBase.songDao().insertSong(s);  // 导入数据库
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                musicPlayer.importMusicList(songList); // 导入音乐文件
                changeSongMessage(songList.get(0));
            }
        };
        new Thread(musicSearch).start();
    }

    /**
     * 初始化音乐播放器
     */
    private void initMusicPlayer() {
        musicPlayer = MusicPlayer.getInstance(this) ;
        searchNativeMusicFiles() ;
    }

    @Override
    protected void initComponentView() {
        albumCircleImage = findViewById(R.id.album_circle_picture) ;
        // 让图片匀速旋转
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.img_animation) ;
        LinearInterpolator lin = new LinearInterpolator() ;
        animation.setInterpolator(lin);
        albumCircleImage.startAnimation(animation);

        musicStartStopButton = findViewById(R.id.music_play_start_button) ;
        nextSongButton = findViewById(R.id.next_song_button) ;
        lastSongButton = findViewById(R.id.last_song_button) ;
        musicModeButton = findViewById(R.id.music_mode_button) ;
        loveSongButton = findViewById(R.id.love_song_button) ;

        artistNameTextView = findViewById(R.id.artist_name_textview) ;
        songNameTextView = findViewById(R.id.song_name_textview);

        handler = new Handler(Looper.myLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case CHANGE_SONG_MESSAGE:{
                        Bundle data = msg.getData() ;
                        String songName = data.getString("songName") ;
                        String artist = data.getString("artist") ;
                        int albumId = data.getInt("albumId") ;
                        artistNameTextView.setText(artist);
                        songNameTextView.setText(songName);
                        // TODO: 2023/2/21 专辑设置这里有问题
//                        getImage(albumId);
                        break ;
                    }
                }
            }
        } ;
    }

    @Override
    protected void registerOnClickEvents() {
        musicStartStopButton.setOnClickListener(this);
        nextSongButton.setOnClickListener(this);
        lastSongButton.setOnClickListener(this);
        musicModeButton.setOnClickListener(this);
        loveSongButton.setOnClickListener(this);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.music_play_start_button:{
                try {
                    playOrPauseSong() ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break ;
            }
            case R.id.next_song_button:{
                try {
                   int curr = musicPlayer.nextSong();
                    Log.d(TAG, "onClick: "+curr);
                   changeSongMessage(songList.get(curr));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break ;
            }
            case R.id.last_song_button:{
                try {
                    int curr = musicPlayer.lastSong();
                    changeSongMessage(songList.get(curr));

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break ;
            }
            case R.id.music_mode_button:{
                musicPlayer.changeCurrentMode();
                break ;
            }
            case R.id.love_song_button:{
                ToastUtil.showShortMessage(this,"已添加至我喜欢。");
                if (!currentLove) {
                    loveSongButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_thumb_up_24_red));
                }
                else{
                    loveSongButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_thumb_up_24));
                }
                currentLove = (!currentLove);
                break ;
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private int playOrPauseSong() throws IOException {
        int current = 0 ;
        if(musicPlayer.getState() == MusicPlayer.NO_PLAYING) {
            current = musicPlayer.playSong();
            musicStartStopButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_pause_circle_outline_48));
        }
        else{
            current = musicPlayer.pauseSong();
            musicStartStopButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_circle_filled_24));
        }
        return current ;
    }


    public static void actionStart(Context mContext){
        Intent intent = new Intent(mContext,MusicActivity.class) ;
        mContext.startActivity(intent);
    }

    /**
     * 初始化控制中心
     */
    protected void registerControllerCenter() {
        center.register(this, new OnControllerListener() {
            @Override
            public Message onControl(int headCode, int bodyCode) {
                Message result = new Message() ;
                Bundle bundle = new Bundle() ;
                switch (headCode){
                    case ControllerCenter.HEAD_LEFT:{
                        Log.d(TAG, "onControl: 切换到上一首歌");
                        bundle.putString(ControllerCenter.TOAST,"last");
                        try {
                            musicPlayer.nextSong();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break ;
                    }
                    case Controller.HEAD_RIGHT:{
                        Log.d(TAG, "onControl: 切换到下一首歌");
                        bundle.putString(ControllerCenter.TOAST,"next");
                        try {
                            musicPlayer.lastSong();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break ;
                    }
                    case Controller.HEAD_DOWN:{
                        Log.d(TAG, "onControl: 暂停/播放");
                        if (musicPlayer.getState() == MusicPlayer.NO_PLAYING){
                            bundle.putString(ControllerCenter.TOAST,"start");
                        }
                        else{
                            bundle.putString(ControllerCenter.TOAST,"pause");
                        }
                        try {
                            playOrPauseSong();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break ;
                    }
                    case Controller.HEAD_LEAN_LEFT:{
                        Log.d(TAG, "onControl: 音量减小");
                        bundle.putString(ControllerCenter.TOAST,"volume down");
                        musicPlayer.volumeDown(4);
                        break ;
                    }
                    case Controller.HEAD_LEAN_RIGHT:{
                        Log.d(TAG, "onControl: 音量增高");
                        bundle.putString(ControllerCenter.TOAST,"volume up");
                        musicPlayer.volumeUp(4);
                        break ;
                    }
                    case Controller.HEAD_DOWN_TWICE:{
                        Log.d(TAG, "onControl: 喜欢这首歌");
                        bundle.putString(ControllerCenter.TOAST,"love this song");
                        musicPlayer.loveSong();
                        break ;
                    }
                    case Controller.HEAD_LEFT_RIGHT:{

                    }
                    case Controller.HEAD_RIGHT_LEFT:{
                        Log.d(TAG, "onControl: 不喜欢这首歌");
                        bundle.putString(ControllerCenter.TOAST,"不喜欢这首歌");
                        break ;
                    }
                    default:{
                        Log.d(TAG, "onControl: 暂无操作");
                        return null ;
                    }
                }
                result.setData(bundle);
                result.what = ControllerCenter.CALL_ON_TOAST ;
                return result ;
            }
        });
    }

}