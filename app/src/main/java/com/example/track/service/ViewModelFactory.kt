package com.example.track.service

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.track.repository.UserRepository
import com.example.track.model.UserViewModel

class ViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return super.create(modelClass)
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
