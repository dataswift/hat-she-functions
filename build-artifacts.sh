#!/bin/bash

source env.sh

echo "Create artifacts directory"
mkdir artifacts

set -e

# Build the scala package
echo "Build Scala package"
cd status-monitor
sbt assembly
cd -
cp status-monitor/target/scala-2.12/status-monitor.jar artifacts/status-monitor.jar

serverless deploy
