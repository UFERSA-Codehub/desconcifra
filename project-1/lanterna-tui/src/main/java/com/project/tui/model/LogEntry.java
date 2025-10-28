package com.project.tui.model;

import com.project.tui.layout.LogLevel;

public class LogEntry {
    private final LogLevel level;
    private final String message;
    private final long timestamp;

    public LogEntry(LogLevel level, String message) {
        this.level = level;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return level + ": " + message;
    }
}
