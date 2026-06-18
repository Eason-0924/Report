package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import system.BorrowRecord;
import system.AppSettings;
import system.StartSystem;
import system.TableStyle;

public class BorrowBook extends JPanel {
	private static final long serialVersionUID = 1L;
	private int user_id;
	private JTable bookTable;
	private DefaultTableModel model;
	private JLabel borrowDaysLabel;
	private JTextField borrowDaysField;
	
	public BorrowBook(int user_id) {
		this.user_id = user_id;
		super.setLayout(new BorderLayout());
        super.setBorder(new EmptyBorder(10, 10, 10, 10)); // 40px bottom padding
        super.setBackground(Color.WHITE);

        String[] columns = {"ID", "Title", "Author", "Publisher", "Year", "ISBN"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        bookTable = new JTable(model);
        bookTable.setFont(bookTable.getFont().deriveFont(15.0f));
        bookTable.getTableHeader().setFont(bookTable.getFont().deriveFont(15.0f));
        bookTable.setRowHeight(25);
        bookTable.getTableHeader().setResizingAllowed(false);
        TableStyle.applyUserStyle(bookTable);
        loadBooksData();

        bookTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        TableColumnModel columnModel = bookTable.getColumnModel();
        
        TableColumn column0 = columnModel.getColumn(0);
        column0.setMinWidth(0);
        column0.setMaxWidth(0);
        column0.setPreferredWidth(0);
        
        TableColumn columnPublisher = columnModel.getColumn(3);
        columnPublisher.setMinWidth(180);
        columnPublisher.setMaxWidth(200);
        columnPublisher.setPreferredWidth(190);
        
        TableColumn columnYear = columnModel.getColumn(4);
        columnYear.setMinWidth(80);
        columnYear.setMaxWidth(100);
        columnYear.setPreferredWidth(90);
        
        JPanel buttomPanel = new JPanel(new GridBagLayout());
        buttomPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        
        int acceptableBorrowDays = AppSettings.get().maxBorrowDays(
        		getLevel(user_id));
        borrowDaysLabel = new JLabel("Enter the borrow days (no more than " + acceptableBorrowDays + " days): ");
        borrowDaysLabel.setFont(borrowDaysLabel.getFont().deriveFont(15.0f));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttomPanel.add(borrowDaysLabel, gbc);
        
        borrowDaysField = new JTextField();
        borrowDaysField.setPreferredSize(new Dimension(100, 1));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        buttomPanel.add(borrowDaysField, gbc);
        
        JButton btnBorrow = new JButton("Borrow Selected Book");
        btnBorrow.setFont(btnBorrow.getFont().deriveFont(15.0f));
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        buttomPanel.add(btnBorrow, gbc);

        this.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        this.add(buttomPanel, BorderLayout.SOUTH); // Button at bottom center

        btnBorrow.addActionListener(_ -> {
            int row = bookTable.getSelectedRow();
            int model_row = bookTable.convertRowIndexToModel(row);
            String borrowDayString = borrowDaysField.getText();
            if (row != -1) {
            	if (borrowDayString.equals("") || borrowDayString.matches(".*\\s.*")) {
            		JOptionPane.showMessageDialog(this, "Please enter the borrow days!", "Borrow Failed", JOptionPane.ERROR_MESSAGE);
            	} else {
            		int days = Integer.valueOf(borrowDayString);
            		if (days > acceptableBorrowDays) {
            			JOptionPane.showMessageDialog(this, "You can't borrow for more than " + acceptableBorrowDays + " days.", "Borrow Failed", JOptionPane.ERROR_MESSAGE);
            		} else {
            			executeBorrowAction(bookTable.getValueAt(model_row, 0).toString(), 
            								bookTable.getValueAt(row, 1).toString(), 
            								Integer.valueOf(borrowDayString));
            		}
            	}
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book first!", "Borrow Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        this.addComponentListener(new ComponentAdapter() {
        	@Override
        	public void componentShown(ComponentEvent e) {
        		loadBooksData();
        	}
		});
    }

    public void loadBooksData() {
        try {
            String sql = "SELECT * FROM books WHERE book_id NOT IN (SELECT book_id FROM borrow_records WHERE return_date IS NULL)";
            Statement stmt = system.StartSystem.db.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("book_id"), 
                						rs.getString("title"), 
                						rs.getString("authors"), 
                						rs.getString("publisher"), 
                						rs.getString("publish_year"), 
                						rs.getString("isbn")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void executeBorrowAction(String bookId, String bookTitle, int borrowDays) {
        try {
            int maximum = AppSettings.get().maxBorrowBooks(getLevel(user_id));
            String limitSql =
                    "SELECT COUNT(*) FROM borrow_records " +
                    "WHERE user_id = ? AND return_date IS NULL";
            try (PreparedStatement limitStatement =
                    StartSystem.db.prepareStatement(limitSql)) {
                limitStatement.setInt(1, user_id);
                try (ResultSet resultSet = limitStatement.executeQuery()) {
                    if (resultSet.next()
                            && resultSet.getInt(1) >= maximum) {
                        JOptionPane.showMessageDialog(
                                this,
                                "You have reached the maximum of "
                                        + maximum + " borrowed books.",
                                "Borrow Limit Reached",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            // 1. First, check if the user already has an active loan for THIS specific book
            String checkSql = "SELECT COUNT(*) FROM borrow_records " + 
                              "WHERE user_id = ? AND book_id = ? AND return_date IS NULL";
            PreparedStatement checkPstmt = StartSystem.db.prepareStatement(checkSql);
            checkPstmt.setString(1, String.valueOf(user_id));
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
            BorrowRecord newRecord = new BorrowRecord(user_id, Integer.valueOf(bookId), borrowDays);
            try {
            	newRecord.insert();
            	loadBooksData(); // Refresh the list
            	JOptionPane.showMessageDialog(this, "Success! You borrowed: " + bookTitle, "Borrow Succeed", JOptionPane.PLAIN_MESSAGE);
            	
            } catch (Exception e) {
            	e.printStackTrace();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Failed During Creating Borrow Record", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
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
    public void setTargetBook(String isbn) {
       
        if (this.borrowDaysField != null) {
            this.borrowDaysField.setText(isbn);
        }
    }
}
