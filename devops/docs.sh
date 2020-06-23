#!/usr/bin/env bash

apidoc -i maid-api/ -o apidoc/ --parse-languages java && serve -s ./apidoc

