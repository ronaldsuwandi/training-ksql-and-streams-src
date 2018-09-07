#! /bin/bash
LABS=$(pwd)
FILES=$(find `pwd` -name build.gradle)
for FILE in $FILES
do
    DIR=$(dirname $FILE)
    cd $DIR
    printf "\n\n\n*** Compiling project in $DIR\n"
    docker container run --rm \
        -v "$PWD":/home/gradle/project \
        -v "$HOME"/.gradle:/root/.gradle \
        -w /home/gradle/project \
        frekele/gradle:latest gradle build --warning-mode=none
done
