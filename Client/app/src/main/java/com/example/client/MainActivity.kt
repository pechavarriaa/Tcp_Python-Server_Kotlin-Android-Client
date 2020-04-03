package com.example.client


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private var userLocalStore: UserLocalStore? = null
    private var listTodos = arrayListOf<Todo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userLocalStore = UserLocalStore(this)

        val user: User? = userLocalStore!!.getLoggedInUser()
        if (user == null) {
            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            killActivity()
        }

        setSupportActionBar(toolbar as Toolbar?)
        val ab: androidx.appcompat.app.ActionBar? = supportActionBar
        val tv = TextView(applicationContext)
        val lp: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT,
            androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT
        )

        tv.layoutParams = lp
        tv.text = "${user!!.username} To Do Items"
        tv.textSize = 20F
        tv.setTextColor(Color.BLACK)
        tv.typeface = ResourcesCompat.getFont(this, R.font.amatic_sc_bold);
        ab?.setBackgroundDrawable(ColorDrawable(0xffB1DCF9.toInt()))
        ab?.displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
        ab?.customView = tv

        populateListView()

        btn_add.setOnClickListener {
            var strNewTodo = add_todo.text.toString()
            strNewTodo.trim()
            if (strNewTodo.isNotEmpty()) {
                addSingleTodo(strNewTodo)
            }
            add_todo.setText("")
        }
    }

    override fun onResume() {
        super.onResume()
        populateListView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater;
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.sync -> {
                populateListView()
                return true
            }
            R.id.sign_out -> {
                userLocalStore?.let { logout(it) }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun populateListView() {

        val user: User? = userLocalStore!!.getLoggedInUser()
        val gson = Gson()
        val getTheTodos = GetTodos(user!!.username, user!!.password, true)

        val jsonString = gson.toJson(getTheTodos)

        var itemlist = arrayListOf<String>()
        var adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_checked
            , itemlist
        )

        runBlocking {
            listTodos = getTodos(jsonString)
        }

        if (listTodos.size == 1 && listTodos[0].taskId == -1) {
            Toast.makeText(applicationContext, "Error Fetching Todos :(", Toast.LENGTH_SHORT)
                .show()
        } else {
            for (todo in listTodos) {
                itemlist.add(todo.taskStr)
            }
            listView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    private fun addSingleTodo(todoitem: String) {

        val user: User? = userLocalStore!!.getLoggedInUser()
        val gson = Gson()
        val getTheTodos = CreateTodo(user?.username, user?.password, todoitem)
        val jsonString = gson.toJson(getTheTodos)

        var itemlist = arrayListOf<String>()
        var adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_checked
            , itemlist
        )

        var createdTodo: Int = -1

        runBlocking {
            createdTodo = createTodo(jsonString)
        }

        if (createdTodo < 0) {
            Toast.makeText(applicationContext, "Error adding new todo :(", Toast.LENGTH_SHORT)
                .show()
        } else {
            listTodos.add(Todo(createdTodo, false, todoitem))
            itemlist.add(todoitem)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    private fun logout(userLocalStore: UserLocalStore) {
        userLocalStore.clearUserData()
        userLocalStore.setUserLoggedIn(false)
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        killActivity()
    }

    private fun killActivity() {
        finish()
    }

    private suspend fun getTodos(jsonString: String): ArrayList<Todo> {
        val result = ArrayList<Todo>()
        withContext(Dispatchers.IO)
        {
            val tcpClientTodo = TcpClient(jsonString)
            if ("200" in tcpClientTodo.serverResponse) {
                val jsonObj = JSONObject(tcpClientTodo.serverResponse)
                val todoResponseJson: JSONArray = jsonObj.getJSONArray("todos")
                for (i in 0 until todoResponseJson.length()) {
                    val c = todoResponseJson.getJSONObject(i)
                    val idT = c.getInt("id")
                    val itemT = c.getString("item")
                    val doneT = c.getBoolean("done")
                    result.add(Todo(idT, doneT, itemT))
                }
            } else {
                result.add(Todo(-1, false, "error"))
            }
        }
        return result
    }

    private suspend fun createTodo(jsonString: String): Int {
        var result = -1
        withContext(Dispatchers.IO)
        {
            val tcpClientTodo = TcpClient(jsonString)
            if ("200" in tcpClientTodo.serverResponse) {
                val jsonObj = JSONObject(tcpClientTodo.serverResponse)
                result = jsonObj.getInt("id")
            }
        }
        return result
    }

    suspend fun markTodo(jsonString: String): Boolean {
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

    suspend fun deleteTodo(jsonString: String): Boolean {
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

    data class GetTodos(
        val user: String? = null,
        val password: String? = null,
        val gettodos: Boolean? = true
    )

    data class MarkTodo(
        val user: String? = null,
        val password: String? = null,
        val marktodo: Int? = 0
    )

    data class RemoveTodo(
        val user: String? = null,
        val password: String? = null,
        val todoitemremove: Int? = 0
    )

    data class CreateTodo(
        val user: String? = null,
        val password: String? = null,
        val todoitem: String? = null
    )

    data class Todo(
        val taskId: Int,
        val taskDone: Boolean,
        val taskStr: String
    )
}




