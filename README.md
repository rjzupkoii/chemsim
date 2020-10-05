# ChemSim

ChemSim is an agent-based modeling (ABM) approach to computational chemistry. ChemSim is primarily a Java application uses a custom scheduler to manage agents, although still contains some code based upon the use of [MASON Multiagent Simulation Toolkit](http://cs.gmu.edu/~eclab/projects/mason/).

## Development Environment

The following is the development environment:

- Eclipse IDE Neon Release (4.6.0 or 4.6.1)
- Java SE SDK 8 (JavaSE-1.8)

The SizeOf JAR file is included and is repsonsible for benchmarking the size of objects when the model starts. This is used to control how many agents are created. All other dependences are managed through the MAVEN POM file.

The following Eclipse plug-ins are recommended for developers wishing to see all documentation:

- ObjectAid UML Explorer for Eclipse (1.1.11)

## Execution

The simulation makes use of premain instrumentation and needs to be launched with the following parameters:

  -javaagent:lib/SizeOf.jar 

Depending upon the molelcue count, tuning of the JVM will be needed to either ensure enough space or prevent overcollection by the GC. One starting point is the following, but they are by no means guarneteed to be ideal:

 -Xms4G  -XX:+UseParallelGC -XX:NewRatio=4

### Launch Examples
With experimental data controlling hydrogen peroxide decay,
> java -javaagent:lib/SizeOf.jar -jar ChemSim.jar -c experiment/chemicals.csv -r experiment/reactions.csv -e experiment/experiment.csv -l 1000000

With linear decay of hydrogen peroxide,
> java -javaagent:lib/SizeOf.jar -jar ChemSim.jar -c experiment/chemicals.csv -r experiment/reactions.csv -l 1000000


# Publications

Zupko, R., Kamath, D., Coscarelli, E., Rouleau, M., & Minakata, D. (2020). Agent-Based model to predict the fate of the degradation of organic compounds in the aqueous-phase UV/H2O2 advanced oxidation process. Process Safety and Environmental Protection, 136, 49–55. https://doi.org/10.1016/j.psep.2020.01.023

