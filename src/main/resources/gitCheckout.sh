#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 3 ]]; then
    echo "Usage: $0 <repo_path> <ssh_url> <branch>"
    exit 1
fi

REPO_PATH="$1"
SSH_URL="$2"
BRANCH="$3"

# Check if the directory exists, and create it if it doesn't
if [[ ! -d "$REPO_PATH" ]]; then
    echo "Directory $REPO_PATH does not exist. Creating it."
    mkdir -p "$REPO_PATH"
fi

echo "Moving to directory: $REPO_PATH"
cd "$REPO_PATH"

# Clone the repository
if git clone "$SSH_URL" .; then
    echo "Clone successful."
else
    echo "Clone failed."
    exit 1
fi

cd "$REPO_PATH"

# Checkout the specified branch
if git checkout "$BRANCH"; then
    echo "Successfully checked out branch $BRANCH"
else
    echo "Branch checkout failed."
    exit 1
fi

# Pull the latest changes
if git pull; then
    echo "Successfully pulled latest changes."
else
    echo "Failed to pull changes."
    exit 1
fi