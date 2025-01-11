FROM eclipse-temurin:17-jre-focal AS final

LABEL MAINTAINER="protege.stanford.edu"

# Install required packages in one go:
# - git for git operations
# - zip for archiving
# - python3.10 and pip for running Python scripts
# - gnupg, wget for MongoDB key installation
# - mongodb-database-tools for mongodump, mongoexport, etc.
RUN apt-get update && \
    apt-get install -y --no-install-recommends git zip python3 python3-distutils python3-pip iputils-ping wget gnupg ssh && \
    wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | apt-key add - && \
    echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/debian bullseye/mongodb-org/6.0 main" > /etc/apt/sources.list.d/mongodb-org-6.0.list && \
    apt-get update && apt-get install -y --no-install-recommends mongodb-database-tools && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy requirements and install Python deps
COPY src/main/resources/requirements.txt /app/requirements.txt
RUN python3 -m pip install --upgrade pip && \
    pip3 install --no-cache-dir -r requirements.txt && \
    pip3 show pymongo || echo "pymongo installation failed"

# Copy Python scripts
COPY src/main/resources/import-backup-collections.py /app/
COPY src/main/resources/dump-project-collections.py /app/
COPY src/main/resources/dump-mongo.py /app/
COPY src/main/resources/commitBackup.sh /app/commitBackup.sh
COPY src/main/resources/gitCheckout.sh /app/gitCheckout.sh
RUN chmod +x /app/commitBackup.sh

# Set Git identity via environment variables
ENV GIT_AUTHOR_NAME="Your Name"
ENV GIT_AUTHOR_EMAIL="you@example.com"
ENV GIT_COMMITTER_NAME="Your Name"
ENV GIT_COMMITTER_EMAIL="you@example.com"

RUN git config --global user.name "$GIT_AUTHOR_NAME" && \
    git config --global user.email "$GIT_AUTHOR_EMAIL"

# Copy JAR file
ARG JAR_FILE
COPY target/${JAR_FILE} /app/icatx-versioning-service.jar

# Expose the application port
EXPOSE 8886

# Check installations
RUN python3 -c "import pymongo; print('pymongo installed successfully')" && \
    which mongodump || echo "mongodump not found"

COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Default entry point for the container
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]