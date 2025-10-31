package com.project.tui;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.project.tui.layout.LayoutManager;
import com.project.tui.layout.LogLevel;

import java.io.IOException;

public class TUIManager {

    private Terminal terminal;
    private Screen screen;
    private TextGraphics textGraphics;
    private LayoutManager layoutManager;
    private boolean isInitialized = false;

    public void initialize() throws IOException {
        if (isInitialized){
            return;
        }

        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        terminal = factory.createTerminal();
        terminal.setCursorVisible(false);
        //CHECK FOR PRIVATE MODE
        screen = new TerminalScreen(terminal);
        screen.startScreen();
        screen.setCursorPosition(null); // Hide cursor

        textGraphics = screen.newTextGraphics();

        textGraphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        textGraphics.setBackgroundColor(TUIConstants.DEFAULT_BG_COLOR);

        layoutManager = new LayoutManager();

        isInitialized = true;
    }

    public void shutdown() throws IOException {
        if (!isInitialized) {
            return;
        }

        try {
        screen.stopScreen();
        //terminal.exitPrivateMode();
        terminal.close();
        
        } catch (IOException e) {
            //throw new IOException("Error while shutting down TUIManager", e);
            System.err.println("Error while shutting down TUIManager: " + e.getMessage());
        } finally {
            isInitialized = false;
        }
    }

    public void clearScreen() {
        if (!isInitialized) {
            return;
        }

        screen.clear();
    }

    public void refresh() {
        if (!isInitialized) {
            return;
        }

        try {
            screen.refresh();
        } catch (IOException e) {
            //throw new IOException("Error while refreshing screen", e);
            System.err.println("Error while refreshing screen: " + e.getMessage());
        }
    }

    public KeyStroke waitForInput() {
        if (!isInitialized) return null;

        try {
            return screen.readInput();
        } catch (IOException e) {
            //throw new IOException("Error while reading input", e);
            System.err.println("Error while reading input: " + e.getMessage());
            return null;
        }
    }

    public TerminalSize getTerminalSize() {
        if (!isInitialized) return new TerminalSize(80, 24); // Default size

        return screen.getTerminalSize();
    }

    public TextGraphics getTextGraphics() {
        return textGraphics;
    }

    public void drawText(int x, int y, String text) {
        if (!isInitialized || textGraphics == null) {
            return;
        }

        textGraphics.putString(x, y, text);
    }

    public void drawTextWithBackground(int x, int y, String text, TextColor fgColor, TextColor bgColor){
        if (!isInitialized || textGraphics == null) {
            return;
        }

        textGraphics.setForegroundColor(fgColor);
        textGraphics.setBackgroundColor(bgColor);

        TerminalSize size = getTerminalSize();
        StringBuilder padding = new StringBuilder(text);

        while (padding.length() < size.getColumns()) {
            padding.append(" ");
        }

        textGraphics.putString(x, y, padding.toString());
    }


    public void drawCenteredText(int row, String text) {
        if (!isInitialized || textGraphics == null) {
            return;
        }

        TerminalSize size = getTerminalSize();
        int x = Math.max(0, (size.getColumns() - text.length()) / 2);
        textGraphics.putString(x, row, text);
    }

    public void drawHorizontalLine(int row) {
        if (!isInitialized || textGraphics == null) {
            return;
        }

        TerminalSize size = getTerminalSize();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < size.getColumns(); i++) {
            line.append(TUIConstants.H_LINE);
        }
        textGraphics.putString(0, row, line.toString());
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void hideCursor() {
        if (!isInitialized) {
            return;
        }

        screen.setCursorPosition(null);
    }

    public void showCursor() {
        if (!isInitialized) {
            return;
        }

        screen.setCursorPosition(new TerminalPosition(0, 0));
        }

    public void fillScreen(TextColor bgColor){
        if (!isInitialized || textGraphics == null) {
            return;
        }

        TerminalSize size = getTerminalSize();

        textGraphics.setBackgroundColor(bgColor);

        textGraphics.fillRectangle(
            new TerminalPosition(0, 0),
            size,
            ' '
        );
    }

    // Layout Management Methods

    /**
     * Draws the layout structure with header and footer.
     * 
     * @param headerText the text to display in the header (app name or filename)
     */
    public void drawLayout(String headerText) {
        if (!isInitialized || layoutManager == null) {
            return;
        }

        layoutManager.drawLayout(screen, headerText);
    }

    /**
     * Updates the status message in the footer.
     * 
     * @param status the status message to display
     */
    public void setLayoutStatus(String status) {
        if (!isInitialized || layoutManager == null) {
            return;
        }

        layoutManager.setStatus(status);
    }

    /**
     * Adds a log entry to the footer with specified level and message.
     * 
     * @param level the log level (INFO, WARNING, ERROR)
     * @param message the log message
     */
    public void addLayoutLog(LogLevel level, String message) {
        if (!isInitialized || layoutManager == null) {
            return;
        }

        layoutManager.addLog(level, message);
    }

    /**
     * Clears the current log entry from the footer.
     */
    public void clearLayoutLog() {
        if (!isInitialized || layoutManager == null) {
            return;
        }

        layoutManager.clearLog();
    }

    /**
     * Clears the current status message from the footer.
     */
    public void clearLayoutStatus() {
        if (!isInitialized || layoutManager == null) {
            return;
        }

        layoutManager.clearStatus();
    }

    /**
     * Gets the usable content area (excludes header, footer, and margins).
     * 
     * @return ContentArea object with position and size information
     */
    public LayoutManager.ContentArea getContentArea() {
        if (!isInitialized || layoutManager == null) {
            return null;
        }

        return layoutManager.getContentArea(getTerminalSize());
    }

}