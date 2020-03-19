#!/usr/bin/env bash
set -x 
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  cd dice-where
  cp ../cd/version.sh .
  ./version.sh
  mvn deploy -P bintray --settings ../cd/mvnsettings.xml
  cd ../dice-where-downloader
  cp ../cd/version.sh .
  ./version.sh
  mvn deploy -P bintray --settings ../cd/mvnsettings.xml
  cd ..
  ./cd/tag.sh
fi
