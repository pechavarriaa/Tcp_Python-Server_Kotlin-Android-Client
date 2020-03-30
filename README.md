# cs330FinalProject
Assignment #5 Android App Sockets To-do List

This project has two core parts which are the following

### Server side
The server side part receives messages from client via TCP and
saves the to-do list items to be later be retrived by the client
via an api from flask set up in the server

#### Server configuration
After getting a domain (todolist.live) set up the flask app 
with gunicorn and nginx as shown in the following [blog](https://www.digitalocean.com/community/tutorials/how-to-serve-flask-applications-with-gunicorn-and-nginx-on-ubuntu-18-04)

### Client side
The client side of this application is an android app, that
sends to-do list items to the server to be stored via TCP,
and it retrieves all to-do items by a http request using the android sdk
