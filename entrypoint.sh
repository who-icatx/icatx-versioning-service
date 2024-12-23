#!/bin/bash

# Check if the SSH_PRIVATE_KEY environment variable is set
if [ -n "$SSH_PRIVATE_KEY" ]; then
  echo "Setting up SSH key..."

  # Create the SSH private key file
  echo "$SSH_PRIVATE_KEY" > /root/.ssh/id_rsa
  chmod 600 /root/.ssh/id_rsa

  # Add GitHub (or other host) to known_hosts to avoid host verification prompts
  ssh-keyscan -H github.com >> /root/.ssh/known_hosts
  chmod 644 /root/.ssh/known_hosts

  echo "SSH setup complete."
else
  echo "No SSH_PRIVATE_KEY provided. Skipping SSH setup."
fi

# Run the Java application
exec java -jar /app/icatx-versioning-service.jar