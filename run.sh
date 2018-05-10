#!/bin/bash

for ndx in {1..10}
do
  java -javaagent:lib/SizeOf.jar -jar ChemSim.jar -n $ndx -r experiment/simple.csv -c experiment/chemicals.csv -e experiment/experiment.csv
done
