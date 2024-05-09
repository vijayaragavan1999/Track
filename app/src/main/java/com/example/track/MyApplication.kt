package com.example.track

import android.app.Application
import android.util.Log
import com.example.track.activity.HomeActivity
import com.example.track.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener


class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext as MyApplication
        userRepository = UserRepository(mContext)

    }

    companion object{
        lateinit var userRepository: UserRepository
        lateinit var activity: HomeActivity
        lateinit var mContext : MyApplication
    }
}