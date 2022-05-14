#!/usr/bin/env bash

gunicorn wsgi:app --bind 0.0.0.0:8080 --log-level=debug --workers=2 --certfile /var/www/api-jomo.pem --keyfile /var/www/jomo-api.key