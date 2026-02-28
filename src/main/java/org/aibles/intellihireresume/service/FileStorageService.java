package org.aibles.intellihireresume.service;

import java.io.InputStream;

public interface FileStorageService {

    String upload(String bucket, String objectKey, InputStream inputStream, long size, String contentType);

    InputStream download(String bucket, String objectKey);

    void delete(String bucket, String objectKey);
}
