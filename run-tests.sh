#!/bin/bash
#

set -o errexit

base="$PWD"
run-tests() {
  cd "$base/$1"
  grails test-app
}

run-tests .
run-tests test/app/plugin-not-installed
run-tests test/app/plugin-installed

exit 0
