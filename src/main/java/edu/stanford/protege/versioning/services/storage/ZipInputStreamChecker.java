package edu.stanford.protege.versioning.services.storage;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ZipInputStreamChecker {

    private static final byte ZIP_FILE_MAGIC_NUMBER_BYTE_0 = 'P';
    private static final byte ZIP_FILE_MAGIC_NUMBER_BYTE_1 = 'K';

    /**
     * Checks if a file is a ZIP file by reading its magic number.
     *
     * @param file The path to the file to check.
     * @return True if the file is a ZIP file, otherwise false.
     * @throws IOException If an I/O error occurs.
     */
    public boolean isZipFile(Path file) throws IOException {
        try (InputStream inputStream = Files.newInputStream(file)) {
            return isZipInputStream(inputStream);
        }
    }

    /**
     * Checks if the provided input stream is a ZIP file stream by reading its magic number.
     *
     * @param inputStream The input stream to check.
     * @return True if the input stream is a ZIP file stream, otherwise false.
     * @throws IOException If an I/O error occurs.
     */
    public boolean isZipInputStream(InputStream inputStream) throws IOException {
        // Ensure the stream supports mark/reset
        if (!inputStream.markSupported()) {
            throw new IllegalArgumentException("Input stream must support mark/reset");
        }

        inputStream.mark(2); // Mark the first two bytes
        int byte0 = inputStream.read();
        int byte1 = inputStream.read();
        inputStream.reset(); // Reset to the marked position

        return byte0 == ZIP_FILE_MAGIC_NUMBER_BYTE_0 && byte1 == ZIP_FILE_MAGIC_NUMBER_BYTE_1;
    }
}

