package com.example.client

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        send_msg.setOnClickListener {


            val gson = Gson()
            val getTheTodos = GetTodos("pechavarriaa", "pepe25", true)
            var jsonString = gson.toJson(getTheTodos)


            var itemlist = arrayListOf<String>()
            var adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_multiple_choice
                , itemlist
            )
            GlobalScope.launch (Dispatchers.Main){
                var tcpClientTodo = TcpClient(jsonString)

            if ("200" !in tcpClientTodo.serverResponse) {
                itemlist.add(tcpClientTodo.serverResponse)
                Toast.makeText(applicationContext, "Message Failed", Toast.LENGTH_SHORT).show()
            } else {
                val jsonObj = JSONObject(tcpClientTodo.serverResponse)
                val unA: JSONArray = jsonObj.getJSONArray("todos")
                for (i in 0 until unA.length()) {
                    val c = unA.getJSONObject(i)
                    val idT = c.getInt("id")
                    val itemT = c.getString("item")
                    val doneT = c.getBoolean("done")
                    itemlist.add(itemT)
                }
            }
                listView.adapter = adapter
                adapter.notifyDataSetChanged()}
        }


    }
    data class GetTodos(
        val user:  String? = null,
        val password:  String? = null,
        val gettodos:  Boolean? = true
    )
    data class MarkTodo(
        val user:  String? = null,
        val password:  String? = null,
        val marktodo:  Int? = 0
    )
    data class RemoveTodo(
        val user:  String? = null,
        val password:  String? = null,
        val todoitemremove:  Int? = 0
    )
    data class CreateTodo(
        val user:  String? = null,
        val password:  String? = null,
        val todoitem:  String? = null
    )
    data class RegisterUser(
        val user:  String? = null,
        val password:  String? = null,
        val register:  Boolean? = true 
    )
    data class SignUser(
        val user:  String? = null,
        val password:  String? = null
    )
}
