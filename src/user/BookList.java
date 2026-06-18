package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import system.StartSystem;
import system.TableStyle;

public class BookList extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JTable recordTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;

    public BookList() {
        super.setLayout(new BorderLayout());
        super.setBorder(new EmptyBorder(5, 5, 5, 5));
        super.setBackground(Color.WHITE);

        // Set uniform font sizes
        Font mainFont = new Font("SansSerif", Font.PLAIN, 15);
        Font boldFont = new Font("SansSerif", Font.BOLD, 15);

        // 1. Top Panel for the search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        JLabel searchLabel = new JLabel("Quick Search: ");
        searchLabel.setFont(boldFont); 
        
        searchField = new JTextField(20);
        searchField.setFont(mainFont); 
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        super.add(searchPanel, BorderLayout.NORTH);

        // 2. Setup the table and model (Without ID column)
        String[] columns = {"Title", "Author", "Publisher", "ISBN"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }
        };
        
        recordTable = new JTable(tableModel);
        
        // Enlarge table content, header, and row height
        recordTable.setFont(mainFont);
        recordTable.getTableHeader().setFont(boldFont);
        recordTable.setRowHeight(25);
        TableStyle.applyUserStyle(recordTable);
        
        // 3. Setup the RowSorter for real-time filtering
        rowSorter = new TableRowSorter<>(tableModel);
        recordTable.setRowSorter(rowSorter);
        
        JScrollPane scrollPane = new JScrollPane(recordTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        super.add(centerPanel, BorderLayout.CENTER);

        // 4. Add DocumentListener to trigger filtering when typing
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }

            private void filterTable() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null); 
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        // 5. Load all books from the database immediately when initialized
        loadInitialData();
    }

    private void loadInitialData() {
        // Fetch data matching the 4 columns
        String sql = "SELECT title, authors, publisher, isbn FROM books";
        
        try (PreparedStatement pstmt = StartSystem.db.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                    rs.getString("title"),
                    rs.getString("authors"),
                    rs.getString("publisher"),
                    rs.getString("isbn")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
