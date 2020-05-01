#!/usr/bin/env bash

function error() {
    echo -e "\033[31m$@\033[0m"
}

BIN=`which build-docker`
if [[ -z $BIN ]]; then
    error "Please define the cmd build-docker in your PATH"
    exit 1
fi

cd `dirname $0`
WORK_DIR=`pwd`
# PROJECT_DIR
cd ../../..

VERSION=`./gradlew :maid-cassandra:printVersion -q`
if [[ -z $VERSION ]]; then
    error "Please define printVersion task in build.gradle"
    exit 1
fi

./gradlew clean && ./gradlew -x test :maid-cassandra:bootJar

cp ./maid-cassandra/build/libs/maid-cassandra-$VERSION.jar $WORK_DIR/maid-server.jar
cd $WORK_DIR

# docker login --username=tukeof registry.cn-hangzhou.aliyuncs.com
REGISTRY_PREFIX="registry.cn-hangzhou.aliyuncs.com/tuke"
IMAGE_NAME="$REGISTRY_PREFIX/maid-server:$VERSION"
$BIN $IMAGE_NAME .
docker push $IMAGE_NAME

rm maid-server.jar
