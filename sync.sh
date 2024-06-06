./package.sh
rsync -rtvu --progress . cute-1:ollama-iccube-poc/ --exclude grades_generator_py --exclude reports