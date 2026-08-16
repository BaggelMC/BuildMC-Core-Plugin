#!/bin/bash
# deploy-javadoc.sh — invoked by Maven's exec-maven-plugin during `mvn deploy`
set -euo pipefail

REMOTE_HOST="$1"
REMOTE_BASE="$2"
ARTIFACT_ID="$3"
VERSION="$4"
REMOTE_USER="$5"
SSH_IDENTITY_PATH="$6"

LOCAL_JAVADOC_DIR="target/apidocs"
REMOTE_VERSION_DIR="$REMOTE_BASE/$ARTIFACT_ID/$VERSION"
REMOTE_LATEST_LINK="$REMOTE_BASE/$ARTIFACT_ID/latest"

SSH_CMD=(ssh -i "$SSH_IDENTITY_PATH" -o IdentitiesOnly=yes "$REMOTE_USER@$REMOTE_HOST")

echo "Deploying $ARTIFACT_ID $VERSION to $REMOTE_HOST:$REMOTE_VERSION_DIR ..."
"${SSH_CMD[@]}" "mkdir -p '$REMOTE_VERSION_DIR'"
rsync -avz --delete -e "ssh -i $SSH_IDENTITY_PATH -o IdentitiesOnly=yes" \
    "$LOCAL_JAVADOC_DIR"/ "$REMOTE_USER@$REMOTE_HOST:$REMOTE_VERSION_DIR"/

if [[ "$VERSION" != *SNAPSHOT* ]]; then
    "${SSH_CMD[@]}" "ln -sfn '$REMOTE_VERSION_DIR' '$REMOTE_LATEST_LINK'"
    echo "Updated 'latest' -> $VERSION"
fi

echo "Regenerating index for $ARTIFACT_ID..."
"${SSH_CMD[@]}" "/home/docs/generate-javadoc-index.sh '$REMOTE_BASE/$ARTIFACT_ID'"

echo "Done: https://$REMOTE_HOST/$ARTIFACT_ID/$VERSION/index.html"