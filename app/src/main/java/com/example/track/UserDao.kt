package com.example.track

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.track.model.LocationUpdate
import com.example.track.model.LoggedUser
import com.example.track.model.Users

@Dao
interface UserDao {

    @Insert
    suspend fun insertEmployee(employee : Users)

    @Insert
    suspend fun insertLoggedUser(employee : LoggedUser)

    @Insert
    suspend fun insertEmployee(employee : LocationUpdate)

    @Update
    suspend fun updateEmployee(employee : Users)

    @Query("DELETE FROM users")
    suspend fun deleteUsers()

    @Query("DELETE FROM logged_user")
    suspend fun deleteLoggedUsers()

    @Query("DELETE FROM location_update")
    suspend fun deleteLocationHistory()

    @Query("SELECT * FROM logged_user")
    suspend fun getLoggedUser(): List<LoggedUser>

    @Query("SELECT * FROM location_update")
    suspend fun getAllLoctionHistory(): List<LocationUpdate>

    @Query("SELECT * FROM users WHERE user_name = :userName")
    suspend fun getUserByUser(userName: String): Users?

    @Query("SELECT * FROM users WHERE user_name = :userName AND password = :password")
    suspend fun checkLoginUser(userName: String,password : String): Users?


}