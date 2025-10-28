package com.project.tui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.io.IOException;


public class TUIExample {
    private TUIManager tui;
    private boolean running = true;

    public TUIExample() {
        tui = new TUIManager();
    }

    public static void main(String[] args) {
        TUIExample example = new TUIExample();
        try {
            example.run();
        } catch (Exception e) {
            System.err.println("Error running TUIExample: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void run() throws Exception {
        tui.initialize();

        try {
            displayWelcomeScreen();
            tui.waitForInput();
            displayInfoScreen();
            tui.waitForInput();
        } finally {
            tui.shutdown();
            System.out.println(TUIConstants.GOODBYE_MESSAGE);
        }
        
    }

    private void displayWelcomeScreen() {
        tui.clearScreen();

        TextGraphics textGraphics = tui.getTextGraphics();

        textGraphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawCenteredText(TUIConstants.TITLE_ROW, TUIConstants.WELCOME_TITLE);

        textGraphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        tui.drawHorizontalLine(TUIConstants.TITLE_ROW + 1);


        textGraphics.setForegroundColor(TUIConstants.MENU_NORMAL_COLOR);
        tui.drawCenteredText(TUIConstants.TITLE_ROW + 4, "test test welcome bruda");
        tui.drawCenteredText(TUIConstants.TITLE_ROW + 6, "mensagem 2");
        tui.drawCenteredText(TUIConstants.TITLE_ROW + 8, "");
        tui.drawCenteredText(TUIConstants.TITLE_ROW + 10, "aaaa");

        tui.refresh();
    }

    private void displayInfoScreen() {
        tui.clearScreen();

        TextGraphics graphics = tui.getTextGraphics();

        // Draw title
        graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawCenteredText(TUIConstants.TITLE_ROW, "Color Demonstration");

        // Draw separator
        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        tui.drawHorizontalLine(TUIConstants.TITLE_ROW + 1);

        // Show different colors
        int row = TUIConstants.TITLE_ROW + 4;

        graphics.setForegroundColor(TUIConstants.SUCCESS_COLOR);
        tui.drawText(10, row, "✓ This is SUCCESS color (Green)");
        row += 2;

        graphics.setForegroundColor(TUIConstants.ERROR_COLOR);
        tui.drawText(10, row, "✗ This is ERROR color (Red)");
        row += 2;

        graphics.setForegroundColor(TUIConstants.WARNING_COLOR);
        tui.drawText(10, row, "⚠ This is WARNING color (Yellow)");
        row += 2;

        graphics.setForegroundColor(TUIConstants.INFO_COLOR);
        tui.drawText(10, row, "ℹ This is INFO color (Blue)");
        row += 2;

        // Draw footer
        graphics.setForegroundColor(TUIConstants.MENU_NORMAL_COLOR);
        tui.drawCenteredText(row + 3, TUIConstants.EXIT_MESSAGE);

        // Refresh screen
        tui.refresh();
    }
}