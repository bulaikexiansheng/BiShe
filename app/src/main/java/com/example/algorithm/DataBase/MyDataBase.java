package com.example.algorithm.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.algorithm.Music.Entrity.Song;
import com.example.algorithm.Music.Entrity.SongDao;

@Database(entities = {Song.class},version = 1)
public abstract class MyDataBase extends RoomDatabase {
    private static final String DATABASE_NAME = "EarMotion" ;
    private static MyDataBase dataBaseInstance ;
    public static synchronized MyDataBase getInstance(Context mContext){
        if (dataBaseInstance == null) {
            dataBaseInstance = Room.databaseBuilder(mContext.getApplicationContext(),
                    MyDataBase.class,DATABASE_NAME).build() ;
        }
        return dataBaseInstance ;
    }
    public abstract SongDao songDao() ;
}
