package com.project.server;

import java.util.List;
import java.util.Map;

public interface Discovery {

    void discoverServices();

    ServiceInfo selectServer(String serviceName, String strategy);

    void registerService(String serviceName, String serverName, String host, int port);

    void unregisterService(String serviceName, String serverName);

    Map<String, List<ServiceInfo>> getServicesTable();

    void start();

    void shutdown();
    
}
