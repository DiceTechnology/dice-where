#!/bin/bash
# Script to tag the GIT repository with a specific version taken from the POM file

set -x

function slack {
  local PAYLOAD="payload={\"channel\": \"dice-opensource\", \"text\":\" $1 \", \"username\": \"Travis\", \"icon_url\": \"https://fst.slack-edge.com/66f9/img/services/travis_36.png\"}"
  echo Sending message to slack
  curl -o /dev/null -s -w "%{http_code}\n" -X POST --data-urlencode "$PAYLOAD" $encrypted_SLACK_URL
}

# Get VERSION from top level POM
VERSION_POM=$( mvn help:evaluate -Dexpression=project.version | grep -v '\[.*' | tail -n1 )

# Get ARTIFACT_ID from top level POM
ARTIFACT_ID_POM=$( mvn help:evaluate -Dexpression=project.artifactId | grep -v '\[.*' | tail -n1 )

# Setup Git Configuration
git config --global user.email "build@travis-ci.com"
git config --global user.name "Travis CI"
GITHUB_REPO_URL_TOKEN="https://${TRAVIS_DICEOSS_GITHUB_TOKEN}:x-oauth-basic@github.com/${TRAVIS_REPO_SLUG}.git"

git remote set-url origin "${GITHUB_REPO_URL_TOKEN}" && \
git tag "${VERSION_POM}" -m "[Travis] Released ${VERSION_POM}" 2>/dev/null && \
git push origin --tags 2>/dev/null && \
echo "Tagged $ARTIFACT_ID_POM with version $VERSION_POM" && \
slack "Tagged $ARTIFACT_ID_POM with version $VERSION_POM"