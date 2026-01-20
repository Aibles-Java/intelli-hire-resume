package org.aibles.intellihireresume.entity.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum FileType {
    PDF("application/pdf", "pdf", 5 * 1024 * 1024), // 5MB
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx", 5 * 1024 * 1024); // 5MB
    
    private final String mimeType;
    private final String extension;
    private final long maxSizeBytes;
    
    FileType(String mimeType, String extension, long maxSizeBytes) {
        this.mimeType = mimeType;
        this.extension = extension;
        this.maxSizeBytes = maxSizeBytes;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }
    
    public static FileType fromExtension(String extension) {
        if (extension == null) {
            return null;
        }
        
        String normalizedExt = extension.toLowerCase().replace(".", "");
        for (FileType type : values()) {
            if (type.extension.equals(normalizedExt)) {
                return type;
            }
        }
        return null;
    }
    
    public static FileType fromMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        
        for (FileType type : values()) {
            if (type.mimeType.equals(mimeType)) {
                return type;
            }
        }
        return null;
    }
    
    public static Set<String> getAllowedExtensions() {
        return Arrays.stream(values())
                .map(FileType::getExtension)
                .collect(Collectors.toSet());
    }
    
    public static Set<String> getAllowedMimeTypes() {
        return Arrays.stream(values())
                .map(FileType::getMimeType)
                .collect(Collectors.toSet());
    }
    
    public boolean isValidSize(long fileSizeBytes) {
        return fileSizeBytes > 0 && fileSizeBytes <= maxSizeBytes;
    }
}