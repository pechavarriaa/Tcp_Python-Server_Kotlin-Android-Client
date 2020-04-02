package com.example.client


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ///*
        val ab: androidx.appcompat.app.ActionBar? = supportActionBar
        val tv = TextView(applicationContext)
        val lp: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT,
            androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT
        )

        val user:String = "pechavarriaa"
        tv.layoutParams = lp
        tv.text = "$user To Do Items"
        tv.textSize = 20F
        tv.setTextColor(Color.BLACK)
        tv.typeface = ResourcesCompat.getFont(this, R.font.amatic_sc_bold);
        ab?.setBackgroundDrawable(ColorDrawable(0xffB1DCF9.toInt()))
        ab?.displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
        ab?.customView = tv
        //val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar
        //actionBar!!.title = "pechavarriaa To Do Items"
        //actionBar!!.setBackgroundDrawable(ColorDrawable(0xffB1DCF9.toInt()))*/


        send_msg.setOnClickListener {

            val gson = Gson()
            val getTheTodos = GetTodos("pechavarriaa", "pepe25", true)
            val jsonString = gson.toJson(getTheTodos)

            var itemlist = arrayListOf<String>()
            var adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_multiple_choice
                , itemlist
            )

            var todoList = arrayListOf<Todo>()

            runBlocking {
                todoList = getTodos(jsonString)
            }

            if (todoList.size == 1 && todoList[0].taskId == -1) {
                Toast.makeText(applicationContext, "Error Fetching Todos :(", Toast.LENGTH_SHORT)
                    .show()
            } else {
                for (todo in todoList) {
                    itemlist.add(todo.taskStr)
                }
                listView.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }
    }

    suspend fun getTodos(jsonString: String): ArrayList<Todo> {
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

    suspend fun createTodo(jsonString: String): Boolean {
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




