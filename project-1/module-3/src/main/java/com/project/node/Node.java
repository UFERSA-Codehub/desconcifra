package com.project.node;

public interface Node {
    void start();
    void stop();
    void searchFile(String fileName);
    int getNodeId();
    int getPort();
}
