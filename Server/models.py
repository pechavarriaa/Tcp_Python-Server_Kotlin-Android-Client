from database import db
from flask_login import UserMixin
from werkzeug.security import generate_password_hash, check_password_hash


class User(UserMixin, db.Model):
    __tablename__ = 'user'
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(100), unique=True,  nullable=False)
    password = db.Column(db.String(100),  nullable=False)
    __table_args__ = ({'extend_existing': True})

    def __init__(self, username, password):
        self.username = username
        self.password = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password, password)

    def get_id(self):
        return self.id


class todolist(db.Model):
    __tablename__ = 'todolist'
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(100))
    todoitem = db.Column(db.String(150))
    date_created = db.Column(db.DateTime,  default=db.func.current_timestamp())
    __table_args__ = (db.Index('UI', username, todoitem, unique=True), {
                      'extend_existing': True})

    def __init__(self, todoitem):
        self.todoitem = todoitem
