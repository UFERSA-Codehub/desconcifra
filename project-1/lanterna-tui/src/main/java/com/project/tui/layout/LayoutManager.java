package com.project.tui.layout;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.tui.TUIConstants;
import com.project.tui.model.LogEntry;

public class LayoutManager {
    private String currentStatus = "";
    private LogEntry currentLog = null;

    public void drawLayout(Screen screen, String headerText) {
        TextGraphics graphics = screen.newTextGraphics();
        graphics.setBackgroundColor(null);
        TerminalSize size = screen.getTerminalSize();

        drawHeader(graphics, size, headerText);
        drawSideBorders(graphics, size);
        drawFooter(graphics, size);
    }

    private void drawHeader(TextGraphics graphics, TerminalSize size, String headerText) {
        int width = size.getColumns();

        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);

        StringBuilder topBorder = new StringBuilder();
        topBorder.append(TUIConstants.TOP_LEFT_CORNER);
        for (int i = 1; i < width - 1; i++) {
            topBorder.append(TUIConstants.H_LINE);
        }
        topBorder.append(TUIConstants.TOP_RIGHT_CORNER);
        graphics.putString(0, 0, topBorder.toString());

        graphics.putString(0, 1, String.valueOf(TUIConstants.V_LINE));
        graphics.putString(LayoutConfig.LEFT_MARGIN, 1, headerText != null ? headerText : "Application");
        graphics.putString(width - 1, 1, String.valueOf(TUIConstants.V_LINE));

        graphics.setForegroundColor(TUIConstants.MENU_SELECTED_BG_COLOR);
        StringBuilder separator = new StringBuilder();
        separator.append("├");
        for (int i = 1; i < width - 1; i++) {
            separator.append(TUIConstants.H_LINE);
        }
        separator.append("┤");
        graphics.putString(0, 2, separator.toString());
    }

    private void drawSideBorders(TextGraphics graphics, TerminalSize size) {
        int height = size.getRows();
        int leftX = 0;
        int rightX = size.getColumns() - 1;

        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);

        int startRow = LayoutConfig.HEADER_HEIGHT;
        int endRow = height - LayoutConfig.FOOTER_HEIGHT;

        for (int row = startRow; row < endRow; row++) {
            graphics.putString(leftX, row, "│");
            graphics.putString(rightX, row, "│");
        }
    }

    private void drawFooter(TextGraphics graphics, TerminalSize size) {
        int width = size.getColumns();
        int separatorRow = size.getRows() - 3;
        int footerRow = size.getRows() - 2;
        int bottomRow = size.getRows() - 1;

        int centerX = width / 2;

        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        StringBuilder separator = new StringBuilder();
        separator.append("├");
        for (int i = 1; i < width - 1; i++) {
            if (i == centerX) {
                separator.append("┬");
            } else {
                separator.append(TUIConstants.H_LINE);
            }
        }
        separator.append("┤");
        graphics.putString(0, separatorRow, separator.toString());

        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        StringBuilder blank = new StringBuilder();
        for (int i = 0; i < width; i++) {
            blank.append(" ");
        }
        graphics.putString(0, footerRow, blank.toString());

        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        graphics.putString(0, footerRow, "│");
        graphics.putString(width - 1, footerRow, "│");

        String leftSide = "Status: " + currentStatus;
        if (leftSide.length() > centerX - 3) {
            leftSide = leftSide.substring(0, centerX - 3);
        }

        String rightSide = "";
        if (currentLog != null) {
            rightSide = "Log: " + currentLog.toString();
        }
        if (rightSide.length() > centerX - 3) {
            rightSide = rightSide.substring(0, centerX - 3);
        }

        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        graphics.putString(LayoutConfig.LEFT_MARGIN, footerRow, leftSide);

        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        graphics.putString(centerX, footerRow, "│");

        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        graphics.putString(centerX + 2, footerRow, rightSide);

        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        StringBuilder bottomBorder = new StringBuilder();
        bottomBorder.append(TUIConstants.BOTTOM_LEFT_CORNER);
        for (int i = 1; i < width - 1; i++) {
            if (i == centerX) {
                bottomBorder.append("┴");
            } else {
                bottomBorder.append(TUIConstants.H_LINE);
            }
        }
        bottomBorder.append(TUIConstants.BOTTOM_RIGHT_CORNER);
        graphics.putString(0, bottomRow, bottomBorder.toString());
    }

    public ContentArea getContentArea(TerminalSize terminalSize) {
        int startX = LayoutConfig.LEFT_MARGIN;
        int startY = LayoutConfig.HEADER_HEIGHT + LayoutConfig.TOP_MARGIN;

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

    public void setStatus(String status) {
        this.currentStatus = status != null ? status : "";
    }

    public void setLog(LogEntry logEntry) {
        this.currentLog = logEntry;
    }

    public void addLog(LogLevel level, String message) {
        this.currentLog = new LogEntry(level, message);
    }

    public void clearLog() {
        this.currentLog = null;
    }

    public void clearStatus() {
        this.currentStatus = "";
    }

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
