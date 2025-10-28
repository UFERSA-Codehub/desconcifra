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

import java.io.IOException;

public class TUIManager {

    private Terminal terminal;
    private Screen screen;
    private TextGraphics textGraphics;
    private boolean isInitialized = false;

    public void initialize() throws IOException {
        if (isInitialized){
            return;
        }

        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        terminal = factory.createTerminal();

        screen = new TerminalScreen(terminal);
        screen.startScreen();

        textGraphics = screen.newTextGraphics();

        textGraphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        textGraphics.setBackgroundColor(TUIConstants.DEFAULT_BG_COLOR);

        isInitialized = true;
    }

    public void shutdown() throws IOException {
        if (!isInitialized) {
            return;
        }

        try {
        screen.stopScreen();
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

        try {
            terminal.setCursorVisible(false);
        } catch (IOException e) {
            //throw new IOException("Error while hiding cursor", e);
            System.err.println("Error while hiding cursor: " + e.getMessage());
        }
    }

    public void showCursor() {
        if (!isInitialized) {
            return;
        }

        try {
            terminal.setCursorVisible(true);
        } catch (IOException e) {
            //throw new IOException("Error while showing cursor", e);
            System.err.println("Error while showing cursor: " + e.getMessage());
        }
    }
}