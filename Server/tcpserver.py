# socketserver to tcp connections
from socketserver import TCPServer, StreamRequestHandler
import socket
# json to retrieve data type
import json
# logging info
import logging
# sqlalchemy as orm for sqlite db
from sqlalchemy import create_engine, MetaData
from sqlalchemy.ext.automap import automap_base
from sqlalchemy.orm import Session
from werkzeug.security import generate_password_hash, check_password_hash

# receive argument list to check in the db


def dbConnection(args):
    '''
       define API functions to deal with user and todo contents
       simulating an HTTP request
    '''

    def authClient(user, currentUser, currentPassword, serversession):
        response = dict()
        try:
            getUser = serversession.query(user).filter(
                user.username == currentUser).one()
            if getUser and check_password_hash(getUser.password, currentPassword):
                response['status'] = 200
            else:
                response['status'] = 401
        except:
            response['status'] = 500

        return response

    def registerUser(user, currentUser, currentPassword, serversession):
        response = dict()
        try:
            newUser = user(username=currentUser,
                           password=generate_password_hash(currentPassword))
            serversession.add(newUser)
            serversession.commit()
            response['status'] = 200
        except:
            response['status'] = 500

        return response

    def setTodo(todolist, currentUser, todoitem, serversession):
        response = dict()
        try:
            newTodo = todolist(username=currentUser, todoitem=todoitem, done=False)
            serversession.add(newTodo)
            serversession.flush()
            serversession.refresh(newTodo)
            response['id'] = newTodo.id
            serversession.commit()
            response['status'] = 200
        except:
            response['status'] = 500

        return response

    def markTodo(todolist, currentUser, id, serversession):
        response = dict()
        try:
            todo = serversession.query(todolist).filter(
                todolist.username == currentUser, todolist.id == id).one()
            # reverse state of todo
            todo.done = not todo.done
            serversession.add(todo)
            serversession.commit()
            response['status'] = 200
        except:
            response['status'] = 500

        return response

    def removeTodo(todolist, currentUser, id, serversession):
        response = dict()
        try:
            serversession.query(todolist).filter(
                todolist.username == currentUser, todolist.id == id).delete()
            serversession.commit()
            response['status'] = 200
        except:
            response['status'] = 500

        return response

    def getTodos(todolist, currentUser, serversession):
        response = dict()
        try:
            todos = serversession.query(todolist).filter(
                todolist.username == currentUser).order_by(todolist.done, todolist.id).all()
            response['status'] = 200
            response['todos'] = [{'id':todo.id , 'item':todo.todoitem,'done':todo.done} for todo in todos]
        except:
            response['status'] = 500
        return response

    '''
      Create Engine and session to use db
    '''
    serverengine = create_engine(
        'sqlite:////home/Tcp_Python-Server_Kotlin-Android-Client/Server/pserver.db')
    metadata = MetaData()
    metadata.reflect(serverengine)
    Base = automap_base(metadata=metadata)
    Base.prepare()
    user = Base.classes.user
    todolist = Base.classes.todolist
    serversession = Session(serverengine)

    '''
      parse arguments to determine proper function response
    '''

    answer = {'status', 400}
    if all(arg in args for arg in ('user', 'password')):
        answer = authClient(user, args['user'],
                            args['password'], serversession)
    
    if all(arg in args for arg in ('user', 'password', 'register')):
        answer = registerUser(user, args['user'],
                              args['password'], serversession)
    
    if all(arg in args for arg in ('user', 'password', 'todoitem')):
        answer = authClient(user, args['user'],
                            args['password'], serversession)
        if 'status' in answer and answer['status'] == 200:
            answer = setTodo(
                todolist, args['user'], args['todoitem'], serversession)
    
    if all(arg in args for arg in ('user', 'password', 'marktodo')):
        answer = authClient(user, args['user'],
                            args['password'], serversession)
        if 'status' in answer and answer['status'] == 200:
            answer = markTodo(todolist, args['user'], args['marktodo'], serversession)
    
    if all(arg in args for arg in ('user', 'password', 'todoitemremove')):
        answer = authClient(user, args['user'],
                            args['password'], serversession)
        if 'status' in answer and answer['status'] == 200:
            answer = removeTodo(
                todolist, args['user'], args['todoitemremove'], serversession)

    if all(arg in args for arg in ('user', 'password', 'gettodos')):
        answer = authClient(user, args['user'],
                            args['password'], serversession)
        if 'status' in answer and answer['status'] == 200:
            answer = getTodos(todolist, args['user'], serversession)

    '''
      close db engine and dispose engine, then return answer in json format
    '''

    serversession.close()
    serverengine.dispose()

    return json.dumps(answer)


class Handler(StreamRequestHandler):
    def handle(self):
        # receive message, process request and send message back
        arglist = json.loads(self.request.recv(1024).decode('utf-8'))
        logging.info("From <%s>: %s" % (self.client_address, arglist))
        response = dbConnection(arglist)
        self.request.sendall(bytes(response+'\n', 'UTF-8'))


class Server(TCPServer):

    SYSTEMD_FIRST_SOCKET_FD = 3

    def __init__(self, server_address, handler_cls):

        TCPServer.__init__(
            self, server_address, handler_cls, bind_and_activate=False)

        self.socket = socket.fromfd(
            self.SYSTEMD_FIRST_SOCKET_FD, self.address_family, self.socket_type)


if __name__ == "__main__":
    """
    Set host and port for communication and keep server alive
    """
    logging.basicConfig(level=logging.INFO)
    HOST, PORT = "0.0.0.0", 6500
    server = Server((HOST, PORT), Handler)
    server.serve_forever()
