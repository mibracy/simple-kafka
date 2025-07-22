#!/bin/bash

START_TIME=$(date +%s)

while true
do
  CURRENT_TIME=$(date +%s)
  ELAPSED_TIME=$((CURRENT_TIME - START_TIME))
  echo -e "\tRunning for $ELAPSED_TIME seconds."
  sleep 5
done
