package com.example.track.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.track.R
import com.example.track.repository.UserRepository
import com.example.track.model.UserViewModel
import com.example.track.model.UserViewModelFactory
import com.example.track.model.Users
import com.example.track.databinding.ActivitySignupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    lateinit var binding : ActivitySignupBinding
    lateinit var mContext : Context

    lateinit var viewModel : UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this

        val userRepository = UserRepository(mContext)

        val factory = UserViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this,factory).get(UserViewModel::class.java)

        binding.imgEye.setOnClickListener {

            if (binding.password.transformationMethod == null) {
                binding.password.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.imgEye.setImageResource(R.drawable.eye_close) // Change to closed eye icon
            } else {

                binding.password.transformationMethod = null
                binding.imgEye.setImageResource(R.drawable.eye_open)
            }

            binding.password.setSelection(binding.password.text.length)
        }
        val minLength = 6  // Minimum length required
        val maxLength = 20 // Maximum length allowed

        val lengthFilter = InputFilter.LengthFilter(maxLength)
        binding.password.filters = arrayOf<InputFilter>(lengthFilter)

        binding.password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this example
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let { password ->
                    var hasDigit = false
                    var hasUppercase = false
                    var hasSpecialChar = false

                    password.forEach { c ->
                        if (c.isDigit()) {
                            hasDigit = true
                        } else if (c.isUpperCase()) {
                            hasUppercase = true
                        } else if (!c.isLetterOrDigit()) {
                            hasSpecialChar = true
                        }
                    }

                    if (password.length in minLength..maxLength
                        && hasDigit && hasUppercase && hasSpecialChar) {
                        binding.signup.isEnabled = true
                    } else {
                        binding.password.error = "Invalid password format"
                        binding.signup.isEnabled = false
                    }
                }
            }
        })


        binding.imgEyeConfirmPass.setOnClickListener {

            if (binding.confirmPassword.transformationMethod == null) {
                binding.confirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.imgEyeConfirmPass.setImageResource(R.drawable.eye_close) // Change to closed eye icon
            } else {

                binding.confirmPassword.transformationMethod = null
                binding.imgEyeConfirmPass.setImageResource(R.drawable.eye_open)
            }

            binding.confirmPassword.setSelection(binding.confirmPassword.text.length)
        }

        binding.signup.setOnClickListener {
            val username = binding.userName.text.toString()
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()
            if (checkValidation(username, password, confirmPassword)) {
                CoroutineScope(Dispatchers.Main).launch {
                    val existUser = viewModel.getUserByUser(username)
                    if (existUser !=null && existUser == true) {
                        Toast.makeText(mContext, "ExistUser", Toast.LENGTH_SHORT).show()
                    } else {
                        val load = async { viewModel.insertUser(username, password) }
                        load.await()
                        startActivity(Intent(mContext, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        }

    }

    fun checkValidation(username: String, password: String, confirmPassword: String) : Boolean{
        var result = false
        if(username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()){
            if(password.equals(confirmPassword)){
                if(Patterns.EMAIL_ADDRESS.matcher(username).matches()){
                    result =true
                }else{
                    Toast.makeText(mContext,"Please fill valid email",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(mContext,"Password and Confirm Password Mismatch", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(mContext,"Please fill the all details",Toast.LENGTH_SHORT).show()
        }
        return result
    }
}