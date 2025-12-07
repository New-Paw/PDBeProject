#!/bin/sh

DIR=$(dirname $0)

# trusting/skipping some artifacts may be problematic as they wont be recognized as dependencies and included in nix-build sandbox
# e.g., to prevent https://docs.gradle.org/current/userguide/dependency_verification.html#sec:skipping-javadocs
# or https://youtrack.jetbrains.com/issue/IDEA-258328
# this was the case of
# - compiler-bridge_2.12-1.9.3-sources.jar (org.scala-sbt:compiler-bridge_2.12:1.9.3)
# - org.codehaus.groovy:groovy-bom:3.0.14 required by org.apache.spark:spark-common-utils by org.apache.logging.log4j:log4j-1.2-api

# DO NOT trust the following if they are utilized in the project as it would omit the files from nix-build deps artifact and the build would fail
# trusting "kotlin-reflect-[0-9.]+.pom" added to prevent ococcasional errors when this lib is required by InteliJ IDEA
# trusting "kotlin-stdlib-[0-9.]+.module" added to prevent errors in InteliJ IDEA when the project is not using kotlin and IDEA plugin needs to compile *.gradle.kts files

# reset components checksums
sed -i '/<components>/,/<\/components>/d' ${DIR}/verification-metadata.xml
# gradle build task must be at least "classes" and "testClasses" (not "help") to detect all dependencies required for build/test
exec gradle --project-dir ${DIR}/.. --refresh-dependencies --write-verification-metadata sha256 classes testClasses
