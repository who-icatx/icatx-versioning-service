#!/bin/bash

# Check if the required parameters are provided
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
  echo "Usage: $0 <ssh-url> <path> <branch>"
  exit 1
fi

SSH_URL="$1"
TARGET_PATH="$2"
BRANCH_NAME="$3"

# Validate SSH URL format
if [[ ! "$SSH_URL" =~ ^git@.*:.*\.git$ ]]; then
  echo "Error: Invalid SSH URL format. Expected format is git@<host>:<repo>.git"
  exit 1
fi

# Check if the directory exists, if not, create it
if [ ! -d "$TARGET_PATH" ]; then
  echo "Directory '$TARGET_PATH' does not exist. Creating it..."
  mkdir -p "$TARGET_PATH" || { echo "Error: Failed to create directory '$TARGET_PATH'"; exit 1; }
fi

# Change to the specified directory
cd "$TARGET_PATH" || { echo "Error: Failed to change to directory '$TARGET_PATH'"; exit 1; }

# Check if the current directory is already a Git repository
if [ -d .git ]; then
  echo "This directory is already a Git repository."
  echo "Setting remote origin to $SSH_URL"
  git remote add origin "$SSH_URL" 2>/dev/null || git remote set-url origin "$SSH_URL"

  # Switch to the specified branch
  echo "Switching to branch '$BRANCH_NAME'"
  git checkout -b "$BRANCH_NAME" 2>/dev/null || git checkout "$BRANCH_NAME"
else
  echo "Initializing a new Git repository."
  git init

  # Create and switch to the specified branch
  echo "Creating and switching to branch '$BRANCH_NAME'"
  git checkout -b "$BRANCH_NAME"

  echo "Setting remote origin to $SSH_URL"
  git remote add origin "$SSH_URL"
fi

echo "Git repository is ready in '$TARGET_PATH' with remote origin set to $SSH_URL on branch '$BRANCH_NAME'."