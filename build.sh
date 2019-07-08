#!/usr/bin/env bash

set -e

function usage() {
  echo >&2 "Usage: $0 [-f] (build fat jar) [-t target (darwin|docker)] [-i] [-j]"
}

#The colon represents an argument!
while getopts :ft:i:j opt
do
  case "$opt" in
    f)
        echo >&2 "Building Fat Jar..."
        mvn package
        echo >&2 "Built Fat Jar Successfully - look for target/xmlJsonConverter.jar-1.0-SNAPSHOT-jar-with-dependencies"
        ;;
    t)
        TARGET="$OPTARG"
        if [[ "${TARGET}" == "darwin" ]]
        then
          echo >&2 "Building native Image for Mac..."
#          native-image -H:+ReportExceptionStackTraces -H:ConfigurationFileDirectories=/Users/andy/graalOutput --no-fallback -jar ./target/xmlJsonConverter-1.0-SNAPSHOT-jar-with-dependencies.jar ./target/macXmlToJsonConverter
          native-image -H:+ReportExceptionStackTraces -H:ConfigurationFileDirectories=/Users/andy/graalOutput --no-server --no-fallback -jar ./target/xmlJsonConverter-1.0-SNAPSHOT-jar-with-dependencies.jar ./target/macXmlToJsonConverter
#          native-image -O0 -H:+ReportExceptionStackTraces -H:ConfigurationFileDirectories=/Users/andy/graalOutput --initialize-at-build-time -jar ./target/xmlJsonConverter-1.0-SNAPSHOT-jar-with-dependencies.jar ./target/macXmlToJsonConverter
#          native-image -H:+ReportExceptionStackTraces -H:+ReportUnsupportedElementsAtRuntime --initialize-at-build-time --initialize-at-run-time=com.aimyourtechnology.xmljson.converter.ConverterApp --allow-incomplete-classpath -jar ./target/xmlJsonConverter-1.0-SNAPSHOT-jar-with-dependencies.jar ./target/macXmlToJsonConverter
          echo >&2 "Built native-image for: " $TARGET
        elif [[ "${TARGET}" == "docker" ]]
        then
          echo >&2 "Building native Image for linux on Docker..."
          docker build -f ./NativeImageDockerfile -t sns/graal-native-image-builder .
#          docker run --rm -v $PWD:/project sns/graal-native-image-builder:latest native-image -H:+ReportExceptionStackTraces -H:ConfigurationFileDirectories=/project/graalOutput --no-fallback -jar /project/target/xmlJsonConverter-1.0-SNAPSHOT-jar-with-dependencies.jar /project/target/linuxXmlToJsonConverter
          docker run --rm -v $PWD:/project sns/graal-native-image-builder:latest native-image -H:+ReportExceptionStackTraces -H:ConfigurationFileDirectories=/project/graalOutput --no-server --no-fallback -jar /project/target/xmlJsonConverter-1.0-SNAPSHOT-jar-with-dependencies.jar /project/target/linuxXmlToJsonConverter
          echo >&2 "Built native-image for: " $TARGET
        fi
        ;;
    i)
        echo >&2 "Building Docker Image to run native executable..."
        docker build -f ./Dockerfile -t sns/xml-json-converter .
        echo >&2 "Built image sns/xml-json-converter"
        ;;
    j)
        echo >&2 "Building Docker Image to run Jar on JVM..."
        docker build -f ./JvmDockerfile -t sns/xml-json-converter-jvm .
        echo >&2 "Built image sns/xml-json-converter-jvm"
        ;;

    *)
        usage
        exit 1
        ;;
  esac

done

