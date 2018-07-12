#!/bin/bash


for train in $(echo de en gr zh); do for test in $(echo de en gr zh) ; do java -cp target/colors-1.0-SNAPSHOT-jar-with-dependencies.jar weka.classifiers.trees.RandomForest -t data/defEmoVector-$train.csv.arff -T data/defEmoVector-$test.csv.arff > train-$train-test-$test.res ; done& done
