#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  cd dice-where
  mvn deploy -P bintray --settings ../cd/mvnsettings.xml
  cd ..
  cd dice-where-downloader
  mvn deploy -P bintray --settings ../cd/mvnsettings.xml
fi
