# Git Repository for Entity JSON Files and Versioning

This repository serves as the central location for storing and managing entity JSON files generated from the iCAT-X Foundation project. The repository supports versioning, backup, and synchronization with iCAT-X through automated processes and scripts.

---

## Initialization of the Git Repository with Entity JSON Files

A **Versioning Microservice** has been implemented to generate and store all entity JSON files in the repository. The service uses the `init-entity-files` API command, which:

1. **Generates JSON Files**:
   - Fetches all entities from the iCAT-X Foundation project.
   - Saves the API Gateway entity response (using the IRI) to disk in a structured folder hierarchy based on the last 3 digits of the `publicId` of each entity.

2. **Git Project Initialization**:
   - Initializes a Git repository specified by the `WEBPROTEGE_GIT_SSHURL` environment variable.
   - Commits and pushes all generated JSON files to the Git repository.

### Endpoint for Initialization
To initiate the process, perform a POST request to the following endpoint: `POST /{project-id}/init-entity-files`


---

## Backup Process

The backup process ensures synchronization between the Git repository and the iCAT-X project state. A script automates the following:

1. **Fetching Changes**:
   - Identifies entities with changes since the last backup.

2. **Outputs**:
   - **Git Commit**: Includes JSON files corresponding to changed entities. The commit message lists the entity IRIs included in the commit.
   - **Backup Zip File**:
     - Contains the OWL Binary file (ontological content) and a MongoDB dump of iCAT-X project documents.
     - The zip file is stored under:
       ```
       icatx-versioning/versions/{projectId}/{date}/{date-time}.zip
       ```

### Endpoint for Backups
To initiate a backup, use the following endpoint: `POST /{project-id}/backup`


### Notes on Backups
- Backups are only created if there are entities with changes.
- Synchronization is achieved between the Git commit and the backup zip file.
- Regular validation of backups is recommended.

---

## Starting a New Branch Project

To create a new branch project:
1. **Manually Create a Branch**: 
   - WHO creates a new branch in GitHub from an existing branch or commit containing the JSON files.
2. **Initialize the iCAT-X Project**:
   - Use the **Create from Existing Sources** feature in iCAT-X.
   - Select **Backup files** as the project source and provide a backup zip file from:
     ```
     /icatx-versioning/versions/{projectId}
     ```
   - This initializes a new project, copying the OWL Binary file and updating MongoDB collections.

### Branch Naming
The UI will prompt for the branch name, which corresponds to the Git branch where changes will be committed.

---

## Merging Branch Changes into the Main Development Branch

When work on a branch is completed:
1. **Merge and Resolve Conflicts**:
   - Merge the branch into the main development branch using Git commands outside of iCAT-X.
   - WHO manages merge requests and conflict resolution.
2. **Update the Main Development Branch**:
   - Use iCAT-X Web API `update` and `addEntity` calls to sync the main development branch with the merged changes.

---

## Key Considerations

- **Synchronization**:
  - Ensure synchronization between Git commits and zip file backups to maintain workflow integrity.
  - The application should be in a read-only state during backups.

- **Environment Variable**:
  - The `WEBPROTEGE_GIT_SSHURL` environment variable must be correctly configured for Git project initialization. 
  - If not set, JSON files will default to:
    ```
    https://github.com/who-icatx/whofic-ontology-files
    ```

- **Backup Validation**:
  - Regular validation ensures changes are effectively captured and stored.

- **Recovery**:
  - The entire Git repository can be recreated using one of the zip files if necessary.

---

This repository ensures a robust workflow for managing entity JSON files with versioning, backups, and synchronization with the iCAT-X project.
