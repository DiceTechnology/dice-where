#!/usr/bin/env bash

set +x
echo $GPG_KEY | base64 --decode | gpg --batch --fast-import
set -x

./cd/version.sh

mvn deploy -P publish -DskipTests=true --settings cd/mvnsettings.xml
cd dice-where
mvn deploy -P publish -DskipTests=true --settings ../cd/mvnsettings.xml
cd ../dice-where-downloader-lib
mvn deploy -P publish -DskipTests=true --settings ../cd/mvnsettings.xml
cd ../dice-where-downloader
mvn deploy -P publish -DskipTests=true --settings ../cd/mvnsettings.xml
cd ..

./cd/tag.sh

