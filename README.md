# Android Tcp Client Kotlin - Python Tcp Server
Android App Sockets To-do List
This project has two core parts which are the following

### [Server side](https://github.com/pechavarriaa/Tcp_Python-Server_Kotlin-Android-Client/blob/master/Server)
The server side part receives messages from client via TCP,
and implements the following operations :
* Authenticate User
* Register User
* Set new to do item
* Mark a todo done/undone
* Remove a todo
* Get user todos

and it does so by using sqlalchemy (python ORM) to access the information
stored in the sqlite database ([see database tables models](https://github.com/pechavarriaa/Tcp_Python-Server_Kotlin-Android-Client/blob/master/Server/models.py))
and sends an appropiate response using JSON based on the request ([See Tcp Server implementation](https://github.com/pechavarriaa/Tcp_Python-Server_Kotlin-Android-Client/blob/master/Server/tcpserver.py))

##### Server configuration

The server is using flask for the configuration of the database,
all user passwords are hashed with werkzeug-security functions

the systemd service to run the flask application was based on this tutorial
with gunicorn and nginx as shown in the following [blog](https://www.digitalocean.com/community/tutorials/how-to-serve-flask-applications-with-gunicorn-and-nginx-on-ubuntu-18-04)

the systemd services to run the TCP server application for getting request via TCP
were created similarly like in the following [gist](https://gist.github.com/kylemanna/d193aaa6b33a89f649524ad27ce47c4b) 

### [Client side](https://github.com/pechavarriaa/Tcp_Python-Server_Kotlin-Android-Client/blob/master/Client)
The client side of this application is an android [app](https://github.com/pechavarriaa/Tcp_Python-Server_Kotlin-Android-Client/tree/master/Client),
that registers a user and saves its credentials with a custom [user object](https://github.com/pechavarriaa/Tcp_Python-Server_Kotlin-Android-Client/blob/master/Client/app/src/main/java/com/example/client/UserLocalStore.kt) using android shared preferences to keep the user logged-in when
the user opens and closes the application.
The tcp client [implementation](https://github.com/pechavarriaa/Tcp_Python-Server_Kotlin-Android-Client/blob/master/Client/app/src/main/java/com/example/client/TcpClient.kt)
receive a string to be sent to the server, and returns the server response as a string,
in the [Main Activity](https://github.com/pechavarriaa/Tcp_Python-Server_Kotlin-Android-Client/blob/master/Client/app/src/main/java/com/example/client/MainActivity.kt)
are the declared the functions and data classes to parse data types to JSON and build a request, then using coroutines send the data to the server and receive an answer.
A user can do the following actions in the application:
* Register
* Log in
* Log out
* Create new todo
* Mark todo done/undone (by touching the todo)
* Delete todo (by long touching the todo)

Every operation has a handler which will send information to the server to update the 
database accordingly, and make the visual changes on the app. The todo items are
always sorted by  (done/undone) and then by (id -> FCFS idea). when the todo list is modified 
the custom adapter will make the changes on the listview, which is the container for all the todo items. 


sends to-do list items to the server to be stored via TCP,
and it retrieves all to-do items by a http request using the android sdk

### OS-Concepts
* Android based application
* Use of TCP connections
* Threads using `Dispatcher.IO` to be able to communicate over TCP (since main thread cannot use I/O [see](https://stackoverflow.com/questions/6343166/how-to-fix-android-os-networkonmainthreadexception)) on Android

### Challenges
* Creating a coroutine with kotlin for opening a new thread and sending and receiving information over tcp
* Setting up tcp server with a systemd service on the server
* Creating a custom adapter for the listview on the mainactivity with kotlin, to present the todo's

### Reproducing the project
* Download the [android apk app](https://todolist.live/download), install the application in your phone, create a user and add/mark/remove todo's
* Go to the [web app](https://todolist.live) and login with the same username and also add/mark/remove todo's and then sync todo's on android from the menu
* Check [Youtube Video](https://youtu.be/Dn4gkpNrb3o) for a demo, and a basic explanation of the project

### License

This project is [MIT licensed](./LICENSE).
