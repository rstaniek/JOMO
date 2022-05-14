#!/usr/bin/env bash

gunicorn wsgi:app --bind 0.0.0.0:8808 --log-level=debug --workers=2 --certfile /var/www/jomo-api.pem --keyfile /var/www/jomo-api.key