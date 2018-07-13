#!/bin/bash

if [ ! -f ./setup.sh ]
then
  echo "This script depends on a 'setup.sh' script in the local directory."
  echo "'setup.sh' should define an alias for 'bt' and 'ac'."
  echo "Alternatively, you may craft a shell script with those names and place them on"
  echo "the PATH."
  exit 1
fi

shopt -s expand_aliases
source ./setup.sh

cp template.po ships.po
bt --stdout --applesingle --optimize ships.bas | ac -as ships.po startup
