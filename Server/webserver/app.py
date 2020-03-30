from flask import Flask, redirect, render_template, request, url_for, abort
from flask_sqlalchemy import SQLAlchemy
from flask_login import LoginManager
from itsdangerous import URLSafeTimedSerializer
from flask_login import current_user, login_user, logout_user
from models import User, todolist

app = Flask(__name__)
app.config.from_pyfile('config.py')
db = SQLAlchemy(app)

login_manager = LoginManager()
login_manager.login_view = 'app.login_users'
login_manager.init_app(app)
login_manager.session_protection = "strong"
login_serializer = URLSafeTimedSerializer(app.secret_key)


@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))


@app.route('/')
def index():
    # if not current_user.is_authenticated:
    #     return redirect(url_for('login_users'))
    return "Hello World!"
    

@app.route('/sign-in', methods=['GET', 'POST'])
def login_users():

    if current_user.is_authenticated:
        return redirect(url_for('index'))
    
    # if request.method == 'POST':
    return abort(404)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=2020, debug=True,use_reloader=False)