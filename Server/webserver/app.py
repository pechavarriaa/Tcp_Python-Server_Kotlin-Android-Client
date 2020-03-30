import sys
from flask import Flask, redirect, render_template, request, url_for, abort
from flask_sqlalchemy import SQLAlchemy
from itsdangerous import URLSafeTimedSerializer
from flask_login import LoginManager,current_user, login_user, logout_user
from models import User, todolist
from database import db

## Setup and Initialization
app = Flask(__name__)
app.config.from_pyfile('config.py')
db.init_app(app)

login_manager = LoginManager()
login_manager.login_view = 'app.login_users'
login_manager.init_app(app)
login_manager.session_protection = "strong"
login_serializer = URLSafeTimedSerializer(app.secret_key)

## Create models from models.py
def setup_database(app):
    with app.app_context():
        db.create_all()

## login manager
@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))

## app routes to build endpoints
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

## run app.py to test locally
if __name__ == '__main__':
    if 'createModels' in sys.argv:
        print("Creating database tables...")
        setup_database(app)
        print("Done!")
    else:
        app.run(host='0.0.0.0', port=2020, debug=True,use_reloader=False)