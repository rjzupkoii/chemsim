#!/bin/bash

FILES=$(ls experiment/*.csv | grep "experiment/.*[a|b]\.csv")
for FILE in $FILES; do
	REACTIONS=$(echo $FILE | awk -F'[/.]' '{print $2}')
	./run.sh $REACTIONS	
	mv data $REACTIONS
done