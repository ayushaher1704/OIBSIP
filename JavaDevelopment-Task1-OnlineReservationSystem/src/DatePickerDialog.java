import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DatePickerDialog extends JDialog {
    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private final JLabel monthYearLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel daysGrid = new JPanel(new GridLayout(0, 7, 4, 4));

    public DatePickerDialog(Window owner, LocalDate initialDate) {
        super(owner, "Select Journey Date", ModalityType.APPLICATION_MODAL);
        this.currentMonth = (initialDate != null) ? YearMonth.from(initialDate) : YearMonth.now();
        this.selectedDate = initialDate;

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Top bar: Previous Month (<), Month & Year title, Next Month (>)
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");

        prevButton.setFocusPainted(false);
        nextButton.setFocusPainted(false);
        monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

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

        // Row 1: Day names (Sun to Sat)
        String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String dayName : dayHeaders) {
            JLabel lbl = new JLabel(dayName, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(Color.GRAY);
            daysGrid.add(lbl);
        }

        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int startColumn = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0, Saturday = 6
        int totalDays = currentMonth.lengthOfMonth();

        // Empty spacer cells before the 1st of the month
        for (int i = 0; i < startColumn; i++) {
            daysGrid.add(new JLabel(""));
        }

        // Day buttons (1 to totalDays)
        for (int day = 1; day <= totalDays; day++) {
            LocalDate cellDate = currentMonth.atDay(day);
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dayButton.setFocusPainted(false);
            dayButton.setMargin(new Insets(5, 8, 5, 8));

            // Disable past dates so users only book today or future journeys
            if (cellDate.isBefore(today)) {
                dayButton.setEnabled(false);
            }

            // Highlight today's date in bold
            if (cellDate.equals(today)) {
                dayButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
                dayButton.setForeground(new Color(29, 78, 216));
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

    // Helper method any form can call to open the calendar and get "YYYY-MM-DD"
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

    // Quick test to preview the mini-calendar popup
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            String chosen = pickDate(null, null);
            System.out.println("Chosen Date: " + chosen);
            System.exit(0);
        });
    }
}