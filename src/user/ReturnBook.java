package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import system.SuspensionManager;
import system.SuspensionManager.SuspensionState;
import system.TableStyle;

public class ReturnBook extends JPanel{
	private static final long serialVersionUID = 1L;
	private int user_id;
	private JTable borrowedTable;
	private DefaultTableModel model;
	private Integer pendingReturnBookId;

	public ReturnBook(int user_id) {
		this.user_id = user_id;
		
		// Use BorderLayout and add significant bottom padding to avoid taskbar blocking
        super.setLayout(new BorderLayout());
        super.setBorder(new EmptyBorder(10, 10, 10, 10)); // 40px bottom padding
        super.setBackground(Color.WHITE);
        
        // Setup the table to show borrowed books
        String[] columns = {
                "Record ID", "Book ID", "Book Title", "Borrow Date", "Due Date"
        };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        // Create a table and add it to a scroll panel.
        borrowedTable = new JTable(model);
        borrowedTable.setFont(borrowedTable.getFont().deriveFont(18.0f));
        borrowedTable.getTableHeader().setFont(borrowedTable.getFont().deriveFont(18.0f));
        borrowedTable.setRowHeight(30);
        borrowedTable.getTableHeader().setResizingAllowed(false);
        TableStyle.applyUserStyle(borrowedTable);

        // Hide internal identifiers.
        TableColumnModel columnModel = borrowedTable.getColumnModel();
        for (int index : new int[] {0, 1}) {
            TableColumn column = columnModel.getColumn(index);
            column.setMinWidth(0);
            column.setMaxWidth(0);
            column.setPreferredWidth(0);
        }
        
        // Create a button panel at the BOTTOM CENTER
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        JButton btnReturn = new JButton("Confirm Return Selected Book");
        btnReturn.setFont(btnReturn.getFont().deriveFont(15.0f));
        buttonPanel.add(btnReturn);

        // Add components to the main panel
        this.add(new JScrollPane(borrowedTable), BorderLayout.CENTER);   // Table in the middle
        this.add(buttonPanel, BorderLayout.SOUTH);  // Button at bottom center

        // Return Button Action
        btnReturn.addActionListener(_ -> {
            int row = borrowedTable.getSelectedRow();
            if (row != -1) {
                confirmSelectedReturn();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book from the table first!", "Return Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Auto-refresh listener
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadActiveLoans();
                openPendingReturnBook();
            }
        });
    }

    public void loadActiveLoans() {
        try {
            String sql = "SELECT r.record_id, r.book_id, b.title, "
            			+ "r.borrow_date, r.due_date "
            			+ "FROM borrow_records r " 
            			+ "JOIN books b ON r.book_id = b.book_id " 
            			+ "WHERE r.user_id = ? AND r.return_date IS NULL "
            			+ "ORDER BY r.borrow_date ASC";
            PreparedStatement stmt = system.StartSystem.db.prepareStatement(sql);
            stmt.setString(1, String.valueOf(this.user_id));
            ResultSet rs = stmt.executeQuery();
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("record_id"),
                						rs.getInt("book_id"),
                						rs.getString("title"),
                						rs.getString("borrow_date"), 
                						rs.getString("due_date")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void openReturnBook(int bookId) {
        pendingReturnBookId = bookId;
        if (isShowing()) {
            loadActiveLoans();
            openPendingReturnBook();
        }
    }

    private void openPendingReturnBook() {
        if (pendingReturnBookId == null) {
            return;
        }

        int bookId = pendingReturnBookId;
        for (int modelRow = 0; modelRow < model.getRowCount(); modelRow++) {
            int rowBookId = Integer.parseInt(
                    model.getValueAt(modelRow, 1).toString());
            if (rowBookId == bookId) {
                int viewRow = borrowedTable.convertRowIndexToView(modelRow);
                borrowedTable.setRowSelectionInterval(viewRow, viewRow);
                borrowedTable.scrollRectToVisible(
                        borrowedTable.getCellRect(viewRow, 0, true));
                pendingReturnBookId = null;
                SwingUtilities.invokeLater(this::confirmSelectedReturn);
                return;
            }
        }
    }

    private void confirmSelectedReturn() {
        int viewRow = borrowedTable.getSelectedRow();
        if (viewRow == -1) {
            return;
        }

        int modelRow = borrowedTable.convertRowIndexToModel(viewRow);
        String recordId = model.getValueAt(modelRow, 0).toString();
        String bookTitle = model.getValueAt(modelRow, 2).toString();
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to return 《" + bookTitle + "》?",
                "Confirm Return",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            executeReturnAction(recordId);
        }
    }

    private void executeReturnAction(String recordId) {
        String infoSql =
                "SELECT r.book_id, r.due_date, b.title " +
                "FROM borrow_records r " +
                "JOIN books b ON r.book_id = b.book_id " +
                "WHERE r.record_id = ?";
        int bookId;
        String bookTitle;
        LocalDate dueDate;

        try (PreparedStatement infoStatement =
                system.StartSystem.db.prepareStatement(infoSql)) {
            infoStatement.setInt(1, Integer.parseInt(recordId));
            try (ResultSet resultSet = infoStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return;
                }
                bookId = resultSet.getInt("book_id");
                bookTitle = resultSet.getString("title");
                dueDate = resultSet.getTimestamp("due_date")
                        .toLocalDateTime()
                        .toLocalDate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            String sql = "UPDATE borrow_records SET return_date = NOW() WHERE record_id = ?";
            try (PreparedStatement pstmt =
                    system.StartSystem.db.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(recordId));
                if (pstmt.executeUpdate() <= 0) {
                    return;
                }
            }

            Reservation.markNextAvailable(bookId);
            loadActiveLoans();
            boolean returnedBookWasOverdue =
                    dueDate.isBefore(LocalDate.now());
            SuspensionState suspension =
                    SuspensionManager.updateAfterReturn(
                            user_id, returnedBookWasOverdue);
            if (suspension.hasOverdueBooks()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Book returned successfully!\n\n"
                                + "You still have overdue books.\n"
                                + "Your account remains suspended until all "
                                + "overdue books are returned.",
                        "Overdue Warning",
                        JOptionPane.WARNING_MESSAGE);
            } else if (returnedBookWasOverdue
                    && suspension.suspended()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Book returned successfully!\n\n"
                                + "All overdue books have now been returned.\n"
                                + "Your account will remain suspended until "
                                + suspension.suspendedUntil() + ".",
                        "Suspension Notice",
                        JOptionPane.WARNING_MESSAGE);
            } else if (returnedBookWasOverdue) {
                JOptionPane.showMessageDialog(
                        this,
                        "Book returned successfully!\n\n"
                                + "All overdue books have now been returned.\n"
                                + "Your suspension has been "
                                + "cancelled immediately.",
                        "Suspension Cancelled",
                        JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Successfully Returned!",
                        "Return Succeed",
                        JOptionPane.PLAIN_MESSAGE);
            }

            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame frame) {
                Review.showWriteReviewDialog(
                        frame, user_id, bookId, bookTitle);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

}
