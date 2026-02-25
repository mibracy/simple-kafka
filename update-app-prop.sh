#!/bin/bash

if [ $# -ne 2 ]; then
  echo "Usage: $0 <property> <value>"
  exit 1
fi

property="$1"
value="$2"

sed -i "s|^${property}=.*|${property}=${value}|" application.properties
