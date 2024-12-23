# icatx-versioning-service


## Description
This microservice handles the GIT versioning of the entities, generates the initial ontology content and handles the creation of nightly backups.


## Features

### Create the initial repository

Creating the initial files associated to the "main" branch or any new repository will be done through an API call on ````/versioning-commands/{projectId}/initial-files````. This call will scan for all the classes 
in the ontology and for each will save the class info as JSON format inside a volume. The binding between the volume and 