package edu.stanford.protege.versioning.files;


import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.versioning.entity.OWLEntityDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class FileService {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    private final String versioningLocation;
    private final ObjectMapper objectMapper;


    public FileService(@Value("${webprotege.versioning.location}") String versioningLocation, ObjectMapper objectMapper) {
        this.versioningLocation = versioningLocation;
        this.objectMapper = objectMapper;
    }

    public void writeEntities(OWLEntityDto dto) {

        // Extract the last 3 characters of the identifier
        String lastThreeChars = getLastThreeCharacters(dto.entityIRI());

        // Define the directory path
        File directory = new File(versioningLocation + lastThreeChars);

        // Create the directory if it does not exist
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + directory.getAbsolutePath());
            } else {
                throw new RuntimeException("Error creating directory" + directory.getAbsolutePath());
            }
        }

        // Write the object as JSON inside the directory
        File jsonFile = new File(directory, extractEntityId(dto.entityIRI()) + ".json");
        writeObjectToJsonFile(jsonFile, dto);
    }

    private String extractEntityId(String iri) {
        if (iri != null && iri.contains("/")) {
            // Split the string by '/' and return the last part
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

    private void writeObjectToJsonFile(File file, OWLEntityDto obj) {
        try {
            objectMapper.writeValue(file, obj);
            System.out.println("Written JSON file: " + file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error writing JSON to file: " + file.getAbsolutePath(), e);
            throw new RuntimeException("Error writing JSON to file: " + file.getAbsolutePath());
        }
    }
}
