from flask import Flask, make_response
from flask_restful import Resource, Api

app = Flask(__name__)
api = Api(app)

class HelloWorldTest(Resource):

    @app.route('/jomo/v1/hello')
    def test():
        headers = {"Content-Type": "application/json"}
        return make_response(
            "Hi mom 👌🏿",
            200,
            headers=headers
        )