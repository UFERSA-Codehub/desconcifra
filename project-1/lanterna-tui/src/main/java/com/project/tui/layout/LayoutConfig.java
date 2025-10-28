package com.project.tui.layout;

/**
 * Configuration constants for the TUI layout system.
 * Defines margins, section heights, and layout structure.
 */
public final class LayoutConfig {
    private LayoutConfig() {}

    // Margin configuration
    public static final int LEFT_MARGIN = 2;
    public static final int RIGHT_MARGIN = 2;
    public static final int TOP_MARGIN = 2;
    public static final int BOTTOM_MARGIN = 2;

    // Fixed section heights
    public static final int HEADER_HEIGHT = 3;  // title + separator + blank row
    public static final int FOOTER_HEIGHT = 1;  // single row for status | log

    // Minimum terminal dimensions
    public static final int MIN_TERMINAL_WIDTH = 40;
    public static final int MIN_TERMINAL_HEIGHT = 10;
}
