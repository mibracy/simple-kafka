#!/bin/bash

## Input CSV file
input_csv="inputFiles.csv"

## Output JSON file
output_json="AEDReconcile.json"

## Set the directory to search for files
directory="./files"

## Initialize JSON array
echo "[" > "$output_json"

## Read CSV file and process each row
awk -F',' '{print $0}' "$input_csv" |
while IFS= read -r row
do
    ## Extract filename from CSV row (assuming it's the first column)
    filename=$(echo "$row" | cut -d',' -f1)

    ## Find the file and get its creation date
    creation_date=$(find $directory -name "$filename" -printf '%T@\n' | sort -n | head -n 1)

    ## Convert creation date to human-readable format
    human_date=$(date -d "@${creation_date%.*}" "+%Y-%m-%d %H:%M:%S")

    ## Create JSON object for the file using string operations
     json_object="  {
     \"filename\": \"$filename\",
     \"creation_date\": \"$human_date\"
   }"

     ## Append JSON object to output file
     echo "$json_object," >> "$output_json"
done

## Remove trailing comma and close JSON array
sed -i '$ s/,$//' "$output_json"
echo "]" >> "$output_json"

echo "Processing complete. Output saved to $output_json"