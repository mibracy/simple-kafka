#!/bin/bash

# Set the timeout in minutes from the first argument, or default to 1 minute
TIMEOUT_MINUTES=${1:-1}
TIMEOUT_SECONDS=$((TIMEOUT_MINUTES * 60))

echo "Starting process with a timeout of $TIMEOUT_MINUTES minute(s)."

# Run the loop_and_echo script in the background
$(pwd)/loop_and_echo.sh &

# Get the process ID (PID) of the background process
PID=$!

# Get the start time
START_TIME=$(date +%s)

# Continuously check the process
while ps -p $PID > /dev/null
do
  # Get the current time
  CURRENT_TIME=$(date +%s)

  # Calculate the elapsed time
  ELAPSED_TIME=$((CURRENT_TIME - START_TIME))

  # Check if the elapsed time is greater than the timeout
  if [ $ELAPSED_TIME -gt $TIMEOUT_SECONDS ]
  then
    echo -e "\nProcess has been running for more than $TIMEOUT_MINUTES minute(s). Attempting graceful shutdown..."
    kill $PID

    # Wait up to 10 seconds for the process to terminate
    for i in {1..10}; do
      if ! ps -p $PID > /dev/null; then
        break # Exit loop if process is gone
      fi
      sleep 1
    done

    # If the process is still alive after the grace period, force kill it
    if ps -p $PID > /dev/null; then
      echo "Process did not terminate gracefully. Sending SIGKILL."
      kill -9 $PID
    else
      echo -e "Process terminated successfully."
    fi
    break
  fi

  # Wait for 1 second before checking again
  sleep 1
done

echo -e "\nProcess finished or was killed."
