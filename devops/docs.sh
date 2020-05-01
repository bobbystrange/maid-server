#!/usr/bin/env bash

apidoc -o apidoc/ --parse-languages java && serve -s ./apidoc

