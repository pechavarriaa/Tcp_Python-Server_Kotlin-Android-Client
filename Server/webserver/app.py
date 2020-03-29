from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_login import LoginManager
from itsdangerous import URLSafeTimedSerializer


app = Flask(__name__)
app.config.from_pyfile('config.py')
db = SQLAlchemy(app)

login_manager = LoginManager()
login_manager.login_view = 'app.login_users'
login_manager.init_app(app)
login_manager.session_protection = "strong"
login_serializer = URLSafeTimedSerializer(app.secret_key)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=2020, debug=True,use_reloader=False)