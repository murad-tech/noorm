#!/bin/sh
# one-time setup after cloning noorm

git config core.hooksPath hooks && echo "hooks enabled: unit tests run on every commit"
