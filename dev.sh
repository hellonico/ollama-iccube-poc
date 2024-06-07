#!/bin/bash

# parameters may be missing if too many
mvn -q compile exec:java -Dexec.args="$1 $2 $3 $4 $5 $6 $7 $8 $9"