package com.project.tui;

import com.googlecode.lanterna.TextColor;

public final class TUIConstants {
    private TUIConstants() {}

    public static final TextColor DEFAULT_FG_COLOR = TextColor.ANSI.WHITE;
    public static final TextColor DEFAULT_BG_COLOR = TextColor.ANSI.BLACK;

    public static final TextColor MENU_NORMAL_COLOR = TextColor.ANSI.WHITE;
    public static final TextColor MENU_SELECTED_COLOR = TextColor.ANSI.BLACK;
    public static final TextColor MENU_SELECTED_BG_COLOR = TextColor.ANSI.CYAN;

    public static final TextColor TITLE_COLOR = TextColor.ANSI.YELLOW;
    public static final TextColor HEADER_COLOR = TextColor.ANSI.GREEN;

    public static final TextColor SUCCESS_COLOR = TextColor.ANSI.GREEN;
    public static final TextColor ERROR_COLOR = TextColor.ANSI.RED;
    public static final TextColor WARNING_COLOR = TextColor.ANSI.YELLOW;
    public static final TextColor INFO_COLOR = TextColor.ANSI.BLUE;



    public static final int TITLE_ROW = 2;
    public static final int MENU_START_ROW = 5;
    public static final int STATUS_ROW = -3;


    public static final int MENU_INDENT = 4;
    public static final String MENU_POINTER = "-> ";
    public static final String MENU_SPACER = "   ";



    public static final String WELCOME_TITLE = "TUI Library test";
    public static final String EXIT_MESSAGE = "Press ESC to exit, ENTER to select";
    public static final String GOODBYE_MESSAGE = "cya!!!!!!!!!!!!!";



    public static final char H_LINE = '─';
    public static final char V_LINE = '│';
    public static final char TOP_LEFT_CORNER = '┌';
    public static final char TOP_RIGHT_CORNER = '┐';
    public static final char BOTTOM_LEFT_CORNER = '└';
    public static final char BOTTOM_RIGHT_CORNER = '┘';
}