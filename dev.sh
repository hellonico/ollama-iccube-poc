#!/bin/bash

mvn -q compile exec:java -Dexec.args="$@"