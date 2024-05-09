package com.example.track.repository

import android.content.Context
import com.example.track.database.MyDatabase
import com.example.track.model.LocationUpdate
import com.example.track.model.LoggedUser
import com.example.track.model.Users

class UserRepository(context: Context) {

    private val database = MyDatabase.getDatabase(context)

    suspend fun insertUser(userDetails : Users){
        database.userDao().insertEmployee(userDetails)
    }

    suspend fun insertLoggedUser(loggedUser : LoggedUser){
        database.userDao().insertLoggedUser(loggedUser)
    }

    suspend fun getLoggedUser():List<LoggedUser>{
        return database.userDao().getLoggedUser()
    }

    suspend fun deleteUsers(){
        return database.userDao().deleteUsers()
    }

    suspend fun deleteLoggedUsers(){
        return database.userDao().deleteLoggedUsers()
    }

    suspend fun deleteLocationHistory(){
        return database.userDao().deleteLocationHistory()
    }

    suspend fun insertLocationUpdate(locationDetails : LocationUpdate){
        database.userDao().insertEmployee(locationDetails)
    }

    suspend fun getAllLoctionHistory():List<LocationUpdate>{
        return database.userDao().getAllLoctionHistory()
    }

    suspend fun getUserByUser(userName: String): Users? {
        return database.userDao().getUserByUser(userName)
    }

    suspend fun checkLoginUser(userName: String, password: String): Users? {
        return database.userDao().checkLoginUser(userName,password)
    }

}