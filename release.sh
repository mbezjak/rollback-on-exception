#!/bin/bash

set -o errexit

version="$1"
[[ -z "$version" ]] && {
  echo "Usage $(basename $0) VERSION" >&2
  exit 1
}

cat <<EOF
(1) make sure Changelog.md mentions version $version
(2) update install section version in README.md
(3) update version in plugin descriptor

Press enter for plugin release, ctrl + c to cancel
EOF
read

git tag "$version"
git push
git push --tags

grails release-plugin --zipOnly --message="RELEASE $version"

exit 0
