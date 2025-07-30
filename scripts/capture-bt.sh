#!/bin/bash +x

JAR="$(find tools/bt/build/libs/ -type f -not -name '*-plain.jar')"

function bt() {
  java -agentlib:native-image-agent=config-merge-dir=tools/bt/src/main/resources/META-INF/native-image \
       -jar "${JAR}" \
       "$@"
}

printf "\n\n*** RUNNING BT THROUGH A NUMBER OF COMMANDS TO CAPTURE DETAILS FOR NATIVE IMAGE ***\n\n"

bt --version
bt --optimize tools/bt/src/test/resources/alien-drizzle.bas --output=alien-drizzle.out
bt --applesingle tools/bt/src/test/resources/route6502.bas --output=route6502.as
bt --wrapper tools/bt/src/test/resources/test.bas --hex
bt tools/bt/src/test/resources/embed-example.bas --copy
bt tools/bt/src/test/resources/mouse.bas --copy
