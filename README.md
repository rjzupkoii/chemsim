# ChemSim

ChemSim is an agent-based modeling (ABM) approach to computational chemistry. ChemSim is primarily a Java application uses a custom scheduler to manage agents, although still contains some code based upon the use of [MASON Multiagent Simulation Toolkit](http://cs.gmu.edu/~eclab/projects/mason/) to manage to simulation.

## Development Environment

The following is the development environment:

- Eclipse IDE Neon Release (4.6.0 or 4.6.1)
- Java SE SDK 8 (JavaSE-1.8)

The MASON JAR file is included due to some remaining dependencies, additional project libraries not included are managed using the MAVEN POM file.

Additionally, the following Eclipse plug-ins are recommended for developers:

- ObjectAid UML Explorer for Eclipse (1.1.11)

## Execution

The simulation makes use of premain instrumentation and needs to be launched with the following parameters:

  -javaagent:lib/SizeOf.jar 

### Launch Examples
With experimental data controlling hydrogen peroxide decay,
> java -javaagent:lib/SizeOf.jar -jar ChemSim.jar -c experiment/chemicals.csv -r experiment/pathway2.csv -e experiment/experiment.csv -l 1000000

With linear decay of hydrogen peroxide,
> java -javaagent:lib/SizeOf.jar -jar ChemSim.jar -c experiment/chemicals.csv -r experiment/pathway2.csv -l 1000000


# Branches
