package com.project.tui.layout;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.tui.TUIConstants;
import com.project.tui.model.LogEntry;

/**
 * Manages the TUI layout structure with fixed header, content area, and footer.
 * Layout structure:
 * - Header: 3 rows (title/filename + separator + blank)
 * - Content: Variable height with 2-column and 2-row margins
 * - Footer: 1 row (status message | log entry)
 */
public class LayoutManager {
    private String currentStatus = "";
    private LogEntry currentLog = null;

    /**
     * Draws the complete layout structure including header and footer.
     * 
     * @param screen the screen to draw on
     * @param headerText the text to display in the header (app name or filename)
     */
    public void drawLayout(Screen screen, String headerText) {
        TextGraphics graphics = screen.newTextGraphics();
        graphics.setBackgroundColor(null); // Use terminal default
        TerminalSize size = screen.getTerminalSize();

        // Draw header
        drawHeader(graphics, size, headerText);

        // Draw footer
        drawFooter(graphics, size);
    }

    /**
     * Draws the header section (3 rows).
     * Row 0: Header text (app name or filename)
     * Row 1: Separator line
     * Row 2: Blank row
     */
    private void drawHeader(TextGraphics graphics, TerminalSize size, String headerText) {
        int width = size.getColumns();

        // Row 0: Header text (centered or left-aligned)
        graphics.setForegroundColor(TUIConstants.COLOR_HEADER);
        String displayText = headerText != null ? headerText : "Application";
        graphics.putString(LayoutConfig.LEFT_MARGIN, 0, displayText);

        // Row 1: Separator line
        graphics.setForegroundColor(TUIConstants.COLOR_BORDER);
        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < width; i++) {
            separator.append("─");
        }
        graphics.putString(0, 1, separator.toString());

        // Row 2: Blank row (no action needed)
    }

    /**
     * Draws the footer section (1 row at bottom).
     * Format: "Status: [message] | Log: [level]: [message]"
     */
    private void drawFooter(TextGraphics graphics, TerminalSize size) {
        int width = size.getColumns();
        int footerRow = size.getRows() - LayoutConfig.FOOTER_HEIGHT;

        // Clear the footer row first
        StringBuilder blank = new StringBuilder();
        for (int i = 0; i < width; i++) {
            blank.append(" ");
        }
        graphics.putString(0, footerRow, blank.toString());

        // Build footer content
        StringBuilder footer = new StringBuilder();
        footer.append("Status: ").append(currentStatus);

        if (currentLog != null) {
            footer.append(" | Log: ").append(currentLog.toString());
        }

        // Draw footer with appropriate color
        graphics.setForegroundColor(TUIConstants.COLOR_TEXT);
        graphics.putString(LayoutConfig.LEFT_MARGIN, footerRow, footer.toString());
    }

    /**
     * Calculates and returns the usable content area dimensions.
     * This area excludes the header, footer, and margins.
     * 
     * @param terminalSize the current terminal size
     * @return ContentArea object with position and size of usable space
     */
    public ContentArea getContentArea(TerminalSize terminalSize) {
        // Calculate content start position (after header + top margin)
        int startX = LayoutConfig.LEFT_MARGIN;
        int startY = LayoutConfig.HEADER_HEIGHT + LayoutConfig.TOP_MARGIN;

        // Calculate content dimensions
        int contentWidth = terminalSize.getColumns() 
                         - LayoutConfig.LEFT_MARGIN 
                         - LayoutConfig.RIGHT_MARGIN;
        
        int contentHeight = terminalSize.getRows() 
                          - LayoutConfig.HEADER_HEIGHT 
                          - LayoutConfig.FOOTER_HEIGHT 
                          - LayoutConfig.TOP_MARGIN 
                          - LayoutConfig.BOTTOM_MARGIN;

        return new ContentArea(
            new TerminalPosition(startX, startY),
            new TerminalSize(contentWidth, contentHeight)
        );
    }

    /**
     * Updates the status message in the footer.
     * 
     * @param status the status message to display
     */
    public void setStatus(String status) {
        this.currentStatus = status != null ? status : "";
    }

    /**
     * Updates the log entry in the footer.
     * 
     * @param logEntry the log entry to display
     */
    public void setLog(LogEntry logEntry) {
        this.currentLog = logEntry;
    }

    /**
     * Convenience method to add a log with level and message.
     * 
     * @param level the log level
     * @param message the log message
     */
    public void addLog(LogLevel level, String message) {
        this.currentLog = new LogEntry(level, message);
    }

    /**
     * Clears the current log entry.
     */
    public void clearLog() {
        this.currentLog = null;
    }

    /**
     * Clears the current status message.
     */
    public void clearStatus() {
        this.currentStatus = "";
    }

    /**
     * Represents the usable content area with position and size.
     */
    public static class ContentArea {
        private final TerminalPosition position;
        private final TerminalSize size;

        public ContentArea(TerminalPosition position, TerminalSize size) {
            this.position = position;
            this.size = size;
        }

        public TerminalPosition getPosition() {
            return position;
        }

        public TerminalSize getSize() {
            return size;
        }

        public int getStartX() {
            return position.getColumn();
        }

        public int getStartY() {
            return position.getRow();
        }

        public int getWidth() {
            return size.getColumns();
        }

        public int getHeight() {
            return size.getRows();
        }

        @Override
        public String toString() {
            return String.format("ContentArea[pos=%s, size=%s]", position, size);
        }
    }
}
