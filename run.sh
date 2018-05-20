#!/bin/bash

# Note the inputs
experiment=experiment/experiment.csv
chemicals=experiment/chemicals.csv
reactions=experiment/reactions.csv
path=../data
zip=../scavenging.zip

# Clean-up the data directory
rm -rf data
mkdir data

# Run the simulation
for ndx in {1..10}
do
  echo $ndx -r $reactions -c $chemicals -e $experiment
  java -javaagent:lib/SizeOf.jar -jar ChemSim.jar -n $ndx -r $reactions -c $chemicals -e $experiment 
done

# Move the results, last console
mkdir data/molecules
mv data/results-* data/results
mkdir data/mols
mv data/molar-* data/mols
mv console.txt data

# Make a copy of the experimental inputs
cp $reaction data
cp $chemicals data
cp $experiment data

# Generate the plots
./analysis.R

# Compress the raw data
cd data; zip -r -X "$zip" * -x "*.DS_Store"