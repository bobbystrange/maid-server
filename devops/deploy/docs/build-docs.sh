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

# copy auth to current directory
cp -r ../../../../../pepper/rita-auth .

# cd to PROJECT_DIR
cd ../../..
rm -rf apidoc/
apidoc -o apidoc/ --parse-languages java

cd $WORK_DIR
cp -r ../../../apidoc .
rm -rf rita-auth

VERSION="0.1.0"
#REGISTRY_PREFIX="registry.cn-hangzhou.aliyuncs.com/tuke"
REGISTRY_PREFIX="tukeof"
IMAGE_NAME="$REGISTRY_PREFIX/maid-docs:$VERSION"
$BIN $IMAGE_NAME .
docker push $IMAGE_NAME

rm -rf apidoc/
