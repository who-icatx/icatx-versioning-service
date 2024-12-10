package edu.stanford.protege.versioning.services.storage;

import edu.stanford.protege.versioning.BackupFileProcessingException;
import edu.stanford.protege.versioning.config.MinioProperties;
import edu.stanford.protege.versioning.services.storage.dtos.*;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

@Service
public class StorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public StorageServiceImpl(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }


    @Override
    public Path downloadFile(DocumentId documentId) {
        var destinationPath = createTempFile();
        try {
            minioClient.downloadObject(DownloadObjectArgs.builder()
                    .filename(destinationPath.toString())
                    .bucket(minioProperties.getUploadsBucketName())
                    .object(documentId.getDocumentId())
                    .build());

            return destinationPath;
        } catch (ErrorResponseException | XmlParserException | ServerException | NoSuchAlgorithmException |
                 IOException | InvalidResponseException | InvalidKeyException | InternalException |
                 InsufficientDataException e) {
            throw new BackupFileProcessingException(e);
        }
    }

    @Override
    public Path extractBackupFiles(Path zipFile) throws IOException {
        var tempDirectory = Files.createTempDirectory("webprotege-extracted-backup-files");
        ZipFileExtractor extractor = new ZipFileExtractor();
        extractor.extractFileToDirectory(zipFile, tempDirectory);
        return tempDirectory;
    }

    @Override
    public void cleanUpFiles(Path pathToFiles) throws IOException {
        FileUtils.deleteDirectory(pathToFiles.toFile());
    }

    @Override
    public ProjectBackupFiles getProjectBackupFilesFromPath(Path backupFilesDirectory) {
        try (var files = Files.walk(backupFilesDirectory, Integer.MAX_VALUE)) {
            File owlBinaryFile = null;
            List<File> mongoCollections = new ArrayList<>();

            for (Path file : files.toList()) {
                File currentFile = file.toFile();

                if (currentFile.getName().endsWith(".binary")) {
                    if (owlBinaryFile != null) {
                        throw new BackupFileProcessingException("Only one owl binary file is allowed");
                    }
                    owlBinaryFile = currentFile;
                } else if (currentFile.getName().endsWith(".json")) {
                    mongoCollections.add(currentFile);
                }
            }

            if (owlBinaryFile == null) {
                throw new BackupFileProcessingException("Owl binary file could not be found");
            }

            return new ProjectBackupFiles(owlBinaryFile, mongoCollections);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path createTempFile() {
        try {
            return Files.createTempFile("webprotege-backup-file", null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
