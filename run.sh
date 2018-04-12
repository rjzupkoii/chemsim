#!/bin/bash

for ndx in {1..10}
do
  java -javaagent:lib/SizeOf.jar -jar ChemSim.jar -run $ndx -reactions experiment/simple.csv -chemicals experiment/chemicals.csv
done
