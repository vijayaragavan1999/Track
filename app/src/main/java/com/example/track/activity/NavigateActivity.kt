package com.example.track.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.track.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NavigateActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mContext = this

        val userRepository = UserRepository(mContext)
        CoroutineScope(Dispatchers.Main).launch {
            val result = userRepository.getLoggedUser()
            if(result.isNotEmpty()){
                startActivity(Intent(mContext,HomeActivity::class.java))

            }else{
                startActivity(Intent(mContext,LoginActivity::class.java))
            }
        }

    }


}