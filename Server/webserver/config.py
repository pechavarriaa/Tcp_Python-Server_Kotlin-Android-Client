import os
# SECURITY
SECRET_KEY = 'pserver#4$secret#$%!key@'
SECURITY_PASSWORD_SALT = 'pserverSecurity'
# SQLALCHEMY CONFIG
try:
    SQLALCHEMY_DATABASE_URI = os.environ["DBPATH"]
except KeyError:
   print("Please set the environment variable DBPATH")
SQLALCHEMY_TRACK_MODIFICATIONS = False