#!/bin/bash

rm -f LICENSE
rm -f README.md
rm -rf .git
git init
git add build.sbt
git add project
git add src
git add .gitignore
git commit -m 'Initial Commit'
rm -f init.sh

