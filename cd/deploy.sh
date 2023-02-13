#!/usr/bin/env bash

set +x
echo $GPG_KEY | base64 --decode | gpg --batch --fast-import
set -x

./cd/version.sh

mvn -q deploy -P publish -DskipTests=true --settings cd/mvnsettings.xml

./cd/tag.sh

