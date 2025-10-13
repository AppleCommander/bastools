# Usage: 'shell scripts/env.sh'

alias bt="java -jar ${PWD}/$(find tools/bt/build/libs/ -iname "*.jar" -not -iname "*plain*")"
alias st="java -jar ${PWD}/$(find tools/st/build/libs/ -iname "*.jar" -not -iname "*plain*")"
