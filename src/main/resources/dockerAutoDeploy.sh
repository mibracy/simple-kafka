#!/bin/bash

# Declare Variables
DEPLOY_ENV="$(pwd)/host"
PATHS=( "conf" "webapps" "cacerts" "ojdbc")

for OPT in "${PATHS[@]}"
do
    case $OPT in

# Deploy ENV specific config files
      "conf")
        FILES=( "catalina.properties" )
        for FILE in "${FILES[@]}"
        do
          cp -p -v -r "$DEPLOY_ENV/$OPT/$FILE" "$(pwd)/$OPT"
        done
        ;;

# Deploy WAR contents
      "webapps")
        WARS=( "kafka")
        for WAR in "${WARS[@]}"
        do
          cp -p -v -r "$DEPLOY_ENV/$OPT/$WAR" "$(pwd)/$OPT"
        done
        ;;

# Deploy misc
      *)
        echo "UNUSED -> $OPT"
        ;;
    esac

done

echo SUCCESS Weinited!! || exit 1

catalina.sh run || exit 1
