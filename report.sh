#!/bin/bash

# Define a list of strings
model_list=("qwen:7b" "mistral" "qwen:14b" "phi" "llama2" "llama2-uncensored" "llama3" "llama3:70b")
mkdir reports
rm -f reports/*
# Loop over the list of strings
for str in "${model_list[@]}"; do
  echo "Testing Model: $str"
  ollama pull $str
  ./ollama-iccube-poc.sh -m $str >> reports/$str.log
done