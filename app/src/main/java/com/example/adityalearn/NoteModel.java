package com.example.adityalearn;

public class NoteModel {
    private String fileName;
    private String fileUrl;

    public NoteModel(String fileName, String fileUrl) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public String getFileName() { return fileName; }
    public String getFileUrl() { return fileUrl; }
}
