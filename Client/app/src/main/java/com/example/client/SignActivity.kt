package com.example.client

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.sign_up.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SignActivity : AppCompatActivity() {

    private var userLocalStore : UserLocalStore ?= null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        userLocalStore = UserLocalStore(this)

        submit_sign_up.setOnClickListener {
            val validateStr: String = validateForm(
                username.text.toString(),
                password.text.toString(),
                confirm_password.text.toString()
            )
            if (validateStr != "OK") {
                Toast.makeText(applicationContext, validateStr, Toast.LENGTH_SHORT).show()
            } else {
                val gson = Gson()
                val jsonString = gson.toJson(
                    RegisterUser(
                        username.text.toString(),
                        password.text.toString(),
                        true
                    )
                )
                var getCreateUser: Boolean = false
                runBlocking {
                    getCreateUser = signUp(jsonString)
                }
                if (!getCreateUser) {
                    Toast.makeText(
                        applicationContext,
                        "Username is already taken",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    userLocalStore?.storeUserData(User(username.text.toString(),password.text.toString()))
                    userLocalStore?.setUserLoggedIn(true)

                    Toast.makeText(applicationContext, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                    killActivity()
                }
            }
        }

        login_instead.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
    }

    private fun killActivity() {
        finish()
    }

    fun validateForm(username: String, password: String, confirmPassword: String): String {

        var resultParse: String = "OK"

        if (username.length < 5 || username.length > 20)
            resultParse = "Username should have between 5 and 20 five characters"

        if(username == password)
            resultParse = "Username and Password must be different"

        if (password != confirmPassword)
            resultParse = "Both Passwords should match"

        if (password.length < 5 || password.length > 30)
            resultParse = "Username should have between 5 and 30 five characters"

        return resultParse
    }

    suspend fun signUp(jsonString: String): Boolean {
        var result = true
        withContext(Dispatchers.IO)
        {
            val tcpClientTodo = TcpClient(jsonString)
            if ("200" !in tcpClientTodo.serverResponse) {
                result = !result
            }
        }
        return result
    }

    data class RegisterUser(
        val user: String? = null,
        val password: String? = null,
        val register: Boolean? = true
    )
}