package com.example.algorithm.Music.Entrity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "songs")
public class Song {
    // 主键歌曲id
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id",typeAffinity = ColumnInfo.INTEGER)
    public int id ;

    @ColumnInfo(name = "songName",typeAffinity = ColumnInfo.TEXT)
    public String songName ;

    @ColumnInfo(name = "singer",typeAffinity = ColumnInfo.TEXT)
    public String singer ;

    @ColumnInfo(name = "path",typeAffinity = ColumnInfo.TEXT)
    public String savePath ;

    @ColumnInfo(name = "duration",typeAffinity = ColumnInfo.INTEGER)
    public int duration ;

    @ColumnInfo(name = "albumId",typeAffinity = ColumnInfo.INTEGER)
    public int albumId ;

    public Song(int id,String songName,String singer,String savePath,int duration,int albumId){
        this.id = id ;
        this.songName = songName ;
        this.singer = singer ;
        this.savePath = savePath ;
        this.duration = duration ;
        this.albumId = albumId ;
    }
}
