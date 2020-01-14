#!/usr/bin/env bash

# This script takes .tsv files containing preregistrations and uploads them
# to the demo server, associating each one with a username.
#
# The expected format of the preregistration data is a .tsv (tab-separated
# values) file containing in each row:
#   1. Timestamp - (2018-01-01T12:34:56)
#   2. Serial # - (decimal)
#   3. Appid - (hex)
#   4. Challenge (base64)
#   5. Public Point (base64)
#   6. Key Handle (base64)
#   7. Registration Signature (urlsafebase64)
#   8. Attestation Certificate (base64)

usage ()
{
  echo 'Usage: ./load_preregistration.sh <host> <tsv file> [username(s)...]'
  echo "Runs the 'preregister/finish' API call on <host>, for every line in <tsv file>."
  echo 'The number of usernames supplied must match number of lines in tsv file.'
  echo
  echo '  Example:'
  echo '    ./load_preregistration.sh localhost:8443 regs.tsv sam jane'
  echo '    [where regs.tsv has 2 lines]'
  exit
}

if [ "$#" -lt 3 ]
then
  usage
fi

if [ ! -f $2 ]; then
    echo "File '$2' not found."
    usage
fi

lines=$(wc -l < $2 | xargs) # trims whitespace
numlines=${lines/.*}
numusers="$(($#-2))"
if [ "$numusers" -ne "$numlines" ]
then
  echo "Got a tsv file with $numlines lines, but got $numusers usernames."
  usage
fi

i=3
while read f; do
  # escape the tsv row
  tsvcontent=$(echo $f | sed $'s; ;\\\\t;g')
  # random base64url user id
  arbitraryid=$(base64 < /dev/urandom | tr -d 'O0Il1+\' | head -c 86 | sed 's/+/-/g; s/\//_/g')
  # make the API call
  output=$(curl --insecure -X POST \
    "https://$1/api/v1/preregister/finish" \
    -H 'Accept: */*' \
    -H 'Accept-Encoding: gzip, deflate' \
    -H 'Cache-Control: no-cache' \
    -H 'Connection: keep-alive' \
    -H 'Content-Type: application/json' \
    -H "Host: $1" \
    -H 'cache-control: no-cache' \
    -d "{
    \"tsvRow\":\"$tsvcontent\",
    \"user\": {\"name\":\"${!i}\", \"displayName\":\"No Display Name\", \"id\": \"$arbitraryid\"}
  }")
  echo
  echo $output
  echo
  ok=$?
  if [ $ok -eq 0 ]; then
    if [[ $output == *"success"* ]]; then
      echo "Successfully loaded preregistration '${!i}'."
    else
      echo "Failed to load preregistration for '${!i}'."
    fi
  else
    echo "Failed to send preregistration '${!i}'."
  fi
  echo
  i=$((i+1))
done <$2