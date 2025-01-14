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

# Check if the directory is already a Git repository
if [ -d ".git" ]; then
    echo "Git repository already exists. Checking out branch: $BRANCH"
    git fetch origin
    if git checkout "$BRANCH"; then
        echo "Checked out branch $BRANCH successfully."
    else
        echo "Failed to checkout branch $BRANCH."
        exit 1
    fi
else
    # Clone the repository
    echo "Git repository not found. Cloning from $SSH_URL."
    if git clone "$SSH_URL" .; then
        if git checkout "$BRANCH"; then
            echo "Checked out branch $BRANCH successfully."
        else
            echo "Failed to checkout branch $BRANCH."
            exit 1
        fi
    else
        echo "Clone failed."
        exit 1
    fi
fi


# Pull the latest changes
if git pull; then
    echo "Successfully pulled latest changes."
else
    echo "Failed to pull changes."
    exit 1
fi