package com.example.track.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class LocationUpdate : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var address: String = ""

    companion object {
        // Track the maximum id value
        private var maxId: Long = 0
    }

    fun setIdIncremented() {
        id = ++maxId
    }
}
