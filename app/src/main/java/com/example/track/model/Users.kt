package com.example.track.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class Users (
    @PrimaryKey(autoGenerate = true)
    val id: Long =0,
    var user_name: String,
    var password: String
)