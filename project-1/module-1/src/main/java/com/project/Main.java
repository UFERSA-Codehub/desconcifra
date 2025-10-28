package com.project;

import com.project.tui.TUIManager;
import com.project.tui.SimpleMenu;
import com.project.tui.TUIConstants;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;

import com.googlecode.lanterna.TextColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TuiTestApp app = new TuiTestApp();
        try {
            app.run();
        } catch (Exception e) {
            System.err.println("Error running TuiTestApp: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class TuiTestApp {
    private TUIManager tui;
    private int quizScore;
    private boolean running;

    public TuiTestApp() {
        this.tui = new TUIManager();
        this.quizScore = 0;
        this.running = true;
    }

    public void run() throws Exception {
        tui.initialize();

        try {
            
            displayWelcomeScreen();

            while (running) {
                int selection = displayMainMenu();

                switch (selection) {
                    case 0:
                        displayColorPalette();
                        break;
                    case 1:
                        displayBoxDrawing();
                        break;
                    case 2:
                        displayColorQuiz();
                        break;
                    case 3:
                        running = false;
                        break;
                    default:
                        // ESC or invalid selection, stay in menu
                        break;
                }
            }
        } finally {
            cleanup();
        }
    }

    private void displayWelcomeScreen() {
        tui.clearScreen();
        tui.fillScreen(TextColor.ANSI.BLACK);
        tui.refresh();

        TextGraphics graphics = tui.getTextGraphics();
        tui.hideCursor();

        // Title
        graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawCenteredText(TUIConstants.TITLE_ROW, "TUI Library Test");

        // Separator
        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        tui.drawHorizontalLine(TUIConstants.TITLE_ROW + 1);

        // Welcome message
        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        tui.drawCenteredText(6, "Welcome to the TUI Test Application");

        // Subtext
        graphics.setForegroundColor(TUIConstants.INFO_COLOR);
        tui.drawCenteredText(8, "This app tests the Lanterna TUI library features");

        // Instruction
        graphics.setForegroundColor(TUIConstants.MENU_SELECTED_BG_COLOR);
        tui.drawCenteredText(12, "Press any key to continue...");

        tui.refresh();

        // Wait for any key press
        tui.waitForInput();
    }

    private int displayMainMenu() {
        tui.clearScreen();

        TextGraphics graphics = tui.getTextGraphics();

        // Title
        graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawCenteredText(TUIConstants.TITLE_ROW, "Main Menu");

        // Separator
        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        tui.drawHorizontalLine(TUIConstants.TITLE_ROW + 1);

        // Create menu items
        List<String> menuItems = new ArrayList<String>();
        menuItems.add("View Color Palette");
        menuItems.add("View Box Drawing Characters");
        menuItems.add("Take Color Quiz");
        menuItems.add("Exit Application");

        // Create and show menu
        SimpleMenu menu = new SimpleMenu(tui, menuItems);
        int selection = menu.show();

        return selection;
    }

    private void displayColorPalette() {
        tui.clearScreen();

        TextGraphics graphics = tui.getTextGraphics();

        // Title
        graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawCenteredText(TUIConstants.TITLE_ROW, "Color Palette Demo");

        // Separator
        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        tui.drawHorizontalLine(TUIConstants.TITLE_ROW + 1);

        // Display colors
        int row = 6;

        graphics.setForegroundColor(TUIConstants.SUCCESS_COLOR);
        tui.drawText(10, row, "Success Color (Green)");
        row += 2;

        graphics.setForegroundColor(TUIConstants.ERROR_COLOR);
        tui.drawText(10, row, "Error Color (Red)");
        row += 2;

        graphics.setForegroundColor(TUIConstants.WARNING_COLOR);
        tui.drawText(10, row, "Warning Color (Yellow)");
        row += 2;

        graphics.setForegroundColor(TUIConstants.INFO_COLOR);
        tui.drawText(10, row, "Info Color (Blue)");
        row += 2;

        graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawText(10, row, "Title Color (Yellow)");
        row += 2;

        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        tui.drawText(10, row, "Header Color (Green)");
        row += 2;

        // Instruction
        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        tui.drawCenteredText(row + 2, "Press any key to return to menu");

        tui.refresh();

        // Wait for input
        tui.waitForInput();
    }

    private void displayBoxDrawing() {
        tui.clearScreen();

        TextGraphics graphics = tui.getTextGraphics();

        // Title
        graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawCenteredText(TUIConstants.TITLE_ROW, "Box Drawing Characters Demo");

        // Separator
        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        tui.drawHorizontalLine(TUIConstants.TITLE_ROW + 1);

        // Character legend
        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        int row = 6;

        tui.drawText(10, row, "Horizontal: " + TUIConstants.H_LINE);
        row += 2;

        tui.drawText(10, row, "Vertical: " + TUIConstants.V_LINE);
        row += 2;

        tui.drawText(10, row, "Top-Left: " + TUIConstants.TOP_LEFT_CORNER + "  Top-Right: " + TUIConstants.TOP_RIGHT_CORNER);
        row += 2;

        tui.drawText(10, row, "Bottom-Left: " + TUIConstants.BOTTOM_LEFT_CORNER + "  Bottom-Right: " + TUIConstants.BOTTOM_RIGHT_CORNER);
        row += 3;

        // Sample box
        graphics.setForegroundColor(TUIConstants.MENU_SELECTED_BG_COLOR);
        tui.drawText(10, row, TUIConstants.TOP_LEFT_CORNER + "-------------------------" + TUIConstants.TOP_RIGHT_CORNER);
        row++;

        tui.drawText(10, row, TUIConstants.V_LINE + "   Sample Box Layout    " + TUIConstants.V_LINE);
        row++;

        tui.drawText(10, row, TUIConstants.V_LINE + "   With proper corners  " + TUIConstants.V_LINE);
        row++;

        tui.drawText(10, row, TUIConstants.V_LINE + "   And drawing chars    " + TUIConstants.V_LINE);
        row++;

        tui.drawText(10, row, TUIConstants.BOTTOM_LEFT_CORNER + "-------------------------" + TUIConstants.BOTTOM_RIGHT_CORNER);
        row += 2;

        // Instruction
        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        tui.drawCenteredText(row + 2, "Press any key to return to menu");

        tui.refresh();

        // Wait for input
        tui.waitForInput();
    }

    private void displayColorQuiz() {
        // Reset score
        quizScore = 0;

        // Ask 3 questions
        if (askQuiz1()) {
            quizScore++;
        }

        if (askQuiz2()) {
            quizScore++;
        }

        if (askQuiz3()) {
            quizScore++;
        }

        // Show final results
        displayQuizResult(quizScore);
    }

    private boolean askQuiz1() {
        tui.clearScreen();

        TextGraphics graphics = tui.getTextGraphics();

        // Title
        graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawCenteredText(TUIConstants.TITLE_ROW, "Quiz Question 1/3");

        // Separator
        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        tui.drawHorizontalLine(TUIConstants.TITLE_ROW + 1);

        // Question
        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        tui.drawCenteredText(6, "What color is shown below?");

        // Show color sample
        graphics.setForegroundColor(TUIConstants.MENU_SELECTED_COLOR);
        graphics.setBackgroundColor(TUIConstants.MENU_SELECTED_BG_COLOR);
        tui.drawText(35, 9, "       ");

        // Reset to normal
        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        graphics.setBackgroundColor(TUIConstants.DEFAULT_BG_COLOR);

        tui.drawText(10, 12, "Choose the color:");

        tui.refresh();

        // Menu options
        List<String> options = new ArrayList<String>();
        options.add("A) Red");
        options.add("B) Cyan");
        options.add("C) Yellow");
        options.add("D) Green");

        SimpleMenu menu = new SimpleMenu(tui, options, 14);
        int selection = menu.show();

        // Check answer (B is correct, index 1)
        boolean correct = (selection == 1);
        showResultScreen(correct, "Cyan");

        return correct;
    }

    private boolean askQuiz2() {
        tui.clearScreen();

        TextGraphics graphics = tui.getTextGraphics();

        // Title
        graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawCenteredText(TUIConstants.TITLE_ROW, "Quiz Question 2/3");

        // Separator
        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        tui.drawHorizontalLine(TUIConstants.TITLE_ROW + 1);

        // Question
        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        tui.drawCenteredText(6, "What color is shown below?");

        // Show color sample
        graphics.setForegroundColor(TUIConstants.ERROR_COLOR);
        tui.drawText(37, 9, "███");

        // Reset to normal
        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);

        tui.drawText(10, 12, "Choose the color:");

        tui.refresh();

        // Menu options
        List<String> options = new ArrayList<String>();
        options.add("A) Green");
        options.add("B) Blue");
        options.add("C) Red");
        options.add("D) Yellow");

        SimpleMenu menu = new SimpleMenu(tui, options, 14);
        int selection = menu.show();

        // Check answer (C is correct, index 2)
        boolean correct = (selection == 2);
        showResultScreen(correct, "Red");

        return correct;
    }

    private boolean askQuiz3() {
        tui.clearScreen();

        TextGraphics graphics = tui.getTextGraphics();

        // Title
        graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawCenteredText(TUIConstants.TITLE_ROW, "Quiz Question 3/3");

        // Separator
        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        tui.drawHorizontalLine(TUIConstants.TITLE_ROW + 1);

        // Question
        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        tui.drawCenteredText(6, "What color is shown below?");

        // Show color sample
        graphics.setForegroundColor(TUIConstants.WARNING_COLOR);
        tui.drawText(37, 9, "▲▼◆");

        // Reset to normal
        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);

        tui.drawText(10, 12, "Choose the color:");

        tui.refresh();

        // Menu options
        List<String> options = new ArrayList<String>();
        options.add("A) Yellow");
        options.add("B) White");
        options.add("C) Blue");
        options.add("D) Green");

        SimpleMenu menu = new SimpleMenu(tui, options, 14);
        int selection = menu.show();

        // Check answer (A is correct, index 0)
        boolean correct = (selection == 0);
        showResultScreen(correct, "Yellow");

        return correct;
    }

    private void showResultScreen(boolean correct, String answer) {
        tui.clearScreen();

        TextGraphics graphics = tui.getTextGraphics();

        if (correct) {
            graphics.setForegroundColor(TUIConstants.SUCCESS_COLOR);
            tui.drawCenteredText(10, "Correct! The answer was: " + answer);
        } else {
            graphics.setForegroundColor(TUIConstants.ERROR_COLOR);
            tui.drawCenteredText(10, "Wrong! The correct answer was: " + answer);
        }

        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        tui.drawCenteredText(13, "Press any key to continue...");

        tui.refresh();

        // Wait for input
        tui.waitForInput();
    }

    private void displayQuizResult(int score) {
        tui.clearScreen();

        TextGraphics graphics = tui.getTextGraphics();

        // Title
        graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawCenteredText(TUIConstants.TITLE_ROW, "Quiz Complete!");

        // Separator
        graphics.setForegroundColor(TUIConstants.HEADER_COLOR);
        tui.drawHorizontalLine(TUIConstants.TITLE_ROW + 1);

        // Score display
        graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
        tui.drawCenteredText(7, "Your Score: " + score + " / 3");

        // Performance message based on score
        int messageRow = 10;
        if (score == 3) {
            graphics.setForegroundColor(TUIConstants.SUCCESS_COLOR);
            tui.drawCenteredText(messageRow, "Perfect! You answered all questions correctly!");
        } else if (score == 2) {
            graphics.setForegroundColor(TUIConstants.INFO_COLOR);
            tui.drawCenteredText(messageRow, "Great job! You got 2 out of 3 correct.");
        } else if (score == 1) {
            graphics.setForegroundColor(TUIConstants.WARNING_COLOR);
            tui.drawCenteredText(messageRow, "Good effort! You got 1 out of 3 correct.");
        } else {
            graphics.setForegroundColor(TUIConstants.ERROR_COLOR);
            tui.drawCenteredText(messageRow, "Don't worry, try again! You can do better.");
        }

        // Instruction
        graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        tui.drawCenteredText(messageRow + 5, "Press any key to return to menu");

        tui.refresh();

        // Wait for input
        tui.waitForInput();
    }

    private void cleanup() {
        try {
            tui.clearScreen();

            TextGraphics graphics = tui.getTextGraphics();
            graphics.setForegroundColor(TUIConstants.TITLE_COLOR);
            tui.drawCenteredText(10, "Goodbye! Thanks for testing the TUI library!");

            graphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
            tui.refresh();

            // Give user time to see goodbye message
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                // Ignore
            }

            tui.shutdown();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}
