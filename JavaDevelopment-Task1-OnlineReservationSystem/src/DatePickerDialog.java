import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class DatePickerDialog extends JDialog {
    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private final JLabel monthYearLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel daysGrid = new JPanel(new GridLayout(0, 7, 4, 4));

    public DatePickerDialog(Window owner, LocalDate initialDate) {
        super(owner, "Select Journey Date", ModalityType.APPLICATION_MODAL);
        this.currentMonth = (initialDate != null) ? YearMonth.from(initialDate) : YearMonth.now();
        this.selectedDate = initialDate;

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(ThemeManager.bgCard());
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
        headerPanel.setBackground(ThemeManager.bgCard());

        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        ThemeManager.styleSecondaryButton(prevButton);
        ThemeManager.styleSecondaryButton(nextButton);

        monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        monthYearLabel.setForeground(ThemeManager.textPrimary());

        prevButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            renderCalendar();
        });

        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            renderCalendar();
        });

        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(monthYearLabel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);

        daysGrid.setBackground(ThemeManager.bgCard());

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(daysGrid, BorderLayout.CENTER);

        renderCalendar();

        setContentPane(root);
        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    private void renderCalendar() {
        daysGrid.removeAll();

        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthYearLabel.setText(monthName + " " + currentMonth.getYear());

        String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String dayName : dayHeaders) {
            JLabel lbl = new JLabel(dayName, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(ThemeManager.textSecondary());
            daysGrid.add(lbl);
        }

        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int startColumn = firstOfMonth.getDayOfWeek().getValue() % 7;
        int totalDays = currentMonth.lengthOfMonth();

        for (int i = 0; i < startColumn; i++) {
            daysGrid.add(new JLabel(""));
        }

        for (int day = 1; day <= totalDays; day++) {
            LocalDate cellDate = currentMonth.atDay(day);
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dayButton.setOpaque(true);
            dayButton.setFocusPainted(false);
            dayButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ThemeManager.border(), 1, true),
                new EmptyBorder(6, 9, 6, 9)
            ));

            if (cellDate.isBefore(today)) {
                dayButton.setEnabled(false);
                dayButton.setBackground(ThemeManager.bgMain());
                dayButton.setForeground(ThemeManager.textSecondary());
            } else if (cellDate.equals(today)) {
                dayButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
                dayButton.setBackground(ThemeManager.primary());
                dayButton.setForeground(Color.WHITE);
                dayButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                dayButton.setBackground(ThemeManager.bgInput());
                dayButton.setForeground(ThemeManager.textPrimary());
                dayButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            dayButton.addActionListener(e -> {
                selectedDate = cellDate;
                dispose();
            });

            daysGrid.add(dayButton);
        }

        daysGrid.revalidate();
        daysGrid.repaint();
        pack();
    }

    public static String pickDate(Component parent, String currentText) {
        LocalDate initial = null;
        try {
            if (currentText != null && !currentText.isBlank()) {
                initial = LocalDate.parse(currentText.trim());
            }
        } catch (Exception ignored) {}

        Window owner = (parent != null) ? SwingUtilities.getWindowAncestor(parent) : null;
        DatePickerDialog dialog = new DatePickerDialog(owner, initial);
        dialog.setVisible(true);

        return (dialog.selectedDate != null) ? dialog.selectedDate.toString() : null;
    }
}