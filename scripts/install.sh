set -e -u

function mvn_install() {
  mvn clean install \
    --settings="$(dirname $0)/settings.xml" "$@"
}

if [ "$TRAVIS_REPO_SLUG" == "cmendesce/kube-rainbow" ] && \
   [ "$TRAVIS_JDK_VERSION" == "openjdk11" ] && \
   [ "$TRAVIS_PULL_REQUEST" == "false" ] && \
   [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "Building..."

  mvn_install

  echo "Build successfully"
fi