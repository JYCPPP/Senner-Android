package com.example.senner.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileScanner {
    public List<File> getFiles(String path) {
        List<File> fileList = new ArrayList<>();
        File directory = new File(path);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                Collections.addAll(fileList, files);
            }
        }
        return fileList;
    }
}