package com.example.client

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject





class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        send_msg.setOnClickListener {
            data class GetTodos(
                val user: String,
                val password: String,
                val gettodos: Boolean
            )

            val gson = Gson()
            var jsonString = gson.toJson(GetTodos("pechavarriaa", "pepe25", true))
            var tcpClientTodo = TcpClient(jsonString)
            var itemlist = arrayListOf<String>()
            var adapter =ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice
                , itemlist)
            // Adding the items to the list when the add button is pressed

            if("200" !in tcpClientTodo.serverResponse ){
                Toast.makeText(applicationContext, "Message Failed", Toast.LENGTH_SHORT).show()}
            else {
                val jsonObj = JSONObject(tcpClientTodo.serverResponse)
                val unA: JSONArray = jsonObj.getJSONArray("todos")

                for (i in 0 until unA.length()) {
                    val c = unA.getJSONObject(i)
                    val title = c.getString("title")
                    val body = c.getString("body")
                }
                data class todo(
                val id: Int,
                val todo:String,
                val done:Boolean
            )
            var testModel = gson.fromJson(jsonString, todo::class.java)
          for ()
                itemlist.add(editText.text.toString())
                listView.adapter =  adapter
                adapter.notifyDataSetChanged()
            }
        }
    }
}