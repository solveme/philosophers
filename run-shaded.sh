#!/usr/bin/env bash

PICOCLI_VERSION="$(cat pom.xml | grep '<picocli.version>' | sed -e 's/<\/.*//' | sed 's/^.*>//')"
PICOCLI_JAR="${HOME}/.m2/repository/info/picocli/picocli/${PICOCLI_VERSION}/picocli-${PICOCLI_VERSION}.jar"

DINNER_VERSION=$(mvn -q --non-recursive -Dexec.executable=echo -Dexec.args='${project.version}' exec:exec)
DINNER_JAR="./target/philosophers-${DINNER_VERSION}.jar"

ECHO_CLASSPATH="${SHOW_CLASSPATH:-false}"
if [[ "${ECHO_CLASSPATH}" == "true" ]]; then
    echo "Classpath:"
    echo "  - ${PICOCLI_JAR}"
    echo "  - ${DINNER_JAR}"
    echo
fi

java -cp "${PICOCLI_JAR}:${DINNER_JAR}" org.solveme.philosophers.DinnerApp $@