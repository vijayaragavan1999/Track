package com.example.track.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_update")
data class LocationUpdate (
    @PrimaryKey(autoGenerate = true)
    val id: Long =0,
    var latitude: Double,
    var longitude: Double,
    var address : String
)