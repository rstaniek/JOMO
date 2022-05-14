from flask import Flask, Response, jsonify, request

from .errors import errors

app = Flask(__name__)
app.register_blueprint(errors)


@app.route("/")
def index():
    return Response("Hi mom 👋🏿!", status=200)


@app.route("/ping")
def ping():
    return Response('pong', status=200)


@app.route("/custom", methods=["POST"])
def custom():
    payload = request.get_json()

    if payload.get("say_hello") is True:
        output = jsonify({"message": "Hello!"})
    else:
        output = jsonify({"message": "..."})

    return output
