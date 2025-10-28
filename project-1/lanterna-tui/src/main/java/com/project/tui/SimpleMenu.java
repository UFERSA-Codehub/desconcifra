package com.project.tui;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.TextColor;

import java.util.ArrayList;
import java.util.List;

public class SimpleMenu {
    private TUIManager tui;
    private List<String> menuItems;
    private int selectedIndex;
    private int startRow;
    private boolean running;

    public SimpleMenu(TUIManager tui, List<String> items, int startRow) {
        this.tui = tui;
        this.menuItems = new ArrayList<>(items);
        this.startRow = startRow;
        this.selectedIndex = 0;
        this.running = true;
    }

    public SimpleMenu(TUIManager tui, List<String> items) {
        this(tui, items, TUIConstants.MENU_START_ROW);
    }

    public int show() {
        running = true;

        while (running) {
            drawMenu();
            handleInput();
        }

        return selectedIndex;
    }


    private void drawMenu() {
        TextGraphics textGraphics = tui.getTextGraphics();

        for (int i=0; i<menuItems.size(); i++){
            int row = startRow + (i*2);
            String item = menuItems.get(i);

            if (i == selectedIndex) {
                tui.drawTextWithBackground(
                 TUIConstants.MENU_INDENT,
                 row,
                 TUIConstants.MENU_POINTER + item,
                 TUIConstants.MENU_SELECTED_COLOR,
                 TUIConstants.MENU_SELECTED_BG_COLOR);
            } else {
                tui.drawTextWithBackground(
                 TUIConstants.MENU_INDENT,
                 row,
                 TUIConstants.MENU_SPACER + item,
                 TUIConstants.MENU_NORMAL_COLOR,
                 TUIConstants.DEFAULT_BG_COLOR);
            }
        }

        textGraphics.setForegroundColor(TUIConstants.DEFAULT_FG_COLOR);
        textGraphics.setBackgroundColor(TUIConstants.DEFAULT_BG_COLOR);
        tui.refresh();


    }

    private void handleInput() {
        KeyStroke keyStroke = tui.waitForInput();

        if (keyStroke == null) {
            return;
        }

        KeyType keyType = keyStroke.getKeyType();

        switch (keyType) {
            case ArrowUp:
                selectedIndex = (selectedIndex - 1 + menuItems.size()) % menuItems.size();
                break;
            case ArrowDown:
                selectedIndex = (selectedIndex + 1) % menuItems.size();
                break;
            case Enter:
                running = false;
                break;
            case Escape:
                selectedIndex = -1;
                running = false;
                break;
            default:
                // Ignore other keys
                break;
        }
    }

    public String getSelectedItem() {
        if (selectedIndex >= 0 && selectedIndex < menuItems.size()) {
            return menuItems.get(selectedIndex);
        }
        return null;
    }

    public void addMenuItem(String item) {
        menuItems.add(item);
    }

    public int getMenuSize() {
        return menuItems.size();
    }
}