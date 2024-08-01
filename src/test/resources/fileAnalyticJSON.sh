#!/bin/bash

# Function to display help
Help() {
  echo "Usage: $0 [options] [arguments]"
  echo "Options:"
  echo "  -h    Display this help message"
  echo "Arguments:"
  echo "  arg1  Required argument 1 (e.g., a directory path)"
}

# Function to create JSON representation of a directory
create_dir_json() {
    local dir="$1"
    local dir_name=$(basename "$dir")
    local file_count=$(find "$dir" -maxdepth 1 -type f | wc -l)
    local oldest_file=$(find "$dir" -maxdepth 1 -type f -printf '%T@ %p\n' | sort -n | head -n 1 | cut -d' ' -f2-)
    local newest_file=$(find "$dir" -maxdepth 1 -type f -printf '%T@ %p\n' | sort -n | tail -n 1 | cut -d' ' -f2-)
    local oldest_date=$(stat -c '%y' "$oldest_file" | cut -d' ' -f1)
    local newest_date=$(stat -c '%y' "$newest_file" | cut -d' ' -f1)
    local runtime=$(date -I )
    local resources=$(echo "CPU `LC_ALL=C top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}'`% RAM `free -m | awk '/Mem:/ { printf("%3.1f%%", $3/$2*100) }'` HDD `df -h / | awk '/\// {print $(NF-1)}'`")

    # Convert the Bash array to JSON format
    local json_output="{ \"name\": \"$dir_name\", \"path\": \"$dir\", \"file_count\": \"$file_count\", \"oldest_date\": \"$oldest_date\", \"newest_date\": \"$newest_date\", \"runtime\": \"$runtime\",\"resources\": \"$resources\" }"

    # Send Event to Kafka REST
    curl -X POST \
      http://192.168.0.101:8666/kafka/api/anal \
      -H 'Content-Type: application/json' \
      -H 'topic: anal' \
      -H 'key: anal' \
      -H 'Authorization: Bearer 7F=pm/BLGERWXmnP5!coVsyQjCs1De2E0F5TkHeJTn1O77M3z-sK?ke58ZCGQblEd12f/d5fDZHBuYKu9qLC=ewxehzv-ys9YKG-F24IIOV?QFqA?tD5kPuqy5ClL/f0NKKqm9JSgbje3WpqL5i!d!52C08-0cyBLC?DORcNUbmJFlzQZ9jGyGsaN4r4OWCWS6zqhZVInBGBJ-AjbJJlTkqovz0/r/amf0svl4!h3i8EhwAbUq!TC6ldmdJZDnE7m5gU-in3ZEYK5rU-hcuEd1a=n8jc8ouu/1i7DocTE2Z1HAl-ni/K1et781W3wVJYLFQqT4XoEyWDUdsY4QXxA/gesHTQygJS4w2?goHj9Inb=wgRD!29kND!malfjyskBZ=cNYHmN46y/40gzBGEBloq-69TEjuZWbmYyrbqBL/u/YfWwpk4oMiivpbb/a-IYeLglw?AirePcoi!!Gnh?qoQLlZ!4NBz-5ExODO14?9EPI/kUcu!ydRxHGI1uz?QMyWLBXSj!JnS0A!MOwHgjZMd52qO1TTLaO?/7q/5PhKd9sIsTmBWYTfpgI2MhzgjZJ5KbWTalcRoX!gfbpGrY6tBewq-!JTD!xi5MowCI7YG79fiSAxMIzldfGnqbYCon734zLijkn73Ed8wDHFffMVW7xk8vW5ysoH/8RTpQJ-PCFq8iFoBsUD9hZz-tIWlCjVN7N-rF2kUigz3rYFhKkl7xHyzaG63zY0SO1fzDBOZtjUh2q!C59HgjyK9EEpR1A!ZwZGgzBwiqzNFQ2vdsokT-CaZZ?oFKAf-!kVX8jxei2CO-GjvVA6!mSjQV4L=y3qJOvjtply8ox-dlTz-/u06Ke0Un5!4GUuvDICyIap?NqntUR3YfwF5hcpTC=gHY!Vc?uXYmiHE?2yAvZuHlVCQuBIT5QQwXnJW2YC3w7K9=ziaSzsuaRQnS!ul1nPApaMZ9E2lWzm-P/Uiv2=Iv-YsxnCdeCt-t?llH/nEi0ILNzPpLtfhDv1VmC1Rr=1TY19i?haIkMtx=!AVgQYeEewvShiYjhl7vvYB-1vCBGhZiG1lzFrkrmTFPzx3sYbM?JAf5jyxU/UBBoUYiarQAu8V3A6fW859L2WE/6806QSS3qoSSrJsF8nF1Xs796cr?gjidtvh7hmA2uS5wjs-kreh!oIXw9JursjaRJ?cLekLSWaeJwrIES1qwuMV3WiJaVY0X!WCJUfEstJKb1A=MhHGpx4tNokuu-R!lBwi5/7T/NIuMy=eykJnRGm4-vbpChrFaWJKHvqs11xOc67WLR6UQw8Eq2P7gml0s5Y1CMwJdsLLEsY/LjbMCH=Im2yfSboilqs9ewVjGsWcxCkwa47LqdXCEhyWS0=eHj1nbywkNbC=!!urD40dDxPPWbcIc9m4y27yej3VdUIfT2-zdcALZ=0lxT287L3?W3LLUycizHVsvs8h/FQhm-!8Mr31YZF6Kxg6H6=FZSU8jL7gCG3imBfU=lIROV/GUO/x8gGWhkR3meU90Uswesj07vJdt-qa3lJg!IkFXkjr7YE8U8zWE8Tsv5VVLQFaTP/REy!!qe/FPfbdSPZk!UH82s-7eNZZisQyZXm1jyeZImu2sLGAZyuK4x//!OTFzV!inXS?6yFRrmXZg1mh52p40jtbU-chvPvEQ94GaMmBPdw?P/rYCBiSFsT5V8ivuOyFo--wakBl5HjCbfmIYK?D50isQcfiMLrl!72MyU/3=x6bdiQ8cen=KVZR=JxGX0RRt?VttRcragMlvAdxtSkBsywEUadHN=35ujASqaqlVBSYD6SUw3D-yiCqcsmjrW1UqwSsgmv/KseIGwhygZrXOKMoRvV9Gvz4pLYKiYxM0jS8sRmSZHHh3BHBlLs/jGbw7MWNIAM2/Ao1aTRf-RR3arVOm2psEOw8txYuW9ejAzG8rH-BVLcvX0g7DclRa!M4G?mmfB9T0awJHh=BqZcePWyW0Kyf3Z/nCPE-4M/q6fbyfjVSQgM1?06?Wbsp7h1iygDS9MBPkEEw9hN7mqP=ldZF' \
      -d "$json_output"

    echo "$json_output" >> file_analytic.json
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
