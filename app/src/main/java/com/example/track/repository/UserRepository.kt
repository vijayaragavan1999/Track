package com.example.track.repository

import android.content.Context
import android.content.LocusId
import com.example.track.database.MyDatabase
import com.example.track.model.LocationUpdate
import com.example.track.model.LoggedUser
import com.example.track.model.Users
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.kotlin.executeTransactionAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class UserRepository(context: Context) {
    val realm = MyDatabase.getDatabase(context)
    suspend fun insertUser(user: String, password: String) {
        withContext(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransactionAwait { transactionRealm ->
                val userDetails = Users()
                userDetails.userName = user
                userDetails.password = password
                transactionRealm.insert(userDetails as RealmObject)
            }
            realm.close()
        }
    }

    suspend fun insertLoggedUser(user: String, password: String) {
        withContext(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransactionAwait { transactionRealm ->
                val loggedUser = LoggedUser()
                loggedUser.userName = user
                loggedUser.password = password
                transactionRealm.insert(loggedUser as RealmObject)
            }
            realm.close()
        }
    }

    suspend fun getLoggedUser(): List<LoggedUser> {
        return withContext(Dispatchers.Main) {
            val realm = Realm.getDefaultInstance()
        val petsToAdopt = mutableListOf<LoggedUser>()
        realm.executeTransactionAwait { realmTransaction ->
            val results: RealmResults<LoggedUser> = realmTransaction.where(LoggedUser::class.java).findAll()
            petsToAdopt.addAll(results)
        }
            realm.close()
            petsToAdopt
        }

    }


    suspend fun deleteUsers() {
        withContext(Dispatchers.IO) {
            realm.executeTransactionAwait { transactionRealm ->
                transactionRealm.where(Users::class.java).findAll().deleteAllFromRealm()
            }
        }
    }

    suspend fun deleteLoggedUsers() {
        withContext(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransactionAwait { transactionRealm ->
                transactionRealm.where(LoggedUser::class.java).findAll().deleteAllFromRealm()
            }
            realm.close()
        }
    }

    suspend fun deleteLocationHistory() {
        withContext(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransactionAwait { transactionRealm ->
                transactionRealm.where(LocationUpdate::class.java).findAll().deleteAllFromRealm()
            }
            realm.close()
        }
    }

    suspend fun insertLocationUpdate(
        latitude: Double,
        longitude: Double,
        address: String
    ) {
        withContext(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransactionAwait { transactionRealm ->
                val locationDetails = LocationUpdate()
                locationDetails.id = getIdIncremented(transactionRealm, LocationUpdate::class.java)
                locationDetails.latitude = latitude
                locationDetails.longitude = longitude
                locationDetails.address = address
                transactionRealm.insert(locationDetails)
            }
            realm.close()
        }
    }

    private fun getIdIncremented(realm: Realm, clazz: Class<LocationUpdate>): Long {
        val lastId = realm.where(clazz).max("id")?.toLong() ?: 0L
        return lastId + 1
    }

    suspend fun getAllLocationHistory(): List<LocationUpdate> {
        return withContext(Dispatchers.Main) {
            val realm = Realm.getDefaultInstance()
            val result = realm.where(LocationUpdate::class.java).findAll()
            realm.close()
            result
        }
    }

    suspend fun getUserByUser(userName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            val user = realm.where(Users::class.java)
                .equalTo("userName", userName)
                .findFirst()
            val exists = user != null
            realm.close()
            exists
        }
    }


    suspend fun checkLoginUser(userName: String, password: String): Users? {
        return withContext(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            val user = realm.where(Users::class.java)
                .equalTo("userName", userName)
                .equalTo("password", password)
                .findFirst()
            realm.close()
            user
        }
    }
}
