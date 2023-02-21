package com.example.algorithm.Music.Entrity;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SongDao {
    @Insert
    void insertSong(Song song) ;

    @Delete
    void deleteSong(Song song) ;

    @Update
    void updateSong(Song song) ;

    @Query("select * from songs")
    List<Song> getSongList() ;

    @Query("select * from songs where id = :id")
    Song getSongById(int id) ;
}
