#!/usr/bin/env bash

PICOCLI_VERSION="4.6.1"
PICOCLI_JAR="${HOME}/.m2/repository/info/picocli/picocli/${PICOCLI_VERSION}/picocli-${PICOCLI_VERSION}.jar"

DINNER_VERSION=$(mvn -q --non-recursive -Dexec.executable=echo -Dexec.args='${project.version}' exec:exec)
DINNER_JAR="./target/philosophers-${DINNER_VERSION}.jar"

echo "Classpath: ${PICOCLI_JAR}:${DINNER_JAR}"

java -cp "${PICOCLI_JAR}:${DINNER_JAR}" org.solveme.philosophers.DinnerApp CIVILIZED