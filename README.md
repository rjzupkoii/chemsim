# ChemSim

ChemSim is an agent-based modeling (ABM) approach to computational chemistry. ChemSim is primarily a Java application that makes use of the [MASON Multiagent Simulation Toolkit](http://cs.gmu.edu/~eclab/projects/mason/) to manage to simulation. It uses the 1.6.0 release of [Java 3D](https://gouessej.wordpress.com/2012/08/01/java-3d-est-de-retour-java-3d-is-back/) ([GitHub Project](https://github.com/hharrison/java3d-core/releases/tag/1.6.0)) for visualization due to MASON dependencies.

## Development Environment

The following is the development environment:

- Eclipse IDE Neon Release (4.6.0 or 4.6.1)
- Java SE SDK 8 (JavaSE-1.8)

A number of JAR files are included in the repository that are dependencies of MASON and GeoMASON, additional project libraries not included are managed using the MAVEN POM file.

Additionally, the following Eclipse plug-ins are recommended for developers:

- ObjectAid UML Explorer for Eclipse (1.1.11)

## Execution

The simulation makes use of premain instrumentation and needs to be launched with the following parameters:

  -javaagent:lib/SizeOf.jar 

# Branches
