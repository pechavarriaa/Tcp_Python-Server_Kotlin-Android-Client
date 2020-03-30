# socketserver to tcp connections
import socketserver
# json to retrieve data type
import json
# sqlalchemy as orm for sqlite db
from sqlalchemy import create_engine, MetaData
from sqlalchemy.ext.automap import automap_base
from sqlalchemy.orm import Session
from werkzeug.security import generate_password_hash, check_password_hash

# receive argument list to check in the db


def dbConnection(arglist):
    '''
       define API functions to deal with user and todo contents
       simulating an HTTP request
    '''

    def authClient(user, currentUser, currentPassword, serversession):
        response = dict()
        try:
            qry = serversession.query(user.username, user.password).filter(
                user.username == currentUser).first()
            if qry and check_password_hash(qry[1], currentPassword):
                response['status'] = 200
            else:
                response['status'] = 401
        except:
            response['status'] = 500

        return response

    def registerUser(user, currentUser, currentPassword, serversession):
        response = dict()
        try:
            serversession.execute(user.insert().values(
                username=currentUser, password=generate_password_hash(currentPassword)))
            response['status'] = 200
        except:
            response['status'] = 500

        return response

    def setTodo(todolist, currentUser, todoitem, serversession):
        response = dict()
        try:
            serversession.execute(todolist.insert().values(
                username=currentUser, todoitem=todoitem))
            response['status'] = 200
        except:
            response['status'] = 500

        return response

    def removeTodo(todolist, currentUser, id, serversession):
        response = dict()
        try:
            serversession.query(todolist).filter(
                todolist.username == currentUser, todolist.id == id).delete()
            session.commit()
            response['status'] = 200
        except:
            response['status'] = 500

        return response

    def getTodos(todolist, currentUser, serversession):
        response = dict()
        try:
            qry = serversession.query(todolist.id, todolist.todoitem).filter(
                todolist.username == currentUser).order_by(todolist.date_created)
            tupleList = []
            for q in qry:
                tupleList.append(q[0], q[1])
            response['status'] = 200
            response['todos'] = tupleList

        except:
            response['status'] = 500
        return response

    '''
      Create Engine and session to use db
    '''
    serverengine = create_engine('sqlite:///pserver.db')
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

    args = json.loads(arglist)
    answer = {'status', 400}
    if all(arg in args for arg in ('user', 'password')):
        answer = authClient(user, args['user'],
                            args['password'], serversession)
    if all(arg in args for arg in ('user', 'password', 'regiser')):
        answer = registerUser(user, args['user'],
                              args['password'], serversession)
    if all(arg in args for arg in ('user', 'password', 'todoitem')):
        answer = authClient(user, args['user'],
                            args['password'], serversession)
        if 'status' in answer and answer['status'] == 200:
            answer = setTodo(
                todolist, args['user'], args['todoitem'], serversession)
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


class TCPServerHandler(socketserver.BaseRequestHandler):
    """
    Class of server to handle connections and return
    what is expected
    """

    def handle(self):

        arglist = json.loads(self.request.recv(1024).decode('utf-8'))
        response = dbConnection(arglist)

        self.request.sendall(bytes(response, 'UTF-8'))


if __name__ == "__main__":
    """
    Set host and port for communication and keep server alive
    """
    HOST, PORT = "0.0.0.0", 6500

    with socketserver.TCPServer((HOST, PORT), TCPServerHandler) as server:
        server.serve_forever()
