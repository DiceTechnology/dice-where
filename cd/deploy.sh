#!/usr/bin/env bash
set -x 
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  gpg --batch --fast-import gpg.asc

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

  mvn deploy -P publish -DskipTests=true --settings cd/mvnsettings.xml
  cd dice-where
  mvn deploy -P publish -DskipTests=true --settings cd/mvnsettings.xml
  cd ../dice-where-downloader-lib
  mvn deploy -P publish -DskipTests=true --settings cd/mvnsettings.xml
  cd ../dice-where-downloader
  mvn deploy -P publish -DskipTests=true --settings cd/mvnsettings.xml
  cd ..

  ./cd/tag.sh
fi
