package com.example.track.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.track.R
import com.example.track.repository.UserRepository
import com.example.track.model.UserViewModel
import com.example.track.model.UserViewModelFactory
import com.example.track.model.Users
import com.example.track.databinding.ActivityMainBinding
import com.example.track.model.LoggedUser
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.async

class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding

    lateinit var viewModel : UserViewModel
    lateinit var mContext : Context
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()


        val userRepository = UserRepository(mContext)

        val factory = UserViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this,factory).get(UserViewModel::class.java)

        binding.signInButton.setOnClickListener {
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, Req_Code)
        }
        binding.signupText.setOnClickListener {
            startActivity(Intent(mContext, SignupActivity::class.java))
        }

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

        binding.login.setOnClickListener{
            val username = binding.userName.text.toString()
            val password = binding.password.text.toString()
            if(checkValidations(username,password)){
                CoroutineScope(Dispatchers.Main).launch {
                    val result = viewModel.checkLoginUser(username,password)
                    if(result!=null){
                        viewModel.insertLoggedUser(LoggedUser(0,username,password))
                        startActivity(Intent(mContext, HomeActivity::class.java))
                        finish()
                    }else{
                        Toast.makeText(mContext,"Email or Password Invalid",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Login failed...", Toast.LENGTH_SHORT).show()
            Log.e("error===================","==================="+e)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources, sign out Firebase Auth
        mGoogleSignInClient.signOut()
    }
    private fun UpdateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(mContext,"Login successfully",Toast.LENGTH_SHORT).show()

                CoroutineScope(Dispatchers.Main).launch {
                    val load =async { viewModel.insertUser(Users(0,account.email.toString(),"")) }
                    load.await()
                    viewModel.insertLoggedUser(LoggedUser(0,account.email.toString(),""))

                    startActivity(Intent(mContext, HomeActivity::class.java))
                    finish()
                }
            }
        }
    }

    fun checkValidations(userName: String, password: String) : Boolean{
        var result =false
        if(userName.isNotEmpty() && password.isNotEmpty()){
            if(Patterns.EMAIL_ADDRESS.matcher(userName).matches()){
                result =true
            }else{
                Toast.makeText(mContext,"Please fill valid email",Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(mContext,"Please fill all the details",Toast.LENGTH_SHORT).show()
        }
        return result
    }
}