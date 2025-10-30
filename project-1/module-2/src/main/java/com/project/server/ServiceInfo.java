package com.project.server;

public class ServiceInfo {
    private String serviceName;
    private String serverName;
    private String host;
    private int port;

    public ServiceInfo(String serviceName, String serverName, String host, int port) {
        this.serviceName = serviceName;
        this.serverName = serverName;
        this.host = host;
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServerName() {
        return serverName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return host + ":" + port;
    }

    @Override
    public String toString() {
        return String.format("ServiceInfo{service='%s', server='%s', address='%s:%d'}", 
            serviceName, serverName, host, port);
    }
}
