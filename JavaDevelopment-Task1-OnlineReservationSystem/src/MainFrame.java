import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

public class MainFrame extends JFrame {
    // Shared containers for live theme updates
    private final JPanel rootContainer = new JPanel(new BorderLayout());
    private final JPanel topBar = new JPanel(new BorderLayout());
    private final JLabel appTitleLabel = new JLabel("Indian Railways — Reservation Desk");
    private final JLabel clerkBadgeLabel = new JLabel();
    private final JButton themeToggleButton = new JButton();
    private final JButton signOutButton = new JButton("Sign Out");
    private final JTabbedPane tabbedPane = new JTabbedPane();

    private final List<JPanel> pageWrappers = new ArrayList<>();
    private final List<JPanel> cardPanels = new ArrayList<>();
    private final List<JLabel> headingLabels = new ArrayList<>();
    private final List<JLabel> fieldLabels = new ArrayList<>();

    // Booking tab fields
    private final JTextField passengerNameField = new JTextField(22);
    private final JComboBox<String> trainComboBox = new JComboBox<>();
    private final JComboBox<String> boardingComboBox = new JComboBox<>();
    private final JComboBox<String> destinationComboBox = new JComboBox<>();
    private final JComboBox<String> classComboBox = new JComboBox<>();
    private final JTextField journeyDateField = new JTextField(13);
    private final JPanel datePanel = new JPanel(new BorderLayout(8, 0));
    private final JButton pickDateButton = new JButton("Select Date");
    private final JLabel estimatedFareLabel = new JLabel("₹0");
    private final JButton bookButton = new JButton("Book Ticket");
    private boolean isUpdatingControls = false;

    // Cancellation tab fields
    private final JTextField pnrSearchField = new JTextField(16);
    private final JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
    private final JButton fetchButton = new JButton("Fetch Ticket");
    private final JPanel detailsCard = new JPanel(new GridBagLayout());
    private final JLabel detailsSectionLabel = new JLabel("Ticket Summary");
    private final JLabel cancelPnrValue = new JLabel("-");
    private final JLabel cancelPassengerValue = new JLabel("-");
    private final JLabel cancelTrainValue = new JLabel("-");
    private final JLabel cancelClassValue = new JLabel("-");
    private final JLabel cancelDateValue = new JLabel("-");
    private final JLabel cancelRouteValue = new JLabel("-");
    private final JLabel cancelFareValue = new JLabel("-");
    private final JButton confirmCancelButton = new JButton("Confirm Cancellation");
    private String loadedPnr = null;

