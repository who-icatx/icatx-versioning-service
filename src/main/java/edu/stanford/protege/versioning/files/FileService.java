package edu.stanford.protege.versioning.files;


import com.fasterxml.jackson.databind.*;
import edu.stanford.protege.versioning.ApplicationException;
import edu.stanford.protege.versioning.entity.EntityChildren;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class FileService {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    private final String versioningLocation;
    private final ObjectMapper objectMapper;


    public FileService(@Value("${webprotege.versioning.jsonFileLocation}") String versioningLocation, ObjectMapper objectMapper) {
        this.versioningLocation = versioningLocation;
        this.objectMapper = objectMapper;
    }


    public File getEntityFile(IRI entityIri, ProjectId projectId) {
        try {

            File directory = new File(versioningLocation + projectId.id() + "/" + getLastThreeCharacters(entityIri.toString()));
            return new File(directory, extractEntityId(entityIri.toString()) + ".json");

        } catch (Exception e) {
            LOGGER.error("Error finding file " + e);
        }
        return null;
    }

    public void writeEntities(IRI entityIri, JsonNode dto, ProjectId projectId) {

        String lastThreeChars = getLastThreeCharacters(entityIri.toString());

        File directory = new File(versioningLocation + projectId.id() + "/" + lastThreeChars);

        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + directory.getAbsolutePath());
            } else {
                throw new RuntimeException("Error creating directory" + directory.getAbsolutePath());
            }
        }

        File jsonFile = new File(directory, extractEntityId(entityIri.toString()) + ".json");
        writeObjectToJsonFile(jsonFile, dto);
    }

    public void writeEntityChildrenFile(EntityChildren entityChildren) {
        String lastThreeChars = getLastThreeCharacters(entityChildren.entityUri());

        File directory = new File(versioningLocation + entityChildren.projectId() + "/" + lastThreeChars);

        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + directory.getAbsolutePath());
            } else {
                throw new RuntimeException("Error creating directory" + directory.getAbsolutePath());
            }
        }

        File jsonFile = new File(directory, "children_" + extractEntityId(entityChildren.entityUri()) + ".json");
        writeObjectToJsonFile(jsonFile, objectMapper.convertValue(entityChildren, JsonNode.class));
    }

    public void removeFileIfExists(ProjectId projectId, IRI entityIri) {
        String lastThreeChars = getLastThreeCharacters(entityIri.toString());

        File directory = new File(versioningLocation + projectId+ "/" + lastThreeChars);
        File jsonFile = new File(directory, "children_" + extractEntityId(entityIri.toString()) + ".json");
        LOGGER.info("Trying to delete the children file on path " + jsonFile.getAbsolutePath());
        if(jsonFile.exists()) {
            var deleteResult = jsonFile.delete();
            LOGGER.info("Delete result on deleting the file {} is {}", jsonFile.getAbsolutePath(), deleteResult);
        }
    }

    private String extractEntityId(String iri) {
        if (iri != null && iri.contains("/")) {
            String[] parts = iri.split("/");
            return parts[parts.length - 1];
        }
        throw new IllegalArgumentException("Invalid IRI: " + iri);
    }

    private String getLastThreeCharacters(String iri) {
        if (iri != null && iri.length() >= 3) {
            return iri.substring(iri.length() - 3);
        }
        throw new IllegalArgumentException("Invalid IRI: " + iri);
    }

    private void writeObjectToJsonFile(File file, JsonNode obj) {
        try {
            objectMapper.writeValue(file, obj);
            System.out.println("Written JSON file: " + file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error writing JSON to file: " + file.getAbsolutePath(), e);
            throw new RuntimeException("Error writing JSON to file: " + file.getAbsolutePath());
        }
    }

    public void createSmallFilesDirectory(ProjectId projectId) {
        File directory = new File(versioningLocation + projectId.id());

        if (!directory.exists()) {
            LOGGER.error("Error saving directory of small files");
           // throw new ApplicationException("Error creating directory" + directory.getAbsolutePath());
        }
    }
}
