import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class ThemeManager {
    private static boolean darkMode = false;

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void toggleTheme() {
        darkMode = !darkMode;
    }

    // Core Surfaces
    public static Color bgMain() {
        return darkMode ? new Color(15, 23, 42) : new Color(241, 245, 249);
    }

    public static Color bgCard() {
        return darkMode ? new Color(30, 41, 59) : Color.WHITE;
    }

    public static Color bgInput() {
        return darkMode ? new Color(15, 23, 42) : new Color(248, 250, 252);
    }

    public static Color border() {
        return darkMode ? new Color(51, 65, 85) : new Color(203, 213, 225);
    }

    // Typography
    public static Color textPrimary() {
        return darkMode ? new Color(248, 250, 252) : new Color(15, 23, 42);
    }

    public static Color textSecondary() {
        return darkMode ? new Color(148, 163, 184) : new Color(71, 85, 105);
    }

    // Accents & Buttons
    public static Color primary() {
        return darkMode ? new Color(59, 130, 246) : new Color(29, 78, 216);
    }

    public static Color danger() {
        return darkMode ? new Color(239, 68, 68) : new Color(220, 38, 38);
    }

    public static Color secondaryBg() {
        return darkMode ? new Color(51, 65, 85) : new Color(226, 232, 240);
    }

    public static Color secondaryFg() {
        return darkMode ? new Color(248, 250, 252) : new Color(15, 23, 42);
    }

    public static Color accentGreen() {
        return darkMode ? new Color(74, 222, 128) : new Color(21, 128, 61);
    }

    public static Color tableHeader() {
        return darkMode ? new Color(15, 23, 42) : new Color(226, 232, 240);
    }

    public static Color selectionBg() {
        return darkMode ? new Color(30, 58, 138) : new Color(219, 234, 254);
    }

    // Component Styling Helpers
    public static JPanel createCardPanel(LayoutManager layout, int padV, int padH) {
        JPanel card = new JPanel(layout);
        card.setBackground(bgCard());
        card.setBorder(new CompoundBorder(
            new LineBorder(border(), 1, true),
            new EmptyBorder(padV, padH, padV, padH)
        ));
        return card;
    }

    public static void styleCard(JPanel card, int padV, int padH) {
        card.setBackground(bgCard());
        card.setBorder(new CompoundBorder(
            new LineBorder(border(), 1, true),
            new EmptyBorder(padV, padH, padV, padH)
        ));
    }

    public static void styleTextField(JTextField field, boolean editable) {
        field.setEditable(editable);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(bgInput());
        field.setForeground(textPrimary());
        field.setCaretColor(textPrimary());
        field.setBorder(new CompoundBorder(
            new LineBorder(border(), 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
    }

    public static void styleComboBox(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(bgInput());
        box.setForeground(textPrimary());
    }

    public static void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBackground(primary());
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(primary(), 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
    }

    public static void styleDangerButton(JButton btn, boolean enabled) {
        btn.setEnabled(enabled);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        if (enabled) {
            btn.setBackground(danger());
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(
                new LineBorder(danger(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
            ));
        } else {
            btn.setBackground(secondaryBg());
            btn.setForeground(textSecondary());
            btn.setBorder(new CompoundBorder(
                new LineBorder(border(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
            ));
        }
    }

    public static void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBackground(secondaryBg());
        btn.setForeground(secondaryFg());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(border(), 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
    }
}