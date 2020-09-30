package com.wookie.demo.bucket;

public enum BucketName {
    PROFILE_IMAGE("wookie-test-upload-app");
    private final String bucketName;

    BucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
