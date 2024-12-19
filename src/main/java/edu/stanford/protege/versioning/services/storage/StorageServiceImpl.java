package edu.stanford.protege.versioning.services.storage;

import edu.stanford.protege.versioning.BackupFileProcessingException;
import edu.stanford.protege.versioning.config.MinioProperties;
import edu.stanford.protege.versioning.dtos.*;
import edu.stanford.protege.webprotege.common.*;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.*;

@Service
public class StorageServiceImpl implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);

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
                    .object(documentId.documentId())
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
        if (Files.exists(pathToFiles)) {
            FileUtils.forceDelete(pathToFiles.toFile());
        }
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

    public Path combineFilesIntoArchive(Path outputPath, RegularTempFile... regularTempFiles) {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
            RegularTempFile firstFile = regularTempFiles[0];
            for(int i = 1; i<regularTempFiles.length; i++){
                if(firstFile.baseDirectory().equals(regularTempFiles[i].baseDirectory())){
                    continue;
                }
                Path fileSource = regularTempFiles[i].baseDirectory();
                Path fileDestination = firstFile.baseDirectory().resolve(fileSource.getFileName());
                Files.copy(fileSource, fileDestination, StandardCopyOption.REPLACE_EXISTING);
                regularTempFiles[i].clearTempFiles();
            }

            Path finalArchivePath = Paths.get(outputPath.toString(), String.format("%s-backup.zip", now.format(formatter)));
            zipDirectory(firstFile.baseDirectory(), finalArchivePath);
            logger.info("Final archive created at: {}", finalArchivePath);
            if(!firstFile.baseDirectory().equals(outputPath)){
                firstFile.clearTempFiles();
            }
            return finalArchivePath;

        } catch (IOException e) {
            throw new RuntimeException("Error combining files into archive", e);
        }
    }

    public void zipDirectory(Path sourceDir, Path archivePath) throws IOException {
        try (
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archivePath.toFile()));
                Stream<Path> filesInDirectory = Files.walk(sourceDir)
        ) {
            filesInDirectory.forEach(path -> {
                try {
                    if (!Files.isDirectory(path)) {
                        ZipEntry entry = new ZipEntry(sourceDir.relativize(path).toString());
                        zos.putNextEntry(entry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    @Override
    public RegularTempFile createFile(Path directory, String fileName) throws IOException {
        return RegularTempFile.create(Files.createFile(directory.resolve(fileName)));
    }
}
