#!/usr/bin/env bash

gunicorn --certfile /var/www/jomo-api.pem --keyfile /var/www/jomo-api.key --bind 0.0.0.0:8808 --log-level=debug --workers=1 wsgi:app