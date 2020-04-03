package com.example.client

import android.content.Context
import android.content.SharedPreferences


class UserLocalStore(context: Context) {
    var userLocalDatabase: SharedPreferences
    fun storeUserData(user: User) {
        val userLocalDatabaseEditor = userLocalDatabase.edit()
        userLocalDatabaseEditor.putString("username", user.username)
        userLocalDatabaseEditor.putString("password", user.password)
        userLocalDatabaseEditor.apply()
    }

    fun setUserLoggedIn(loggedIn: Boolean) {
        val userLocalDatabaseEditor = userLocalDatabase.edit()
        userLocalDatabaseEditor.putBoolean("loggedIn", loggedIn)
        userLocalDatabaseEditor.apply()
    }

    fun clearUserData() {
        val userLocalDatabaseEditor = userLocalDatabase.edit()
        userLocalDatabaseEditor.clear()
        userLocalDatabaseEditor.apply()
    }


    fun getLoggedInUser(): User? {
        if (!userLocalDatabase.getBoolean("loggedIn", false)) {
            return null
        }
        val username = userLocalDatabase.getString("username", "")
        val password = userLocalDatabase.getString("password", "")
        return User(username, password)
    }

    companion object {
        const val SP_NAME = "userDetails"
    }

    init {
        userLocalDatabase = context.getSharedPreferences(SP_NAME, 0)
    }
}