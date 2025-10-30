package com.project.server;

import java.io.IOException;

public interface Calculator {
    
    void registerWithDirectory(String directoryHost, int directoryPort) throws IOException;
    
    void start();
    
    void shutdown();
    
}