    // All Tickets tab fields
    private final JPanel filterPanel = new JPanel(new BorderLayout(10, 0));
    private final JTextField filterField = new JTextField();
    private final DefaultTableModel ticketsTableModel = new DefaultTableModel(
        new String[]{"PNR", "Passenger", "Train", "Class", "Date", "Route", "Fare"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable ticketsTable = new JTable(ticketsTableModel);
    private final JScrollPane tableScrollPane = new JScrollPane(ticketsTable);
    private final TableRowSorter<DefaultTableModel> tableSorter = new TableRowSorter<>(ticketsTableModel);
    private final JPanel bottomBar = new JPanel(new BorderLayout(8, 0));
    private final JPanel tableButtonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JLabel copyStatusLabel = new JLabel("Select a row to copy its PNR");
    private final JButton copyPnrButton = new JButton("Copy Selected PNR");
    private final JButton refreshButton = new JButton("Refresh");

    public MainFrame(String clerkName) {
        setTitle("Railway Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        rootContainer.add(createTopBar(clerkName), BorderLayout.NORTH);

        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("Book Ticket", createBookingPanel());
        tabbedPane.addTab("Cancel Ticket", createCancellationPanel());
        tabbedPane.addTab("All Tickets", createAllTicketsPanel());

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(14, 18, 18, 18));
        centerWrapper.add(tabbedPane, BorderLayout.CENTER);
        rootContainer.add(centerWrapper, BorderLayout.CENTER);

        add(rootContainer);
        refreshTicketsTable();
        applyTheme();

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createTopBar(String clerkName) {
        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftInfo.setOpaque(false);

        appTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        clerkBadgeLabel.setText("Clerk: " + clerkName);
        clerkBadgeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        leftInfo.add(appTitleLabel);
        leftInfo.add(clerkBadgeLabel);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        themeToggleButton.addActionListener(e -> {
            ThemeManager.toggleTheme();
            applyTheme();
        });

        signOutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        rightActions.add(themeToggleButton);
        rightActions.add(signOutButton);

        topBar.add(leftInfo, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        return topBar;
    }

    // ==================== TAB 1: BOOK TICKET ====================
    private JPanel createBookingPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));
        pageWrappers.add(wrapper);

        JPanel card = new JPanel(new GridBagLayout());
        cardPanels.add(card);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("New Ticket Reservation");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        headingLabels.add(heading);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 8, 12, 8);
        card.add(heading, gbc);
        gbc.insets = new Insets(7, 8, 7, 8);

        List<String> trains = ReservationService.getAllTrains();
        for (String train : trains) {
            trainComboBox.addItem(train);
        }
        trainComboBox.setMaximumRowCount(8);

        pickDateButton.addActionListener(e -> {
            String chosenDate = DatePickerDialog.pickDate(this, journeyDateField.getText());
            if (chosenDate != null) {
                journeyDateField.setText(chosenDate);
            }
        });

        datePanel.setOpaque(false);
        datePanel.add(journeyDateField, BorderLayout.CENTER);
        datePanel.add(pickDateButton, BorderLayout.EAST);

        estimatedFareLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        gbc.gridwidth = 1;
        addRow(card, gbc, 1, "Passenger Name:", passengerNameField);
        addRow(card, gbc, 2, "Select Train:", trainComboBox);
        addRow(card, gbc, 3, "Boarding Station:", boardingComboBox);
        addRow(card, gbc, 4, "Destination Station:", destinationComboBox);
        addRow(card, gbc, 5, "Class Type:", classComboBox);
        addRow(card, gbc, 6, "Journey Date:", datePanel);
        addRow(card, gbc, 7, "Estimated Fare:", estimatedFareLabel);

        trainComboBox.addActionListener(e -> updateTrainOptions());
        boardingComboBox.addActionListener(e -> { if (!isUpdatingControls) updateFarePreview(); });
        destinationComboBox.addActionListener(e -> { if (!isUpdatingControls) updateFarePreview(); });
        classComboBox.addActionListener(e -> { if (!isUpdatingControls) updateFarePreview(); });

        updateTrainOptions();

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 4, 8);
        card.add(bookButton, gbc);

