#!/bin/bash +x

JAR="$(find tools/st/build/libs/ -type f -not -name '*-plain.jar')"

function st() {
  java -agentlib:native-image-agent=config-merge-dir=tools/st/src/main/resources/META-INF/native-image \
       -jar "${JAR}" \
       "$@"
}

printf "\n\n*** RUNNING ST THROUGH A NUMBER OF COMMANDS TO CAPTURE DETAILS FOR NATIVE IMAGE ***\n\n"

st --version
st generate --help
st extract --help
st generate --demo-code --name MOUSE.BIN --output mousedemo.po api/src/test/resources/mouse-bitmap.st
st generate --single --name MOUSE.BIN --output mouse.as api/src/test/resources/mouse-bitmap.st
st generate --name MOUSE.BIN --output mouse.bin api/src/test/resources/mouse-bitmap.st
st extract mouse.bin --stdout
