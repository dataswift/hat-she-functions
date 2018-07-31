#!/bin/bash

source env.sh

echo "Create artifacts directory"
mkdir artifacts

set -e

# Build the scala package
echo "Build Scala package"
cd hat-she-functions
sbt assembly
cd -
cp hat-she-functions/target/scala-2.12/hat-she-functions.jar artifacts/hat-she-functions.jar

serverless deploy
