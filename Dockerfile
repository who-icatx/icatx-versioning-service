# Stage for Python dependencies and MongoDB tools
FROM python:3.10-slim as python-base

# Install MongoDB tools, Python dependencies, and necessary utilities
RUN apt-get update && \
    apt-get install -y iputils-ping wget gnupg && \
    wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | apt-key add - && \
    echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/debian bullseye/mongodb-org/6.0 main" | tee /etc/apt/sources.list.d/mongodb-org-6.0.list && \
    apt-get update && apt-get install -y mongodb-database-tools && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Set up working directory for Python
WORKDIR /app

# Copy Python dependencies
COPY src/main/resources/requirements.txt /app/requirements.txt

# Upgrade pip and install dependencies
RUN python -m pip install --upgrade pip && \
    pip install --no-cache-dir -r /app/requirements.txt && \
    pip show pymongo || echo "pymongo installation failed"

# Debug: Verify installation
RUN python -c "import pymongo; print('pymongo installed successfully')"


# Copy Python script
COPY src/main/resources/import-backup-collections.py /app/import-backup-collections.py
COPY src/main/resources/dump-project-collections.py /app/dump-project-collections.py
COPY src/main/resources/dump-mongo.py /app/dump-mongo.py

# Final combined image
FROM python:3.10-slim

LABEL MAINTAINER="protege.stanford.edu"

# Install Java
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get install -y git && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy MongoDB tools from the python-base stage
COPY --from=python-base /usr/bin/mongo* /usr/bin/

# Set working directory for the combined container
WORKDIR /app

# Expose the application port
EXPOSE 8886

# Copy application JAR from the Java build stage
ARG JAR_FILE
COPY target/${JAR_FILE} /app/icatx-versioning-service.jar

# Copy Python setup and scripts
COPY --from=python-base /app /app

# Install Python dependencies in the final stage
RUN pip install --no-cache-dir -r /app/requirements.txt && \
    pip show pymongo || echo "pymongo installation failed"

# Debugging: Check pymongo and mongodump availability
RUN python -c "import pymongo; print('pymongo installed successfully')" && \
    which mongodump || echo "mongodump not found"

# Default entry point for the container
ENTRYPOINT ["java", "-jar", "/app/icatx-versioning-service.jar"]