        bookButton.addActionListener(e -> handleBooking());

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ==================== TAB 2: CANCEL TICKET ====================
    private JPanel createCancellationPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));
        pageWrappers.add(wrapper);

        JPanel card = new JPanel(new GridBagLayout());
        cardPanels.add(card);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Cancel Reservation by PNR");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        headingLabels.add(heading);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 8, 12, 8);
        card.add(heading, gbc);
        gbc.insets = new Insets(7, 8, 7, 8);

        fetchButton.addActionListener(e -> handleFetchPnr());

        searchPanel.setOpaque(false);
        searchPanel.add(pnrSearchField, BorderLayout.CENTER);
        searchPanel.add(fetchButton, BorderLayout.EAST);

        gbc.gridwidth = 1;
        addRow(card, gbc, 1, "Enter PNR Number:", searchPanel);

        // Nested summary card
        GridBagConstraints dGbc = new GridBagConstraints();
        dGbc.insets = new Insets(5, 6, 5, 12);
        dGbc.anchor = GridBagConstraints.WEST;
        dGbc.fill = GridBagConstraints.HORIZONTAL;

        detailsSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dGbc.gridx = 0;
        dGbc.gridy = 0;
        dGbc.gridwidth = 2;
        dGbc.insets = new Insets(0, 6, 8, 6);
        detailsCard.add(detailsSectionLabel, dGbc);
        dGbc.gridwidth = 1;
        dGbc.insets = new Insets(4, 6, 4, 12);

        addDetailRow(detailsCard, dGbc, 1, "PNR Number:", cancelPnrValue);
        addDetailRow(detailsCard, dGbc, 2, "Passenger Name:", cancelPassengerValue);
        addDetailRow(detailsCard, dGbc, 3, "Train:", cancelTrainValue);
        addDetailRow(detailsCard, dGbc, 4, "Class:", cancelClassValue);
        addDetailRow(detailsCard, dGbc, 5, "Journey Date:", cancelDateValue);
        addDetailRow(detailsCard, dGbc, 6, "Route:", cancelRouteValue);
        addDetailRow(detailsCard, dGbc, 7, "Ticket Fare:", cancelFareValue);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 8, 10, 8);
        card.add(detailsCard, gbc);

        confirmCancelButton.addActionListener(e -> handleCancelTicket());

        gbc.gridy = 3;
        gbc.insets = new Insets(8, 8, 4, 8);
        card.add(confirmCancelButton, gbc);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ==================== TAB 3: ALL TICKETS ====================
    private JPanel createAllTicketsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));
        pageWrappers.add(wrapper);

        JPanel card = new JPanel(new BorderLayout(12, 12));
        cardPanels.add(card);

        filterPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search (PNR / Name / Train / Station):");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldLabels.add(searchLabel);

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            private void applyFilter() {
                String text = filterField.getText().trim();
                if (text.isEmpty()) {
                    tableSorter.setRowFilter(null);
                } else {
                    tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
                }
            }
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        filterPanel.add(searchLabel, BorderLayout.WEST);
        filterPanel.add(filterField, BorderLayout.CENTER);
        card.add(filterPanel, BorderLayout.NORTH);

        ticketsTable.setRowSorter(tableSorter);
        ticketsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketsTable.setRowHeight(26);
        ticketsTable.getTableHeader().setReorderingAllowed(false);
        ticketsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ticketsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        ticketsTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        ticketsTable.getColumnModel().getColumn(1).setPreferredWidth(105);
        ticketsTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        ticketsTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        ticketsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        ticketsTable.getColumnModel().getColumn(5).setPreferredWidth(165);
        ticketsTable.getColumnModel().getColumn(6).setPreferredWidth(65);

        tableScrollPane.setPreferredSize(new Dimension(740, 250));
        card.add(tableScrollPane, BorderLayout.CENTER);

        bottomBar.setOpaque(false);
        copyStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        tableButtonGroup.setOpaque(false);
        copyPnrButton.addActionListener(e -> handleCopyPnr());
        refreshButton.addActionListener(e -> {
            refreshTicketsTable();
            copyStatusLabel.setText("Table refreshed (" + ticketsTableModel.getRowCount() + " active tickets)");
            copyStatusLabel.setForeground(ThemeManager.textSecondary());
        });

        tableButtonGroup.add(copyPnrButton);
        tableButtonGroup.add(refreshButton);

        bottomBar.add(copyStatusLabel, BorderLayout.WEST);
        bottomBar.add(tableButtonGroup, BorderLayout.EAST);
        card.add(bottomBar, BorderLayout.SOUTH);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private void applyTheme() {
        rootContainer.setBackground(ThemeManager.bgMain());

        topBar.setBackground(ThemeManager.bgCard());
        topBar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, ThemeManager.border()),
            new EmptyBorder(12, 20, 12, 20)
        ));
        appTitleLabel.setForeground(ThemeManager.textPrimary());
        clerkBadgeLabel.setForeground(ThemeManager.textSecondary());

        themeToggleButton.setText(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        ThemeManager.styleSecondaryButton(themeToggleButton);
        ThemeManager.styleSecondaryButton(signOutButton);

        tabbedPane.setBackground(ThemeManager.bgCard());
        tabbedPane.setForeground(ThemeManager.textPrimary());

        for (JPanel wrapper : pageWrappers) {
            wrapper.setBackground(ThemeManager.bgMain());
        }
        for (JPanel card : cardPanels) {
            ThemeManager.styleCard(card, 20, 24);
        }
        for (JLabel h : headingLabels) {
            h.setForeground(ThemeManager.textPrimary());
        }
        for (JLabel lbl : fieldLabels) {
            lbl.setForeground(ThemeManager.textSecondary());
        }

        // Booking tab controls
        ThemeManager.styleTextField(passengerNameField, true);
        ThemeManager.styleComboBox(trainComboBox);
        ThemeManager.styleComboBox(boardingComboBox);
        ThemeManager.styleComboBox(destinationComboBox);
        ThemeManager.styleComboBox(classComboBox);
        ThemeManager.styleTextField(journeyDateField, false);
        ThemeManager.styleSecondaryButton(pickDateButton);
        ThemeManager.stylePrimaryButton(bookButton);
        updateFarePreview();

        // Cancellation tab controls
        ThemeManager.styleTextField(pnrSearchField, true);
        ThemeManager.styleSecondaryButton(fetchButton);
        detailsCard.setBackground(ThemeManager.bgInput());
        detailsCard.setBorder(new CompoundBorder(
            new LineBorder(ThemeManager.border(), 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        detailsSectionLabel.setForeground(ThemeManager.textPrimary());

        JLabel[] valueLabels = {
            cancelPnrValue, cancelPassengerValue, cancelTrainValue,
            cancelClassValue, cancelDateValue, cancelRouteValue, cancelFareValue
        };
        for (JLabel v : valueLabels) {
            v.setForeground(ThemeManager.textPrimary());
        }
        ThemeManager.styleDangerButton(confirmCancelButton, loadedPnr != null);

        // All Tickets tab controls
        ThemeManager.styleTextField(filterField, true);
        ticketsTable.setBackground(ThemeManager.bgCard());
        ticketsTable.setForeground(ThemeManager.textPrimary());
        ticketsTable.setGridColor(ThemeManager.border());
        ticketsTable.setSelectionBackground(ThemeManager.selectionBg());
        ticketsTable.setSelectionForeground(ThemeManager.textPrimary());

        JTableHeader header = ticketsTable.getTableHeader();
        header.setBackground(ThemeManager.tableHeader());
        header.setForeground(ThemeManager.textPrimary());

        tableScrollPane.getViewport().setBackground(ThemeManager.bgCard());
        tableScrollPane.setBorder(new LineBorder(ThemeManager.border(), 1, true));
        copyStatusLabel.setForeground(ThemeManager.textSecondary());
        ThemeManager.styleSecondaryButton(copyPnrButton);
        ThemeManager.styleSecondaryButton(refreshButton);

        repaint();
    }

    private void refreshTicketsTable() {
        ticketsTableModel.setRowCount(0);
        List<Reservation> all = ReservationService.getAllReservations();
        for (Reservation r : all) {
            ticketsTableModel.addRow(new Object[]{
                r.getPnr(),
                r.getPassengerName(),
                r.getTrainNumber() + " - " + r.getTrainName(),
                r.getClassType(),
                r.getJourneyDate(),
                r.getSource() + " -> " + r.getDestination(),
                r.getFare() > 0 ? "₹" + r.getFare() : "-"
            });
        }
    }

    private void handleCopyPnr() {
        int viewRow = ticketsTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Click a ticket row in the table first.",
                "No Ticket Selected",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int modelRow = ticketsTable.convertRowIndexToModel(viewRow);
        String pnr = ticketsTableModel.getValueAt(modelRow, 0).toString();

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(pnr), null);
        copyStatusLabel.setText("Copied PNR " + pnr + " to clipboard!");
        copyStatusLabel.setForeground(ThemeManager.accentGreen());
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldLabels.add(label);

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JLabel valueLabel) {
        JLabel keyLabel = new JLabel(labelText);
        keyLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldLabels.add(keyLabel);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        panel.add(keyLabel, gbc);

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(valueLabel, gbc);
    }

    private void updateTrainOptions() {
        String selectedTrain = (String) trainComboBox.getSelectedItem();
        if (selectedTrain == null) return;

        isUpdatingControls = true;
        try {
            String trainNumber = selectedTrain.split(" - ", 2)[0].trim();

            String[] stops = ReservationService.getRouteStops(trainNumber);
            boardingComboBox.removeAllItems();
            destinationComboBox.removeAllItems();
            for (String stop : stops) {
                boardingComboBox.addItem(stop);
                destinationComboBox.addItem(stop);
            }
            if (stops.length > 1) {
                boardingComboBox.setSelectedIndex(0);
                destinationComboBox.setSelectedIndex(stops.length - 1);
            }

            String[] classes = ReservationService.getTrainClasses(trainNumber);
            classComboBox.removeAllItems();
            for (String cls : classes) {
                classComboBox.addItem(cls);
            }
        } finally {
            isUpdatingControls = false;
        }

        updateFarePreview();
    }

    private void updateFarePreview() {
        String selectedTrain = (String) trainComboBox.getSelectedItem();
        String selectedClass = (String) classComboBox.getSelectedItem();
        int boardingIdx = boardingComboBox.getSelectedIndex();
        int destIdx = destinationComboBox.getSelectedIndex();

        if (selectedTrain == null || selectedClass == null || boardingIdx < 0 || destIdx < 0) {
            estimatedFareLabel.setText("₹0");
            return;
        }

        if (boardingIdx >= destIdx) {
            estimatedFareLabel.setText("Select valid route");
            estimatedFareLabel.setForeground(ThemeManager.danger());
            return;
        }

        String trainNumber = selectedTrain.split(" - ", 2)[0].trim();
        int fare = ReservationService.calculateFare(trainNumber, boardingIdx, destIdx, selectedClass);
        int stopsCount = destIdx - boardingIdx;
        estimatedFareLabel.setText("₹" + fare + "  (" + stopsCount + (stopsCount == 1 ? " stop)" : " stops)"));
        estimatedFareLabel.setForeground(ThemeManager.accentGreen());
    }

    private void handleBooking() {
        String passenger = passengerNameField.getText();
        String selectedTrain = (String) trainComboBox.getSelectedItem();
        String boarding = (String) boardingComboBox.getSelectedItem();
        String destination = (String) destinationComboBox.getSelectedItem();
        String classType = (String) classComboBox.getSelectedItem();
        String date = journeyDateField.getText();

        if (selectedTrain == null || boarding == null || destination == null || classType == null) {
            JOptionPane.showMessageDialog(this, "Please select a train, class, and route.", "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int boardingIdx = boardingComboBox.getSelectedIndex();
        int destIdx = destinationComboBox.getSelectedIndex();

        if (boardingIdx >= destIdx) {
            JOptionPane.showMessageDialog(
                this,
                "Destination station must come after the boarding station on this train's route.",
                "Invalid Route Selection",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String[] trainParts = selectedTrain.split(" - ", 2);
        String trainNumber = trainParts[0].trim();
        String trainName = trainParts[1].trim();
        int fare = ReservationService.calculateFare(trainNumber, boardingIdx, destIdx, classType);

        try {
            Reservation ticket = ReservationService.bookTicket(
                passenger, trainNumber, trainName, classType, date, boarding, destination, fare
            );

            refreshTicketsTable();

            String receipt = String.format(
                "Reservation Confirmed!\n\n" +
                "PNR Number:    %s\n" +
                "Passenger:     %s\n" +
                "Train:         %s - %s\n" +
                "Class:         %s\n" +
                "Journey Date:  %s\n" +
                "Route:         %s  ->  %s\n" +
                "Total Fare:    ₹%d",
                ticket.getPnr(),
                ticket.getPassengerName(),
                ticket.getTrainNumber(),
                ticket.getTrainName(),
                ticket.getClassType(),
                ticket.getJourneyDate(),
                ticket.getSource(),
                ticket.getDestination(),
                ticket.getFare()
            );

            JOptionPane.showMessageDialog(
                this,
                receipt,
                "Booking Receipt — PNR " + ticket.getPnr(),
                JOptionPane.INFORMATION_MESSAGE
            );

            passengerNameField.setText("");
            journeyDateField.setText("");
            classComboBox.setSelectedIndex(0);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Cannot Complete Booking",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void handleFetchPnr() {
        String pnrInput = pnrSearchField.getText().trim();

        if (pnrInput.isEmpty() || !pnrInput.matches("\\d+")) {
            clearCancellationDetails();
            JOptionPane.showMessageDialog(
                this,
                "Please enter a valid numeric PNR number.",
                "Invalid PNR",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Reservation ticket = ReservationService.findByPnr(pnrInput);
        if (ticket == null) {
            clearCancellationDetails();
            JOptionPane.showMessageDialog(
                this,
                "No active reservation found for PNR: " + pnrInput,
                "PNR Not Found",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        loadedPnr = ticket.getPnr();
        cancelPnrValue.setText(ticket.getPnr());
        cancelPassengerValue.setText(ticket.getPassengerName());
        cancelTrainValue.setText(ticket.getTrainNumber() + " - " + ticket.getTrainName());
        cancelClassValue.setText(ticket.getClassType());
        cancelDateValue.setText(ticket.getJourneyDate());
        cancelRouteValue.setText(ticket.getSource() + "  ->  " + ticket.getDestination());
        cancelFareValue.setText(ticket.getFare() > 0 ? "₹" + ticket.getFare() : "-");
        ThemeManager.styleDangerButton(confirmCancelButton, true);
    }

    private void handleCancelTicket() {
        if (loadedPnr == null) return;

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel PNR " + loadedPnr + " for " + cancelPassengerValue.getText() + "?\nThis action cannot be undone.",
            "Confirm Ticket Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            boolean deleted = ReservationService.cancelByPnr(loadedPnr);
            if (deleted) {
                refreshTicketsTable();
                JOptionPane.showMessageDialog(
                    this,
                    "Reservation PNR " + loadedPnr + " has been cancelled.",
                    "Ticket Cancelled",
                    JOptionPane.INFORMATION_MESSAGE
                );
                pnrSearchField.setText("");
                clearCancellationDetails();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Could not cancel reservation. It may have already been removed.",
                    "Cancellation Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void clearCancellationDetails() {
        loadedPnr = null;
        cancelPnrValue.setText("-");
        cancelPassengerValue.setText("-");
        cancelTrainValue.setText("-");
        cancelClassValue.setText("-");
        cancelDateValue.setText("-");
        cancelRouteValue.setText("-");
        cancelFareValue.setText("-");
        ThemeManager.styleDangerButton(confirmCancelButton, false);
    }
}