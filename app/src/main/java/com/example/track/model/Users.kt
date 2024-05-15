package com.example.track.model


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class Users : RealmObject() {
    @PrimaryKey
    var userName: String = ""
    var password: String = ""
}
