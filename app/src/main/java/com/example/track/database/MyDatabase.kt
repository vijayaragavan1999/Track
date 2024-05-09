package com.example.track.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.track.MyApplication
import com.example.track.UserDao
import com.example.track.model.LocationUpdate
import com.example.track.model.LoggedUser
import com.example.track.model.Users


@Database(entities = [Users::class,LocationUpdate::class,LoggedUser::class],version = 1,exportSchema =false)
abstract class MyDatabase : RoomDatabase() {

    abstract fun userDao() : UserDao

    companion object {
        @Volatile
        private var INSTANCE : MyDatabase?=null

        fun getDatabase(context: Context): MyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "sample"
                ).build()
                INSTANCE = instance
                return instance
            }
        }


    }

}