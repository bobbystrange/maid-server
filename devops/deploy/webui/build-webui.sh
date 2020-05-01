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

cd ../../../../maid-webui/
yarn run build
cd $WORK_DIR
cp -r ../../../../maid-webui/build .

VERSION="0.1.0"
REGISTRY_PREFIX="registry.cn-hangzhou.aliyuncs.com/tuke"
IMAGE_NAME="$REGISTRY_PREFIX/maid-webui:$VERSION"
$BIN $IMAGE_NAME .
docker push $IMAGE_NAME

rm -rf build/
