package com.example.track.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.track.repository.UserRepository
import kotlinx.coroutines.launch


class UserViewModel(private val repository : UserRepository) : ViewModel() {

    private val userLocationHistory = MutableLiveData<List<LocationUpdate>>()
    private val userDetails = MutableLiveData<Boolean>()

    fun insertUser(user: String, password: String){
        viewModelScope.launch {
            repository.insertUser(user,password)
            userLocationHistory.value = repository.getAllLocationHistory()
        }
    }

    fun insertLoggedUser(user: String, password: String){
        viewModelScope.launch {
            repository.insertLoggedUser(user,password)
        }
    }


    fun getAllLoctionHistory() : MutableLiveData<List<LocationUpdate>> {
        viewModelScope.launch {
            userLocationHistory.postValue(repository.getAllLocationHistory())
        }
        return userLocationHistory
    }

    suspend fun getUserByUser(userName : String) :Boolean{
        return repository.getUserByUser(userName)
    }

    suspend fun checkLoginUser(userName : String,password : String) : Users?{
        return repository.checkLoginUser(userName,password)
    }


}

class TaskItemModelFactory(private val repository: UserRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UserViewModel::class.java))
            return UserViewModel(repository) as T

        throw IllegalArgumentException("Unknown class for View Model")
    }
}