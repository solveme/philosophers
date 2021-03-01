#!/usr/bin/env bash

CURRENT_DIR="$( cd $( dirname ${BASH_SOURCE[0]} ) >/dev/null 2>&1 && pwd )"

${CURRENT_DIR}/target/appassembler/bin/dinner $@