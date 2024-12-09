package edu.stanford.protege.versioning.services.python;

import edu.stanford.protege.webprotege.common.ProjectId;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;

@Service
public class PythonServiceImpl implements PythonService {

    private static final Logger logger = LoggerFactory.getLogger(PythonServiceImpl.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String dbName;


    @Override
    public void importMongoCollections(ProjectId projectId, Path inputDirectory) {
        Path scriptPath = null;
        try {
            scriptPath = extractScriptFromResources("import-backup-collections.py");


            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    scriptPath.toAbsolutePath().toString(),
                    mongoUri,
                    dbName,
                    inputDirectory.toString(),
                    projectId.id()
            );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String message = "Python script execution failed with exit code: " + exitCode;
                logger.error(message);
                throw new RuntimeException(message);
            }

            logger.info("Mongo collections imported successfully!");
        } catch (IOException | InterruptedException e) {
            String message = "Failed to import Mongo collections";
            logger.error(message);
            throw new RuntimeException(message, e);
        } finally {
            if (scriptPath != null) {
                try {
                    Files.deleteIfExists(scriptPath);
                    logger.info("Temporary script file deleted: " + scriptPath);
                } catch (IOException e) {
                    logger.error("Failed to delete temporary script file: " + scriptPath);
                }
            }
        }
    }

    private Path extractScriptFromResources(String resourceName) throws IOException {
        try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (resourceStream == null) {
                throw new FileNotFoundException("Resource not found: " + resourceName);
            }

            Path tempFile = Files.createTempFile("script", ".py");
            tempFile.toFile().deleteOnExit();

            try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
                resourceStream.transferTo(outputStream);
            }

            return tempFile;
        }
    }

}

