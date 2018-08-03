#!/bin/bash

source env.sh

echo "Create artifacts directory"
mkdir artifacts

set -e

# Build the scala package
echo "Build data-feed-counter Scala package"
cd function-data-feed-counter
sbt assembly
cd -
cp function-data-feed-counter/target/scala-2.12/data-feed-counter.jar artifacts/data-feed-counter.jar

# Build the scala package
echo "Build sentiment-tracker Scala package"
cd function-sentiment-tracker
sbt assembly
cd -
cp function-sentiment-tracker/target/scala-2.12/sentiment-tracker.jar artifacts/sentiment-tracker.jar

serverless deploy
