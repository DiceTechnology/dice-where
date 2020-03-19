#!/usr/bin/env bash
set -x 
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  cd dice-where
  ../cd/version.sh
  mvn deploy -P bintray --settings ../cd/mvnsettings.xml
  cd ../dice-where-downloader
  ../cd/version.sh
  mvn deploy -P bintray --settings ../cd/mvnsettings.xml
  cd ..
  ./cd/tag.sh
fi
