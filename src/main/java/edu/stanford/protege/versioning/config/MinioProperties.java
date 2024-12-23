package edu.stanford.protege.versioning.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "webprotege.minio")
@Component
public class MinioProperties {

    private String accessKey;

    private String secretKey;

    private String endPoint;

    private String uploadsBucketName;

    private String versioningBucketName;

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public void setUploadsBucketName(String uploadsBucketName) {
        this.uploadsBucketName = uploadsBucketName;
    }

    public void setVersioningBucketName(String versioningBucketName) {
        this.versioningBucketName = versioningBucketName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public String getUploadsBucketName() {
        return uploadsBucketName;
    }

    public String getVersioningBucketName() {
        return versioningBucketName;
    }
}
