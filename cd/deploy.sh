#!/usr/bin/env bash
set -x 
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  ./cd/version.sh

  cd dice-where
  cp ../cd/version.sh .
  ./version.sh
  cd ../dice-where-downloader-lib
  cp ../cd/version.sh .
  ./version.sh
  cd ../dice-where-downloader
  cp ../cd/version.sh .
  ./version.sh
  cd ..

  mvn deploy -P bintray --settings cd/mvnsettings.xml
  cd dice-where
  mvn deploy -P bintray --settings ../cd/mvnsettings.xml
  cd ../dice-where-downloader-lib
  mvn deploy -P bintray --settings ../cd/mvnsettings.xml
  cd ../dice-where-downloader
  mvn deploy -P bintray --settings ../cd/mvnsettings.xml
  cd ..

  ./cd/tag.sh
fi
