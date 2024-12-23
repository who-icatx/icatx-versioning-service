package edu.stanford.protege.versioning;

public class BackupFileProcessingException extends RuntimeException {

    public BackupFileProcessingException(Throwable cause) {
        super(cause);
    }

    public BackupFileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BackupFileProcessingException(String message) {
        super(message);
    }
}
