#!/bin/bash

# Define the log filename
LOG_FILE="audit.log"
ENV=

# Function to ask the user questions and generate the log entry
ask_questions() {
  bold=$(tput bold)
  normal=$(tput sgr0)
  
  # Question 1: Name of the user
  read -rp "              Enter your ID: " username
  if [[ "$username" == "" ]]; then
    echo -e "Error: Must enter ID!"
    return 1
  fi

  # Question 2: Date (format: MM/DD/YYYY)
  # read -p "  WARNING: Have you verified this is correct ENV for change request?: (y/${bold}N${normal})" confirmation
  # if [[ "$confirmation" != "y" ]]; then
  #   echo -e "Error: Please verify before running again!"
  #   return 1
  # fi

  date_input=$(date +"%m-%d-%Y %H:%M:%S")
  echo -e "Date: $date_input" >> "$LOG_FILE"
  echo -e " User: $username" >> "$LOG_FILE"

  while [ 1 ]
  do
    menu
    echo -e "  -> Activity Input: $option" >> "$LOG_FILE"
    case $option in
      0)
        break 1 ;;
      1)
        diskspace ;;
      2)
        available_mem ;;
      3)
        logged_in_users ;;
      4)
        prepare_deployment ;;
      5)
        verify_artifact ;;
      6)
        execute_deployment ;;
      7)
        monitor ;;
      *)
        clear
        echo -e "\t\tThat isn't a valid option."
    esac
    echo -en "\n\n\t\t Hit any key to continue..."
    read -rn 1
  done
  clear

  echo -e "\n" >> "$LOG_FILE"

  echo -e "\n$ENV Log entry has been generated and saved to $LOG_FILE."
}

monitor () {
    btop
}

prepare_deployment() {
  current_dir=$(pwd)
  parent_dir="$(dirname "$current_dir")"
  verify_env
  date_input=$(date +"%Y-%m-%d")

  echo -e "  -> Compressing..." >> "$LOG_FILE"

  mkdir -p "$current_dir/$date_input"
  zip -FSr -X "$current_dir/$date_input/artifact.zip" \
  "$parent_dir"/* \
  -x "$parent_dir/bin/*" \
  -x "$parent_dir/resources/env.json" \
  >> "$LOG_FILE"

  if [ $? -ne 0 ]; then
    clear
    echo
    echo -e "\t\t     Error Archiving!"
    return 1
  fi

  clear
  echo
  echo -e "\tCreated Archive -> $current_dir/$date_input/artifact.zip"
}

verify_artifact() {
  current_dir=$(pwd)
  parent_dir="$(dirname "$current_dir")"
  verify_env
  date_input=$(date +"%Y-%m-%d")

   # Get the list of directories and extract the date part
   dates=$(find $current_dir -maxdepth 1 -type d -name "????-??-??" | sort -nr | sed 's|.*/||')

   # Convert dates to an array
   IFS=$'\n' read -d '' -ra date_array <<< "$dates"

   # Display options to the user
   clear
   echo
   echo -e "\t\tSelect a date:"
   for i in "${!date_array[@]}"; do
       echo -e "\t$((i+1)). ${date_array[i]}"
   done

   # Get user input
   while [ 1 ]
   do
      read -p "Enter your choice (1-${#date_array[@]}): " choice

      # Validate and display the selected date
      if [[ $choice =~ ^[0-9]+$ ]] && [ "$choice" -ge 1 ] && [ "$choice" -le "${#date_array[@]}" ]; then
          selected_date=${date_array[$((choice-1))]}
          break
      else
          echo "Invalid choice"
      fi
   done

  echo -e "  -> Test Extracting..." >> "$LOG_FILE"
  clear
#  unzip -l "$current_dir/$selected_date/artifact.zip"

  diff -y --suppress-common-lines -W 333  <(unzip -lqq "$current_dir/$date_input/artifact.zip" | sort -k 4) <(unzip -lqq "$current_dir/$selected_date/artifact.zip"  | sort -k 4)
  echo
  diff <(md5sum "$current_dir/$date_input/artifact.zip" | cut -f1 -d ' ') <(md5sum "$current_dir/$selected_date/artifact.zip" | cut -f1 -d ' ')

  if [ $? -eq 0 ]; then
    clear
    echo
    echo -e "\t\t\tMatches!"
  fi
  # insert steps to copy files
  # cp -v artifact/lib/*.jar ../dev/lib/  >> "$LOG_FILE"
  # cp -v artifact/resources/* ../dev/resources/  >> "$LOG_FILE"

}

verify_env() {
    if [[ $(whoami) == "mikey" && $(hostname) == "ubertoaster" ]]; then
      echo -e "  -> Yippy Kai-Yee!!" >> "$LOG_FILE"
      ENV="TEST"
    else
      echo -e "  -> Issue Detected!" >> "$LOG_FILE"
    fi
}

diskspace() {
  clear
  lsblk -f
}

available_mem() {
  clear
  free --giga -h -t
}

logged_in_users() {
  clear
  who
}

function menu {
  clear
  echo
  echo -e "\t\t IUTool Menu:\n"
  echo -e "\t1. Display Disk Space."
  echo -e "\t2. Display Available Memory."
  echo -e "\t3. View user(s) on system."
  echo -e "\t4. Create an Artifact ZIP."
  echo -e "\t5. Compare contents of Artifact ZIPs."
  echo -e "\t6. Deploy an Artifact ZIP."
  echo -e "\t7. Monitor System."
  echo -e "\t0. Exit menu."
  echo
  echo -en "\t\t Enter option: "

  read -rn 1 option
}


# Main program
clear
echo
echo -e "\tWelcome to Mikey's Interactive Unix Tool!"
if ask_questions; then
    # Display the log file contents if requested by the user
    read -rp "Would you like to view the log file? (y/${bold}N${normal}): " view_log
    if [[ $view_log == 'y' ]]; then
        clear
        cat "$LOG_FILE"
    fi
fi