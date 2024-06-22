package com.example.taskpixie.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.taskpixie.R
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.LoginUserPayload
import com.example.taskpixie.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WelcomeActivity : AppCompatActivity() {
    private lateinit var userPreferences: UserPreferences
    private var keepSplashScreenOn = true

    // ==================== Lifecycles ==================== //

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        splashScreen.setKeepOnScreenCondition { keepSplashScreenOn }

        userPreferences = UserPreferences(this)

        lifecycleScope.launch {
            val identity = withContext(Dispatchers.IO) { userPreferences.identity.first() }
            val password = withContext(Dispatchers.IO) { userPreferences.password.first() }

            if (!identity.isNullOrEmpty() && !password.isNullOrEmpty()) {
                autoLogin(identity, password)
            } else {
                keepSplashScreenOn = false
            }
        }
    }

    // ==================== Mechanics ==================== //

    /**
     * Perform auto Login while on splash screen
     */
    private fun autoLogin(identity: String, password: String) {
        val payload = LoginUserPayload(identity, password)
        RetrofitClient.userService.loginUser(payload).enqueue(object :
            Callback<ApiResponse<String>> {
            override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                val gson = Gson()
                val type = object : TypeToken<ApiResponse<String>>() {}.type

                val apiResponse: ApiResponse<String>? = if (response.isSuccessful) {
                    response.body()
                } else {
                    gson.fromJson(response.errorBody()?.charStream(), type)
                }

                if (apiResponse?.status == "success") {
                    val accessToken = response.headers()["Authorization"]?.replace("Bearer ", "")
                    val refreshToken = response.headers()["X-Refresh-Token"]

                    if (accessToken != null && refreshToken != null) {
                        lifecycleScope.launch {
                            userPreferences.saveTokens(accessToken, refreshToken)
                            fetchUserDetails("Bearer $accessToken")
                        }
                    } else {
                        keepSplashScreenOn = false
                    }
                } else {
                    keepSplashScreenOn = false
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                keepSplashScreenOn = false
            }
        })
    }

    /**
     * Fetch user details after successful login and save user ID
     */
    private fun fetchUserDetails(token: String) {
        RetrofitClient.userService.getLoggedUser(token).enqueue(object : Callback<ApiResponse<User>> {
            override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success") {
                        val user = apiResponse.data
                        user?.let {
                            lifecycleScope.launch {
                                userPreferences.saveUserId(it.id)
                                userPreferences.saveUsername(it.username)
                                Toast.makeText(this@WelcomeActivity, "Welcome back, ${it.username}!", Toast.LENGTH_SHORT).show()

                                navigateToMainActivity()
                            }
                        }
                    } else {
                        keepSplashScreenOn = false
                    }
                } else {
                    keepSplashScreenOn = false
                }
            }

            override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                keepSplashScreenOn = false
            }
        })
    }

    /**
     * We should directly head to main activity if the login is success
     */
    private fun navigateToMainActivity() {
        keepSplashScreenOn = false
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
