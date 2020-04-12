import sys
from flask import Flask, redirect, render_template, request, url_for, abort, flash, send_from_directory, jsonify
from flask_sqlalchemy import SQLAlchemy
from itsdangerous import URLSafeTimedSerializer
from flask_login import LoginManager, current_user, login_user, logout_user
from forms import LoginForm, SignUpForm
from models import User, todolist
from database import db
from functools import wraps
import os

# Setup and Initialization
app = Flask(__name__)
app.config.from_pyfile('config.py')
db.init_app(app)

login_manager = LoginManager()
login_manager.login_view = 'app.login_users'
login_manager.init_app(app)
login_manager.session_protection = "strong"
login_serializer = URLSafeTimedSerializer(app.secret_key)

# Create models from models.py


def setup_database(app):
    with app.app_context():
        db.create_all()

# login manager
@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))


def login_required(f):
    @wraps(f)
    def wrap(*args, **kwargs):
        if current_user.is_active:
            return f(*args, **kwargs)
        else:
            return redirect(url_for('login_users'))
    return wrap


# app routes to build endpoints
@app.route('/', methods=['GET', 'POST'])
def index():
    if not current_user.is_authenticated:
        return redirect(url_for('login_users'))

    user = current_user.username
    todos = []
    qry = todolist.query.filter_by(username=user).order_by(
        todolist.done, todolist.id).all()
    for q in qry:
        todos.append(q)

    return render_template('index.html', user=user, todos=todos)


@app.route('/download', methods=['GET'])
def download():
    dwn = os.path.join(app.root_path, app.config['UPLOAD_FOLDER'])
    return send_from_directory(directory=dwn,
                               filename='todo_list.apk', mimetype='application/vnd.android.package-archive', as_attachment=True)


@app.route('/login', methods=['GET', 'POST'])
def login_users():
    if current_user.is_authenticated:
        return redirect(url_for('index'))

    form = LoginForm()
    if request.method == 'POST':
        if form.validate_on_submit():
            user = User.query.filter_by(username=form.username.data).first()
            if not user or not user.check_password(form.password.data):
                flash('Invalid Credentials', category='warning')
                return render_template('login.html', form=form)
            login_user(user)
            return redirect(url_for('index'))

    return render_template('login.html', form=form)


@app.route('/logout', methods=['GET', 'POST'])
def logout():
    if current_user.is_authenticated:
        logout_user()
        flash('Succesfully Logout', category='secondary')
    return redirect(url_for('login_users'))


@app.route('/sign-up', methods=['GET', 'POST'])
def sign_up():

    if current_user.is_authenticated:
        return redirect(url_for('index'))

    form = SignUpForm()
    if form.validate_on_submit():
        user = User.query.filter_by(username=form.username.data).first()
        if user:
            flash('Username is taken', category='warning')
            return render_template('signup.html', form=form)

        try:
            new_user = User(
                username=form.username.data,
                password=form.password.data)
            db.session.add(new_user)
            db.session.commit()
            flash(
                f'Account created for {form.username.data}', category='success')
        except:
            flash(f'Error Creating Account', category='danger')

        try:
            login_user(new_user)
        except:
            pass

        return redirect(url_for('index'))

    return render_template('signup.html', form=form)


@app.route('/addtodo')
@login_required
def addtodo():
    if 'todo' not in request.args:
        return jsonify(answer='MISSING_ARGUMENT')
    todoitem = request.args['todo']
    user = current_user.username
    if db.session.query(todolist.id).filter_by(
            username=user, todoitem=todoitem).scalar() is not None:
        return jsonify(answer='DUPLICATE')

    todos = []
    toadd = todolist(user, todoitem, False)
    try:
        db.session.add(toadd)
        db.session.commit()
    except:
        db.session.rollback()
        return jsonify(answer='INSERTION ERROR')

    qry = todolist.query.filter_by(username=current_user.username).order_by(
        todolist.done, todolist.id).all()
    for q in qry:
        td = q.__dict__
        del td['_sa_instance_state']
        todos.append(q.__dict__)

    return jsonify(answer='OK', todos=todos)


@app.route('/marktodo')
@login_required
def marktodo():
    if 'todoid' not in request.args:
        return jsonify(answer='MISSING_ARGUMENT')

    todoid = request.args['todoid']

    if db.session.query(todolist.id).filter_by(id=todoid, username=current_user.username).scalar() is None:
        return jsonify(answer='ID_NOT_FOUND')

    todo = todolist.query.filter_by(id=todoid).first()
    todo.done = not todo.done

    try:
        db.session.commit()
    except:
        db.session.rollback()
        return jsonify(answer='UPDATE_ERROR')

    todos = []
    qry = todolist.query.filter_by(username=current_user.username).order_by(
        todolist.done, todolist.id).all()
    for q in qry:
        td = q.__dict__
        del td['_sa_instance_state']
        todos.append(q.__dict__)

    return jsonify(answer='OK', todos=todos)


@app.route('/deletetodo')
@login_required
def deletetodo():
    if 'todoid' not in request.args:
        return jsonify(answer='MISSING_ARGUMENT')

    todoid = request.args['todoid']
    if db.session.query(todolist.id).filter_by(id=todoid, username=current_user.username).scalar() is None:
        return jsonify(answer='ID_NOT_FOUND')

    todo = todolist.query.filter_by(id=todoid).first()  
    try:
        db.session.delete(todo)
        db.session.commit()
    except:
        db.session.rollback()
        return jsonify(answer='DELETE_ERROR')

    todos = []
    qry = todolist.query.filter_by(username=current_user.username).order_by(
        todolist.done, todolist.id).all()
    for q in qry:
        td = q.__dict__
        del td['_sa_instance_state']
        todos.append(q.__dict__)

    return jsonify(answer='OK', todos=todos)

# run app.py to test locally
if __name__ == '__main__':
    if 'createModels' in sys.argv:
        print("Creating database tables...")
        setup_database(app)
        print("Done!")
    else:
        app.run(host='0.0.0.0', port=2030, debug=True)
