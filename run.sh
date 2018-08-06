#!/bin/bash

# Check for expected parameter
if [ -z "$1" ]; then
  echo "Usage: ./run.sh [pathway]"
  exit 1
fi
REACTIONS=$1

# Note the inputs
#experiment=experiment/experiment.csv
chemicals=experiment/chemicals.csv
reactions=experiment/$REACTIONS.csv
path=../data
zip=../$REACTIONS.zip

# Clean-up the data directory
rm -rf data
mkdir data

# Run the simulation
for ndx in {1..1}
do
  java -javaagent:lib/SizeOf.jar -Xms4G -XX:+UseG1GC -jar ChemSim.jar -n $ndx -r $reactions -c $chemicals -l 1.0E+06 -w 1 -s 1  
done

# Move the results, last console
mkdir data/molecules
mv data/results-* data/molecules
mkdir data/mols
mv data/molar-* data/mols
mv console.txt data

# Make a copy of the experimental inputs
cp $reactions data
cp $chemicals data
#cp $experiment data

# Generate the plots
./analysis.R

# Compress the raw data
cd data; zip -r -X "$zip" * -x "*.DS_Store"
