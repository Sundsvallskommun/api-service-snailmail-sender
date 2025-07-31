#!/usr/bin/env bash

set -xe

cd test

if [ "$1" = '-f' ]; then
  docker compose down
fi

if docker compose ps app | grep -q '(healthy)'; then
  docker compose run --rm smoketest || docker compose logs app
else
  docker compose up --build -d
  docker compose logs -f
fi
