package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;

import system.BorrowRecord;
import system.AppSettings;
import system.StartSystem;
import system.TableStyle;
import system.SuspensionManager;
import system.SuspensionManager.SuspensionState;

public class SearchBook extends JPanel {
    private static final long serialVersionUID = 1L;
    
    // Components
    private JTextField keywordField;
    private JTextField titleField;
    private JComboBox<String> authorBox;
    private JComboBox<String> subjectBox;
    private JComboBox<String> publishPlaceBox;
    private JComboBox<String> publisherBox;
    private JComboBox<String> publishYearBox;
    private JComboBox<String> sourceBox;
    private JTextField isbnField;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private TableColumn idColumn;
    private TableColumn[] allOptionalColumns;
    private final String[] columns = {"ID", "Title", "Author", "Subject", "Publisher", "Year", "Source", "ISBN"};
    
    private static boolean boxIsFiltering = false;
    private static boolean boxIsComposing = false;
    
    // Store the current logged-in user session ID
    private int currentUserId;

    // Updated constructor to accept userId
    public SearchBook(int userId) {
        this.currentUserId = userId;
        
        super.setLayout(new BorderLayout(10, 10));
        super.setBorder(new EmptyBorder(10, 10, 10, 10));
        super.setBackground(Color.WHITE);

        // ==========================================
        // 1. TOP PANEL (Search Forms)
        // ==========================================
        JPanel topSearchPanel = new JPanel(new GridBagLayout());
        topSearchPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        keywordField = new JTextField(20);
        titleField = new JTextField();
        authorBox = new JComboBox<String>();
        subjectBox = new JComboBox<String>();
        publishPlaceBox = new JComboBox<String>();
        publisherBox = new JComboBox<String>();
        publishYearBox = new JComboBox<String>();
        sourceBox = new JComboBox<String>();
        isbnField = new JTextField();

        setPlainBox(authorBox, getItems("authors"));
        setSearchBox(subjectBox, getItems("subjects"));
        setPlainBox(publishPlaceBox, getItems("publish_place"));
        setPlainBox(publisherBox, getItems("publisher"));
        setPlainBox(publishYearBox, getItems("publish_year"));
        setPlainBox(sourceBox, getItems("source"));

        Font labelFont = new Font("SansSerif", Font.BOLD, 14);
        Font inputFont = new Font("SansSerif", Font.PLAIN, 14);
        keywordField.setFont(inputFont);

        // -- Quick Search Section --
        JPanel quickPanel = new JPanel(new GridBagLayout());
        quickPanel.setBackground(Color.WHITE);
        quickPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Quick Search", TitledBorder.LEFT, TitledBorder.TOP, labelFont));
        
        GridBagConstraints qc = new GridBagConstraints();
        qc.insets = new Insets(5, 5, 5, 5);
        qc.anchor = GridBagConstraints.WEST;
        qc.weightx = 0;
        quickPanel.add(new JLabel("Keyword:"), qc);
        qc.gridx = 1;
        qc.weightx = 1.0;
        qc.fill = GridBagConstraints.HORIZONTAL;
        quickPanel.add(keywordField, qc);
        qc.gridx = 2;
        qc.weightx = 0;
        qc.fill = GridBagConstraints.NONE;
        JButton quickBtn = new JButton("Search");
        quickBtn.addActionListener(_ -> performQuickSearch());
        quickPanel.add(quickBtn, qc);

        // -- Precise Search Section --
        JPanel precisePanel = new JPanel(new GridBagLayout());
        precisePanel.setBackground(Color.WHITE);
        precisePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Precise Search", TitledBorder.LEFT, TitledBorder.TOP, labelFont));
        
        GridBagConstraints pc = new GridBagConstraints();
        pc.insets = new Insets(5, 5, 5, 5);
        pc.fill = GridBagConstraints.HORIZONTAL;
        
        addRow(precisePanel, pc, 0, new JLabel("Book Title:"), titleField, new JLabel("Author:"), authorBox);
        addRow(precisePanel, pc, 1, new JLabel("Subject:"), subjectBox, new JLabel("Publish Place:"), publishPlaceBox);
        addRow(precisePanel, pc, 2, new JLabel("Publisher:"), publisherBox, new JLabel("Publish Year:"), publishYearBox);
        addRow(precisePanel, pc, 3, new JLabel("Source:"), sourceBox, new JLabel("ISBN:"), isbnField);
        
        pc.gridx = 3;
        pc.gridy = 4;
        pc.weightx = 0;
        pc.fill = GridBagConstraints.NONE;
        pc.anchor = GridBagConstraints.EAST;
        JPanel preciseButtonPanel =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        preciseButtonPanel.setBackground(Color.WHITE);
        JButton clearPreciseBtn = new JButton("Clear");
        clearPreciseBtn.addActionListener(_ -> clearPreciseSearch());
        JButton preciseBtn = new JButton("Search");
        preciseBtn.addActionListener(_ -> performPreciseSearch());
        preciseButtonPanel.add(clearPreciseBtn);
        preciseButtonPanel.add(preciseBtn);
        precisePanel.add(preciseButtonPanel, pc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        topSearchPanel.add(quickPanel, gbc);
        gbc.gridy = 1;
        topSearchPanel.add(precisePanel, gbc);

        super.add(topSearchPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. CENTER PANEL (Table and Action Bar)
        // ==========================================
        JPanel centerPanel = new JPanel(new BorderLayout(0, 5));
        centerPanel.setBackground(Color.WHITE);

        // 【更新】移到表格正上方的動作列
        JPanel tableActionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        tableActionBar.setBackground(Color.WHITE);
        
        JButton sendToBorrowBtn = new JButton("Borrow Selected Book");
        sendToBorrowBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        sendToBorrowBtn.setFocusPainted(false);
        
        sendToBorrowBtn.addActionListener(_ -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a book from the table first.",
                        "Borrow Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int modelRow = bookTable.convertRowIndexToModel(selectedRow);
            int bookId = Integer.parseInt(
                    bookTable.getModel().getValueAt(modelRow, 0).toString());
            String title =
                    bookTable.getModel().getValueAt(modelRow, 1).toString();
            showBorrowDialog(bookId, title);
        });
        
        tableActionBar.add(sendToBorrowBtn);

        JButton reserveButton = new JButton("Reserve Selected Book");
        reserveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        reserveButton.setFocusPainted(false);
        reserveButton.addActionListener(_ -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a book from the table first.",
                        "Reserve Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int modelRow = bookTable.convertRowIndexToModel(selectedRow);
            int bookId = Integer.parseInt(
                    bookTable.getModel().getValueAt(modelRow, 0).toString());
            String title =
                    bookTable.getModel().getValueAt(modelRow, 1).toString();
            Reservation.reserveBook(currentUserId, bookId, title, this);
        });
        tableActionBar.add(reserveButton);
        centerPanel.add(tableActionBar, BorderLayout.NORTH);

        // Setup the table
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {return false;}
        };
        
