package com.cogneworx.intelsync.ingestion.model;

import java.util.Arrays;

public class IntelDocument {

    private String id;

    private String filename;

    private String contentType;

    private String detectedType;

    private String extractedText;

    private String[] metadata;

    public IntelDocument() {
    }

    public IntelDocument(String id, String filename, String contentType, String detectedType, String extractedText, String[] metadata) {
        this.id = id;
        this.filename = filename;
        this.contentType = contentType;
        this.detectedType = detectedType;
        this.extractedText = extractedText;
        this.metadata = metadata;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDetectedType() {
        return detectedType;
    }

    public void setDetectedType(String detectedType) {
        this.detectedType = detectedType;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String[] getMetadata() {
        return metadata;
    }

    public void setMetadata(String[] metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "IntelDocument{" +
                "id='" + id + '\'' +
                ", filename='" + filename + '\'' +
                ", contentType='" + contentType + '\'' +
                ", detectedType='" + detectedType + '\'' +
                ", extractedText='" + (extractedText != null ? extractedText.substring(0, Math.min(100, extractedText.length())) + "..." : null) + '\'' +
                ", metadata=" + Arrays.toString(metadata) +
                '}';
    }
}
