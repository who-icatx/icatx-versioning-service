package edu.stanford.protege.versioning.services.python;

import edu.stanford.protege.versioning.dtos.MongoCollectionsTempFiles;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

@Service
public class PythonServiceImpl implements PythonService {

    private static final Logger logger = LoggerFactory.getLogger(PythonServiceImpl.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String dbName;

    @Value("${webprotege.directories.backup}")
    private String backupDirectory;

    private static final String IMPORT_BACKUP_COLLECTIONS_SCRIPT = "/app/import-backup-collections.py";
    private static final String MONGODB_DUMP_SCRIPT = "/app/dump-mongo.py";
    private static final String DUMP_PROJECT_COLLECTIONS_SCRIPT = "/app/dump-project-collections.py";

    @Override
    public void importMongoCollections(ProjectId projectId, Path inputDirectory) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    IMPORT_BACKUP_COLLECTIONS_SCRIPT,
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
        }
    }

    @Override
    public void createMongoDump() {
        try {

            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String archiveName = String.format("%s/mongoDump/%s", backupDirectory, currentDate);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    MONGODB_DUMP_SCRIPT,
                    mongoUri,
                    dbName,
                    archiveName
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

            logger.info("MongoDB dump created successfully at {}", archiveName);
        } catch (IOException | InterruptedException e) {
            String message = "Failed to create MongoDB dump";
            logger.error(message);
            throw new RuntimeException(message, e);
        }
    }

    @Override
    public MongoCollectionsTempFiles createCollectionsBackup(ProjectId projectId) {
        try {
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String outputPath = String.format("%s/%s/%s/%s", backupDirectory, projectId.id(), currentDate,"collections");

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    DUMP_PROJECT_COLLECTIONS_SCRIPT,
                    mongoUri,
                    dbName,
                    projectId.id(),
                    outputPath
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

            logger.info("Collections backup for project {} created successfully at {}", projectId.id(), outputPath);
            return new MongoCollectionsTempFiles(Paths.get(outputPath));

        } catch (IOException | InterruptedException e) {
            String message = "Failed to create collections backup";
            logger.error(message);
            throw new RuntimeException(message, e);
        }
    }

}

