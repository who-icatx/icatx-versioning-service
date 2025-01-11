#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 3 ]]; then
    echo "Usage: $0 <repo_path> <archive_path> <commit_message>"
    exit 1
fi

REPO_PATH="$1"
ARCHIVE_FILE="$2"
COMMIT_MESSAGE="$3"
METADATA_FILE="metadata.txt"

# Commit and push changes in the Git repository
echo "Moving to directory: $REPO_PATH"
cd "$REPO_PATH"
git add -A
if git commit -m "$COMMIT_MESSAGE"; then
    echo "Commit successful."
else
    echo "No changes to commit."
fi

current_branch=$(git branch --show-current)
git push origin "$current_branch"

# Retrieve the last commit ID
last_commit_id=$(git rev-parse HEAD)

# Return to the previous directory
cd -

# Create metadata file temporarily
echo "Branch: $current_branch" > "$METADATA_FILE"
echo "Commit ID: $last_commit_id" >> "$METADATA_FILE"

# Check if the archive exists
if [[ ! -f $ARCHIVE_FILE ]]; then
    echo "Archive $ARCHIVE_FILE does not exist. Creating a new archive."
    zip "$ARCHIVE_FILE" "$METADATA_FILE"
else
    # Update the existing archive with the new metadata file
    zip -u "$ARCHIVE_FILE" "$METADATA_FILE"
fi

# Remove the temporary metadata file so it's only in the archive
rm "$METADATA_FILE"

echo "Metadata updated and added to $ARCHIVE_FILE successfully."
