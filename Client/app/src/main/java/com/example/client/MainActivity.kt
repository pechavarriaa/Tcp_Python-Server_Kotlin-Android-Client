package com.example.client

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
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
    private var listTodos = mutableListOf<Todo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userLocalStore = UserLocalStore(this)
        val user: User? = userLocalStore!!.getLoggedInUser()

        if (user == null) {
            logout()
        }

        val gson = Gson()
        val jsonString = gson.toJson(
            LoginUser(
                user?.username,
                user?.password
            )
        )

        var getLoginUser: Boolean = false
        runBlocking {
            getLoginUser = logIn(jsonString)
        }

        if (!getLoginUser) {
            logout()
        }

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
        tv.text = "${user?.username} To Do Items"
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

        listView.setOnItemClickListener { _, _, position, _ ->
            markSingleTodo(position)
        }
        listView.setOnItemLongClickListener{ _, _, position, _ ->
            val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert)
            // set message of alert dialog
            dialogBuilder.setMessage("Delete '${listTodos[position].taskStr}' from the to do list?")
                // positive button text and action
                .setPositiveButton("Delete") { _, _ ->
                    deleteSingleTodo(position)
                }
                // negative button text and action
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel()
                }
            val alert = dialogBuilder.create()
            alert.setTitle("Delete To Do")
            alert.show()
            true
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
        }
        when (item.itemId) {
            R.id.sign_out -> {
                logout()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun populateListView() {

        val user: User? = userLocalStore!!.getLoggedInUser()
        val gson = Gson()
        val getTheTodos = GetTodos(user?.username, user?.password, true)
        val jsonString = gson.toJson(getTheTodos)

        runBlocking {
            listTodos = getTodos(jsonString)
        }

        if (listTodos.size == 1 && listTodos[0].taskId == -1) {
            Toast.makeText(applicationContext, "Error Fetching Todos :(", Toast.LENGTH_SHORT)
                .show()
        } else {
            listView.adapter = CustomAdapter(this, listTodos as ArrayList<Todo>)
        }
    }

    private fun addSingleTodo(todoitem: String) {

        val user: User? = userLocalStore!!.getLoggedInUser()
        val gson = Gson()
        val getTheTodos = CreateTodo(user?.username, user?.password, todoitem)
        val jsonString = gson.toJson(getTheTodos)
        var createdTodo: Int = -1

        runBlocking {
            createdTodo = createTodo(jsonString)
        }

        if (createdTodo < 0) {
            Toast.makeText(applicationContext, "Error adding new todo :(", Toast.LENGTH_SHORT)
                .show()
        } else {
            listTodos.add(Todo(createdTodo, false, todoitem))
            listTodos.sortWith(compareBy({ it.taskDone }, { it.taskId }))
            listView.adapter = CustomAdapter(this, listTodos as ArrayList<Todo>)
        }
    }

    private fun markSingleTodo(position: Int) {

        val user: User? = userLocalStore?.getLoggedInUser()
        val gson = Gson()
        val todoMark = MarkTodo(user?.username, user?.password, listTodos[position].taskId)
        val jsonString = gson.toJson(todoMark)
        var markedTodo: Boolean = false

        runBlocking {
            markedTodo = markTodo(jsonString)
        }
        if (!markedTodo) {
            Toast.makeText(applicationContext, "Error marking todo :(", Toast.LENGTH_SHORT)
                .show()
        } else {
            listTodos[position].taskDone = !listTodos[position].taskDone
            listTodos.sortWith(compareBy({ it.taskDone }, { it.taskId }))
            listView.adapter = CustomAdapter(this, listTodos as ArrayList<Todo>)
        }
    }

    private fun deleteSingleTodo(position: Int) {
        val user: User? = userLocalStore?.getLoggedInUser()
        val gson = Gson()
        val todoMark = RemoveTodo(user?.username, user?.password, listTodos[position].taskId)
        val jsonString = gson.toJson(todoMark)
        var deletedTodo: Boolean = false

        runBlocking {
            deletedTodo = deleteTodo(jsonString)
        }
        if (!deletedTodo) {
            Toast.makeText(applicationContext, "Error deleting todo :(", Toast.LENGTH_SHORT)
                .show()
        } else {
            listTodos.removeAt(position)
            listTodos.sortWith(compareBy({ it.taskDone }, { it.taskId }))
            listView.adapter = CustomAdapter(this, listTodos as ArrayList<Todo>)
        }
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

    private fun logout() {
        userLocalStore?.clearUserData()
        userLocalStore?.setUserLoggedIn(false)
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

    private suspend fun markTodo(jsonString: String): Boolean {
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

    private suspend fun deleteTodo(jsonString: String): Boolean {
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

    class CustomAdapter(
        private val context: Context,
        private val dataList: ArrayList<Todo>
    ) : BaseAdapter() {
        private val inflater: LayoutInflater =
            this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return dataList.size
        }

        override fun getItem(position: Int): Int {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val rowView = inflater.inflate(R.layout.list_view_layout, parent, false)
            rowView.findViewById<TextView>(R.id.textView).text = dataList[position].taskStr
            if (dataList[position].taskDone)
                rowView.findViewById<TextView>(R.id.textView).paintFlags =
                    Paint.STRIKE_THRU_TEXT_FLAG
            return rowView
        }
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
        var taskDone: Boolean,
        val taskStr: String
    )

    data class LoginUser(
        val user: String? = null,
        val password: String? = null
    )
}