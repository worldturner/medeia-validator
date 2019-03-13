#!/bin/bash

REPEATS=200
CP=target/medeia-validator-performance-0.8.4-SNAPSHOT-jar-with-dependencies.jar
CLASS=com.worldturner.medeia.schema.performance.v4meta.MetaSchemaPerformanceTestingKt

for i in {1..200}; do
    java -cp $CP $CLASS draft07 0
    java -cp $CP $CLASS draft07 1
    java -cp $CP $CLASS draft07 2
    java -cp $CP $CLASS draft07 3
    echo ===
done