        bookTable = new JTable(tableModel);
        bookTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        bookTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        bookTable.setRowHeight(25);
        bookTable.getTableHeader().setResizingAllowed(false);
        TableStyle.applyUserStyle(bookTable);

        TableColumnModel columnModel = bookTable.getColumnModel();
        idColumn = columnModel.getColumn(0);
        allOptionalColumns = new TableColumn[5];
        for (int i = 0; i < 5; i++) {
            allOptionalColumns[i] = columnModel.getColumn(i + 3);
        }
        refreshVisibleColumns();
        
        bookTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1) {
                    int row = bookTable.getSelectedRow();
                    int model_row = bookTable.convertRowIndexToModel(row);
                    String bookId = bookTable.getModel().getValueAt(model_row, 0).toString();
                    new BookInfo(Integer.valueOf(bookId));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        super.add(centerPanel, BorderLayout.CENTER);
        
        performQuickSearch();   
    }
    
    // Methods
    public void receiveGlobalSearch(String keyword) {
        if (this.keywordField != null) {
            this.keywordField.setText(keyword);
            performQuickSearch();
        }
    }

    public void openBorrowDialog(int bookId, String bookTitle) {
        keywordField.setText(bookTitle);
        performQuickSearch();

        for (int modelRow = 0; modelRow < tableModel.getRowCount(); modelRow++) {
            int rowBookId = Integer.parseInt(
                    tableModel.getValueAt(modelRow, 0).toString());
            if (rowBookId == bookId) {
                int viewRow = bookTable.convertRowIndexToView(modelRow);
                bookTable.setRowSelectionInterval(viewRow, viewRow);
                bookTable.scrollRectToVisible(
                        bookTable.getCellRect(viewRow, 0, true));
                break;
            }
        }

        SwingUtilities.invokeLater(() ->
                showBorrowDialog(bookId, bookTitle));
    }

    private void showBorrowDialog(int bookId, String bookTitle) {
        SuspensionState suspension = getSuspensionState(currentUserId);
        if (suspension != null && suspension.suspended()) {
            String message;
            if (suspension.hasOverdueBooks()) {
                message = "Your account is suspended because you "
                        + "have overdue books.\nPlease return all "
                        + "overdue books first.";
            } else if (suspension.suspendedUntil() != null) {
                message = "Your account is suspended until "
                        + suspension.suspendedUntil()
                        + " after returning all overdue books.\n"
                        + "If you want to end the suspension early, "
                        + "please contact the administrator.";
            } else {
                message = "Your account is suspended. "
                        + "Please contact the administrator.";
            }
            JOptionPane.showMessageDialog(
                    this, message, "Borrow Failed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!canStartBorrow(bookId, bookTitle)) {
            return;
        }

        int acceptableBorrowDays = AppSettings.get().maxBorrowDays(
                getLevel(currentUserId));
        String input = JOptionPane.showInputDialog(
                this,
                "You are borrowing: 《" + bookTitle
                        + "》\nEnter borrow days (1-"
                        + acceptableBorrowDays + "):",
                "Confirm Borrow",
                JOptionPane.PLAIN_MESSAGE);

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        try {
            int days = Integer.parseInt(input.trim());
            if (days < 1 || days > acceptableBorrowDays) {
                JOptionPane.showMessageDialog(
                        this,
                        "Days must be between 1 and "
                                + acceptableBorrowDays + ".",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            executeBorrowAction(
                    String.valueOf(bookId), bookTitle, days);
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid integer number.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean canStartBorrow(int bookId, String bookTitle) {
        String ownLoanSql =
                "SELECT COUNT(*) FROM borrow_records " +
                "WHERE user_id = ? AND book_id = ? AND return_date IS NULL";
        try (PreparedStatement statement =
                StartSystem.db.prepareStatement(ownLoanSql)) {
            statement.setInt(1, currentUserId);
            statement.setInt(2, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "You already borrowed " + bookTitle,
                            "Borrow Failed",
                            JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }

            if (hasReachedBorrowLimit(bookTitle)) {
                return false;
            }

            String reservationRestriction =
                    Reservation.getBorrowRestriction(currentUserId, bookId);
            if (reservationRestriction != null) {
                showBorrowRestriction(
                        bookId, bookTitle, reservationRestriction);
                return false;
            }
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to check whether the book can be borrowed.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean hasReachedBorrowLimit(String bookTitle)
            throws SQLException {
        String roleLevel = getLevel(currentUserId);
        int maximum = AppSettings.get().maxBorrowBooks(roleLevel);
        String sql =
                "SELECT COUNT(*) FROM borrow_records " +
                "WHERE user_id = ? AND return_date IS NULL";
        try (PreparedStatement statement =
                StartSystem.db.prepareStatement(sql)) {
            statement.setInt(1, currentUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) >= maximum) {
                    JOptionPane.showMessageDialog(
                            this,
                            "You have reached the maximum of "
                                    + maximum + " borrowed books.\n"
                                    + "Return a book before borrowing 《"
                                    + bookTitle + "》.",
                            "Borrow Limit Reached",
                            JOptionPane.WARNING_MESSAGE);
                    return true;
                }
            }
        }
        return false;
    }

    private void showBorrowRestriction(
            int bookId, String bookTitle, String restriction) {
        if (restriction.contains("currently borrowed")) {
            Object[] options = {"Reserve", "Cancel"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "This book is currently borrowed.\n"
                            + "Would you like to reserve 《"
                            + bookTitle + "》?",
                    "Book Is Borrowed",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (choice == 0) {
                Reservation.reserveBook(
                        currentUserId, bookId, bookTitle, this);
            }
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                restriction,
                "Borrow Failed",
                JOptionPane.PLAIN_MESSAGE);
    }
    
    private static List<String> getItems(String itemName) {
        String sql;
        switch (itemName) {
        case "publish_place" :
            sql = "SELECT publisher FROM books";
            break;
        default:
            sql = "SELECT " + itemName + " FROM books";
            break;
        }
        
        Set<String> itemSet = new HashSet<String>();
        try (Statement stmt = system.StartSystem.db.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String items = rs.getString(1);
                if (items != null) {
                    String[] arr;
                    switch (itemName) {
                    case "authors":
                    case "subjects":
                        arr = items.split(",\\s*");
                        for (String s : arr) {
                            itemSet.add(s.trim());
                        }
                        break;
                    case "publish_place":                       
                        arr = items.split("\\s*:\\s*");
                        itemSet.add(arr[0]);
                        break;
                    case "publisher":
                        arr = items.split("\\s*:\\s*");
                        itemSet.add(arr[arr.length - 1]);
                        break;
                    case "publish_year":
                    default:
                        itemSet.add(items);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<String> itemList = new ArrayList<String>(itemSet);
        itemList.sort(String.CASE_INSENSITIVE_ORDER);
        return itemList;
    }
    
    private static void addRow(JPanel p, GridBagConstraints gbc, int row, JLabel l1, JComponent c1, JLabel l2, JComponent c2) {
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.weightx = 0; p.add(l1, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; p.add(c1, gbc);
        gbc.gridx = 2; gbc.weightx = 0; p.add(l2, gbc);
        gbc.gridx = 3; gbc.weightx = 1.0; p.add(c2, gbc);
    }
    
    private static void setPlainBox(JComboBox<String> box, List<String> allItems) {
        box.setEditable(true);
        box.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        JTextComponent editor = (JTextComponent) box.getEditor().getEditorComponent();
        
        Runnable updateModel = () -> {
            String filter = editor.getText();
            int caretPos = editor.getCaretPosition();
            DefaultComboBoxModel<String> newModel = new DefaultComboBoxModel<>();
            for (String s : allItems) { newModel.addElement(s); }
            box.setModel(newModel);
            editor.setText(filter);
            try { editor.setCaretPosition(caretPos); } catch (Exception ex) { editor.setCaretPosition(filter.length()); }
        };
        
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                boxIsFiltering = true; updateModel.run(); boxIsFiltering = false; box.showPopup();
            }
        });
        
        box.addActionListener(_ -> {
            if (!boxIsFiltering) {
                Object selectedItem = box.getSelectedItem();
                if (selectedItem != null) {
                    boxIsFiltering = true;
                    String selectedString = selectedItem.toString();
                    editor.setText(selectedString);
                    box.hidePopup();
                    SwingUtilities.invokeLater(() -> {
                        try { editor.setCaretPosition(selectedString.length()); } 
                        catch (IllegalArgumentException ex) { editor.setCaretPosition(editor.getText().length()); }
                    });
                    boxIsFiltering = false;
                }
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { e.consume(); }
        });
    }
    
    private static void setSearchBox(JComboBox<String> box, List<String> allItems) {
        box.setEditable(true);
        box.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        JTextComponent editor = (JTextComponent) box.getEditor().getEditorComponent();

        Runnable updateModel = () -> {
            String filter = editor.getText();
            int caretPos = editor.getCaretPosition(); 
            DefaultComboBoxModel<String> newModel = new DefaultComboBoxModel<>();
            for (String s : allItems) {
                if (filter.isEmpty() || s.toLowerCase().contains(filter.toLowerCase())) {
                    newModel.addElement(s);
                }
            }
            box.setModel(newModel);
            editor.setText(filter);
            try { editor.setCaretPosition(caretPos); } catch (Exception ex) { editor.setCaretPosition(filter.length()); }
            
            boolean isExactMatch = false;
            if (newModel.getSize() == 1 && newModel.getElementAt(0).equalsIgnoreCase(filter)) isExactMatch = true;
            if (editor.hasFocus() && newModel.getSize() > 0 && !isExactMatch) { box.setPopupVisible(true); } 
            else { box.hidePopup(); }
        };
        
        editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {update();}
            public void removeUpdate(DocumentEvent e) {update();}
            public void changedUpdate(DocumentEvent e) {update();}
            private void update() {
                if (boxIsFiltering || boxIsComposing) return;
                SwingUtilities.invokeLater(() -> {
                    boxIsFiltering = true; updateModel.run(); boxIsFiltering = false; 
                });
            }
        });
        
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (editor.getText().isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        boxIsFiltering = true; updateModel.run(); boxIsFiltering = false; box.showPopup();
                    });
                }
            }
        });
        
        box.addActionListener(_ -> {
            if (!boxIsFiltering) {
                Object selectedItem = box.getSelectedItem();
                if (selectedItem != null) {
                    boxIsFiltering = true;
                    String selectedString = selectedItem.toString();
                    editor.setText(selectedString);
                    box.hidePopup();
                    SwingUtilities.invokeLater(() -> {
                        try { editor.setCaretPosition(selectedString.length()); } 
                        catch (IllegalArgumentException ex) { editor.setCaretPosition(editor.getText().length()); }
                    });
                    boxIsFiltering = false;
                }
            }
        });
        
        editor.addInputMethodListener(new InputMethodListener() {
            @Override
            public void inputMethodTextChanged(InputMethodEvent event) {
                if (event.getCommittedCharacterCount() == 0 && event.getText() != null) {
                    boxIsComposing = true; box.hidePopup();
                } else {
                    boxIsComposing = false;
                    SwingUtilities.invokeLater(() -> {
                        boxIsFiltering = true; updateModel.run(); boxIsFiltering = false;
                    });
                }
            }
            @Override
            public void caretPositionChanged(InputMethodEvent event) {}
        });
    }
    
    private void performQuickSearch() {
        String keyword = keywordField.getText().trim();
        showAllColumns();
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM books WHERE 1=1 AND " + 
                     "(title LIKE ? OR authors LIKE ? OR subjects LIKE ? OR " +
                     "publisher LIKE ? OR publish_year LIKE ? OR source LIKE ? OR isbn LIKE ?)";
        String searchPattern = "%" + keyword + "%";
        
        try (PreparedStatement pstmt = StartSystem.db.prepareStatement(sql)){
            for (int i = 1; i <= 7; i++) { pstmt.setString(i, searchPattern); }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getString("book_id"), rs.getString("title"), rs.getString("authors"), 
                        rs.getString("subjects"), rs.getString("publisher"), rs.getString("publish_year"), 
                        rs.getString("source"), rs.getString("isbn")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void performPreciseSearch() {
        refreshVisibleColumns();
        tableModel.setRowCount(0);
        StringBuilder sql = new StringBuilder("SELECT book_id, title, authors, subjects, publisher, publish_year, source, isbn FROM books WHERE 1=1 ");
        List<Object> params = new ArrayList<Object>();
        
        addQueryParam(sql, params, "title", titleField.getText());
        addQueryParam(sql, params, "authors", comboText(authorBox));
        addQueryParam(sql, params, "subjects", comboText(subjectBox));
        
        if (hasValue(publishPlaceBox) || hasValue(publisherBox)) {
            String place = comboText(publishPlaceBox);
            String pub = comboText(publisherBox);
            if (!place.isEmpty() && !pub.isEmpty()) {
                addQueryParam(sql, params, "publisher", place + " : " + pub);
            } else {
                addQueryParam(sql, params, "publisher", place);
                addQueryParam(sql, params, "publisher", pub);
            }
        }
        addQueryParam(sql, params, "publish_year", comboText(publishYearBox));
        addQueryParam(sql, params, "source", comboText(sourceBox));
        addQueryParam(sql, params, "isbn", isbnField.getText());
        
        try (PreparedStatement pstmt = StartSystem.db.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) { pstmt.setObject(i + 1, "%" + params.get(i) + "%"); }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getString("book_id"), rs.getString("title"), rs.getString("authors"), 
                        rs.getString("subjects"), rs.getString("publisher"), rs.getString("publish_year"), 
                        rs.getString("source"), rs.getString("isbn")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void clearPreciseSearch() {
        titleField.setText("");
        isbnField.setText("");
        clearComboBox(authorBox);
        clearComboBox(subjectBox);
        clearComboBox(publishPlaceBox);
        clearComboBox(publisherBox);
        clearComboBox(publishYearBox);
        clearComboBox(sourceBox);
        keywordField.setText("");
        performQuickSearch();
    }

    private void clearComboBox(JComboBox<String> box) {
        box.setSelectedItem("");
        JTextComponent editor =
                (JTextComponent) box.getEditor().getEditorComponent();
        editor.setText("");
    }
    
    private void addQueryParam(StringBuilder sql, List<Object> params, String column, String value) {
        if (value != null && !value.trim().isEmpty() && !value.equals(" : ")) { 
            sql.append(" AND ").append(column).append(" LIKE ?");
            params.add(value.trim());
        }
    }
    
    private void refreshVisibleColumns() {
        bookTable.removeColumn(idColumn);
        for (TableColumn col : allOptionalColumns) {
            try { bookTable.removeColumn(col); } catch (IllegalArgumentException e) { continue; }
        }
        if (hasValue(subjectBox))                                   bookTable.addColumn(allOptionalColumns[0]);
        if (hasValue(publishPlaceBox) || hasValue(publisherBox))    bookTable.addColumn(allOptionalColumns[1]);
        if (hasValue(publishYearBox))                               bookTable.addColumn(allOptionalColumns[2]);
        if (hasValue(sourceBox))                                    bookTable.addColumn(allOptionalColumns[3]);
        if (!isbnField.getText().trim().isEmpty())                  bookTable.addColumn(allOptionalColumns[4]);
    }

    private boolean hasValue(JComboBox<String> box) {
        return !comboText(box).isEmpty();
    }

    private String comboText(JComboBox<String> box) {
        JTextComponent editor =
                (JTextComponent) box.getEditor().getEditorComponent();
        return editor.getText().trim();
    }
    
    private void showAllColumns() {
        for (TableColumn column : allOptionalColumns) {
            try { bookTable.removeColumn(column); } catch (Exception e) { continue; }
        }
        for (TableColumn column : allOptionalColumns) { bookTable.addColumn(column); }
    }
    
    private String getLevel(int user_id) {
    	String sql = "SELECT role_level FROM users WHERE user_id = ?";
    	try (PreparedStatement pstmt = StartSystem.db.prepareStatement(sql)) {
    		pstmt.setString(1, String.valueOf(user_id));
    		
    		ResultSet rs = pstmt.executeQuery();
    		if (rs.next()) {
    			return rs.getString(1);
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return null;
    }

    private SuspensionState getSuspensionState(int userId) {
        try {
            return SuspensionManager.refreshUserStatus(userId);
        } catch (SQLException exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to check your account status.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return new SuspensionState(true, null, false);
        }
    }
    
    private void executeBorrowAction(String bookId, String bookTitle, int borrowDays) {
        try {
            int selectedBookId = Integer.parseInt(bookId);
            if (hasReachedBorrowLimit(bookTitle)) {
                return;
            }
            String reservationRestriction =
                    Reservation.getBorrowRestriction(
                            currentUserId, selectedBookId);
            if (reservationRestriction != null) {
                showBorrowRestriction(
                        selectedBookId, bookTitle, reservationRestriction);
                return;
            }

            // 1. First, check if the user already has an active loan for THIS specific book
            String checkSql = "SELECT COUNT(*) FROM borrow_records " + 
                              "WHERE user_id = ? AND book_id = ? AND return_date IS NULL";
            PreparedStatement checkPstmt = StartSystem.db.prepareStatement(checkSql);
            checkPstmt.setString(1, String.valueOf(currentUserId));
            checkPstmt.setString(2, bookId);
            
            ResultSet rs = checkPstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // If count > 0, it means the user already has this book
                JOptionPane.showMessageDialog(
                        this,
                        "You already borrowed " + bookTitle,
                        "Borrow Failed",
                        JOptionPane.WARNING_MESSAGE);
                return; // Stop the execution here
            }

            // 2. If not borrowed, proceed with the INSERT
            BorrowRecord newRecord = new BorrowRecord(currentUserId, Integer.valueOf(bookId), borrowDays);
            try {
            	newRecord.insert();
                Reservation.completeReservation(
                        currentUserId, selectedBookId);
            	JOptionPane.showMessageDialog(
            			this,
            			"<html>Success! You borrowed: <b>《"
            					+ escapeHtml(bookTitle)
            					+ "》</b><br>Please return before "
            					+ newRecord.getDue_date() + ".</html>",
            			"Borrow Succeed",
            			JOptionPane.PLAIN_MESSAGE);
            	
            } catch (Exception e) {
            	e.printStackTrace();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Failed During Creating Borrow Record", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
