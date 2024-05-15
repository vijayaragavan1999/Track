package com.example.track.database

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration

class MyDatabase private constructor() {

    companion object {
        private var realm: Realm? = null

        fun getDatabase(context: Context): Realm {
            if (realm == null) {
                Realm.init(context)
                val config = RealmConfiguration.Builder()
                    .name("track.realm") // Set the database name
                    .schemaVersion(1) // Set the schema version
                    .deleteRealmIfMigrationNeeded() // Handle migration automatically
                    .build()
//                Realm.setDefaultConfiguration(config)
                realm = Realm.getDefaultInstance()
            }
            return realm!!
        }

        fun closeDatabase() {
            realm?.close()
            realm = null
        }
    }
}
