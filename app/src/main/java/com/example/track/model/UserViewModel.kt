package com.example.track.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.track.repository.UserRepository
import kotlinx.coroutines.launch


class UserViewModel(private val repository : UserRepository) : ViewModel() {

    private val userLocationHistory = MutableLiveData<List<LocationUpdate>>()

    fun insertUser(user : Users){
        viewModelScope.launch {
            repository.insertUser(user)
            userLocationHistory.value = repository.getAllLoctionHistory()
        }
    }

    fun insertLoggedUser(user : LoggedUser){
        viewModelScope.launch {
            repository.insertLoggedUser(user)
        }
    }


    fun getAllLoctionHistory() : MutableLiveData<List<LocationUpdate>> {
        viewModelScope.launch {
            userLocationHistory.postValue(repository.getAllLoctionHistory())
        }
        return userLocationHistory
    }

    suspend fun getUserByUser(userName : String) : Users?{
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