package com.filesmanager.domain.ports;

import java.nio.file.Path;
import java.util.List;

public interface FileStoragePort {
    void storeFile(String filename, byte[] content);
    List<Path> listFiles();
    void deleteFile(String filename);
    void deleteDirectory(String directoryPath);
    byte[] readFile(String filename);
    void createDirectory(String directoryPath);
} 