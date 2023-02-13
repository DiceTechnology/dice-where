#!/usr/bin/env bash
set -x 

echo $GPG_KEY | gpg --batch --fast-import

./cd/version.sh

mvn deploy -P publish -DskipTests=true --settings ../cd/mvnsettings.xml
cd dice-where
mvn deploy -P publish -DskipTests=true --settings ../cd/mvnsettings.xml
cd ../dice-where-downloader-lib
mvn deploy -P publish -DskipTests=true --settings ../cd/mvnsettings.xml
cd ../dice-where-downloader
mvn deploy -P publish -DskipTests=true --settings ../cd/mvnsettings.xml
cd ..

./cd/tag.sh

