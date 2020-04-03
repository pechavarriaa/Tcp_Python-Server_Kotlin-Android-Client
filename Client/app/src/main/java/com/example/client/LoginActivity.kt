package com.example.client

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private var userLocalStore : UserLocalStore ?= null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        userLocalStore = UserLocalStore(this)

        if(userLocalStore!!.getLoggedInUser() != null){
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            killActivity()
        }

        sign_up_instead.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SignActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }

        submit_login.setOnClickListener{

                val validateStr: String = validateForm(
                    username.text.toString(),
                    password.text.toString()
                )
                if (validateStr != "OK") {
                    Toast.makeText(applicationContext, validateStr, Toast.LENGTH_SHORT).show()
                } else {
                    val gson = Gson()
                    val jsonString = gson.toJson(
                        LoginUser(
                            username.text.toString(),
                            password.text.toString()
                        )
                    )
                    var getLoginUser: Boolean = false
                    runBlocking {
                        getLoginUser = logIn(jsonString)
                    }
                    if (!getLoginUser) {
                        Toast.makeText(
                            applicationContext,
                            "Login Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        userLocalStore!!.storeUserData(User(username.text.toString(),password.text.toString()))
                        userLocalStore!!.setUserLoggedIn(true)
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
    }

    private fun killActivity() {
        finish()
    }

    private fun validateForm(username: String, password: String): String {

        var resultParse: String = "OK"

        if (username.isEmpty() || password.isEmpty())
            resultParse = "Both fields must be filled"

        if (username.length < 5 || username.length > 20)
            resultParse = "Login Failed"

        if (password.length < 5 || password.length > 30)
            resultParse =  "Login Failed"


        return resultParse
    }

    private suspend fun logIn(jsonString: String): Boolean {
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

    data class LoginUser(
        val user: String? = null,
        val password: String? = null
    )
}