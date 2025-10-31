package com.project.file;

import java.util.ArrayList;
import java.util.List;

public class FileStorage {
    private List<String> files;
    private int nodeId;

    public FileStorage(int nodeId) {
        this.nodeId = nodeId;
        this.files = new ArrayList<>();
        initializeFiles();
    }

    private void initializeFiles() {
        int startIndex = (nodeId * 10) + 1;
        int endIndex = startIndex + 9;
        
        for (int i = startIndex; i <= endIndex; i++) {
            files.add("arquivo" + i);
        }
    }

    public boolean hasFile(String fileName) {
        return files.contains(fileName);
    }

    public List<String> getFiles() {
        return new ArrayList<>(files);
    }

    public int getNodeId() {
        return nodeId;
    }
}
