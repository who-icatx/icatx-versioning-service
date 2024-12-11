package edu.stanford.protege.versioning.services.storage;

import edu.stanford.protege.versioning.BackupFileProcessingException;
import edu.stanford.protege.versioning.config.MinioProperties;
import edu.stanford.protege.versioning.services.storage.dtos.*;
import edu.stanford.protege.webprotege.common.BlobLocation;
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
        cleanUpFiles(zipFile);
        return tempDirectory;
    }

    @Override
    public void cleanUpFiles(Path pathToFiles) throws IOException {
        FileUtils.forceDelete(pathToFiles.toFile());
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

    @Override
    public BlobLocation uploadFileToMinio(Path fileToUpload) throws IOException {

        try {
            return storeFile(fileToUpload);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new BackupFileProcessingException("Could not store file to minio", e);
        } finally {
            cleanUpFiles(fileToUpload);
        }
    }

    private BlobLocation storeFile(Path tempFile) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        var location = generateBlobLocation();
        // Create bucket if necessary
        createBucketIfNecessary(location);
        minioClient.uploadObject(UploadObjectArgs.builder()
                .filename(tempFile.toString())
                .bucket(location.bucket())
                .object(location.name())
                .contentType("application/octet-stream")
                .build());
        return location;
    }

    private void createBucketIfNecessary(BlobLocation location) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(location.bucket()).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(location.bucket()).build());
        }
    }

    private Path createTempFile() {
        try {
            return Files.createTempFile("webprotege-backup-file-", UUID.randomUUID().toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private BlobLocation generateBlobLocation() {
        return new BlobLocation(minioProperties.getVersioningBucketName(), generateObjectName());
    }

    private static String generateObjectName() {
        return "versioning-" + UUID.randomUUID();
    }
}
