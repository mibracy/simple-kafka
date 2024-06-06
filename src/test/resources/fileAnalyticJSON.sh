#!/bin/bash

# Function to display help
Help() {
  echo "Usage: $0 [options] [arguments]"
  echo "Options:"
  echo "  -h    Display this help message"
  echo "Arguments:"
  echo "  arg1  Required argument 1 (e.g., a directory path)"
}

#Function to create JSON representation of a directory
create_dir_json() {
    local dir="$1"
    local dir_name=$(basename "$dir")
    local file_count=$(find "$dir" -maxdepth 1 -type f | wc -l)
    local oldest_file=$(find "$dir" -maxdepth 1 -type f -printf '%T@ %p\n' | sort -n | head -n 1 | cut -d' ' -f2-)
    local newest_file=$(find "$dir" -maxdepth 1 -type f -printf '%T@ %p\n' | sort -n | tail -n 1 | cut -d' ' -f2-)
    local oldest_date=$(stat -c '%y' "$oldest_file" | cut -d' ' -f1)
    local newest_date=$(stat -c '%y' "$newest_file" | cut -d' ' -f1)
    local children=()

    for subdir in "$dir"/; do
        if [ -d "$subdir" ]; then
            children+=("$(create_dir_json "$subdir")")
        fi
    done

    local dir_json=$(jq -n --arg name "$dir_name" --arg file_count "$file_count" --arg oldest_date "$oldest_date" --arg newest_date "$newest_date" '{name: $name, file_count: $file_count, oldest_date: $oldest_date, newest_date: $newest_date, children: []}')

    if [ "${#children[@]}" -gt 0 ]; then
        dir_json=$(jq --argjson children "${children[]}" "$dir_json" '.children = $children')
    fi

    echo "$dir_json" >> file_analytic.json
}

# Process the input options and arguments
while getopts ":ha:" option; do
  case $option in
    h) # Display help
      Help
      exit;;
    a) # Optional argument 2
      arg2=$OPTARG;;
    \?) # Invalid option
      echo "Error: Invalid option"
      exit 1;;
  esac
done

# Shift the arguments to handle positional arguments
shift $((OPTIND - 1))

# Check if required argument 1 is provided
if [ -z "$1" ]; then
  echo "Error: Missing required argument 1"
  Help
  exit 1
fi

# Assign positional arguments
# Specify the root directory to start from
root_dir=$1

#Create the JSON representation
json_output=$(create_dir_json "$root_dir")
