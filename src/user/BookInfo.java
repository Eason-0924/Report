package user;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import system.Book;
import system.StartSystem;
import system.TableStyle;

public class BookInfo extends JFrame{

	private static final long serialVersionUID = 1L;
	private Book book;
	private JPanel contentPanel;
	private JPanel infoPanel;
	private JTable recordTable;
	private DefaultTableModel model;
	private static final int panelSize_x = 810;
	private static final int panelSize_y = 700;
	
	public BookInfo(int book_id) {
		super.setTitle("Book Information");
		super.setBackground(Color.WHITE);
		super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Get the information of the book
		book = getBook(book_id);
		
		// Create content panel
		contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setPreferredSize(new Dimension(panelSize_x, panelSize_y));
		contentPanel.setBackground(Color.WHITE);
		
		// Create info panel
		infoPanel = new JPanel(new GridBagLayout());
		infoPanel.setBackground(Color.WHITE);
		JLabel title = new JLabel("Title: ");
		JLabel titleInfo = new JLabel(book.getTitle());
		JLabel authors = new JLabel("Authors: ");
		JLabel authorsInfo = new JLabel(String.join(", ", book.getAuthors()));
		JLabel subjects = new JLabel("Subjects: ");
		JLabel subjectsInfo = new JLabel(String.join(", ", book.getSubjects()));
		JLabel publisher = new JLabel("Publisher: ");
		JLabel publisherInfo = new JLabel(book.getPublisher());
		JLabel publishYear = new JLabel("Publish Year: ");
		JLabel publishYearInfo = new JLabel(book.getPublish_year());
		JLabel edition = new JLabel("Edition: ");
		JLabel editionInfo = new JLabel(book.getEdition());
		JLabel format = new JLabel("Format: ");
		JLabel formatInfo = new JLabel(book.getFormate_desc());
		JLabel source = new JLabel("Source: ");
		JLabel sourceInfo = new JLabel(book.getSource());
		JLabel isbn = new JLabel("ISBN: ");
		JLabel isbnInfo = new JLabel(String.join(", ", book.getIsbn()));
		JLabel note = new JLabel("Note: ");
		JLabel noteInfo = new JLabel(book.getNote());
		
		for (JLabel l : new JLabel[] {title, titleInfo, authors, authorsInfo, subjects, subjectsInfo, 
									  publisher, publisherInfo, publishYear, publishYearInfo, edition, editionInfo, 
									  format, formatInfo, source, sourceInfo, isbn, isbnInfo, note, noteInfo}) {
			l.setFont(l.getFont().deriveFont(15.f));
		}
		
		// Add labels to info panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(3, 3, 3, 5);
		gbc.gridwidth = 1;
		gbc.weightx = 0.25;
		gbc.weighty = 1.0;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		infoPanel.add(title, gbc);
		gbc.gridx = 1;
		infoPanel.add(titleInfo, gbc);
		gbc.gridx = 2;
		infoPanel.add(authors, gbc);
		gbc.gridx = 3;
		infoPanel.add(authorsInfo, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		infoPanel.add(subjects, gbc);
		gbc.gridx = 1;
		infoPanel.add(subjectsInfo, gbc);
		gbc.gridx = 2;
		infoPanel.add(publisher, gbc);
		gbc.gridx = 3;
		infoPanel.add(publisherInfo, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		infoPanel.add(publishYear, gbc);
		gbc.gridx = 1;
		infoPanel.add(publishYearInfo, gbc);
		gbc.gridx = 2;
		infoPanel.add(edition, gbc);
		gbc.gridx = 3;
		infoPanel.add(editionInfo, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		infoPanel.add(format, gbc);
		gbc.gridx = 1;
		infoPanel.add(formatInfo, gbc);
		gbc.gridx = 2;
		infoPanel.add(source, gbc);
		gbc.gridx = 3;
		infoPanel.add(sourceInfo, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		infoPanel.add(isbn, gbc);
		gbc.gridx = 1;
		infoPanel.add(isbnInfo, gbc);
		gbc.gridx = 2;
		infoPanel.add(note, gbc);
		gbc.gridx = 3;
		infoPanel.add(noteInfo, gbc);
		
		// Create record panel
		String[] columns = {"ID", "User", "Borrow Date", "Borrow Days", "Due Date", "Return Date"};
		model = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {return false;}
		};
		
		recordTable = new JTable(model);
		recordTable.setFont(recordTable.getFont().deriveFont(15.0f));
		recordTable.getTableHeader().setFont(recordTable.getFont().deriveFont(15.0f));
		recordTable.setRowHeight(25);
		recordTable.getTableHeader().setResizingAllowed(false);
		TableStyle.applyUserStyle(recordTable);
		loadRecordData(book_id);
		
		recordTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		TableColumnModel columnModel = recordTable.getColumnModel();
		
		TableColumn column0 = columnModel.getColumn(0);
        column0.setMinWidth(0);
        column0.setMaxWidth(0);
        column0.setPreferredWidth(0);
		
        TableColumn columnName = columnModel.getColumn(1);
        columnName.setMinWidth(80);
        columnName.setMaxWidth(100);
        columnName.setPreferredWidth(90);

        TableColumn columnDay = columnModel.getColumn(3);
        columnDay.setMinWidth(120);
        columnDay.setMaxWidth(140);
        columnDay.setPreferredWidth(130);

        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		contentPanel.add(infoPanel, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.gridy = 1;
		contentPanel.add(new JScrollPane(recordTable), gbc);

		gbc.gridy = 2;
		gbc.weighty = 0.7;
		contentPanel.add(
				ReviewPanel.createReviewsPanel(book_id, true), gbc);
		
		super.setContentPane(contentPanel);
		super.pack();
		super.setResizable(false);
		super.setLocationRelativeTo(null);
		super.setVisible(true);
	}

	private Book getBook(int book_id) {
		String sql = "SELECT * FROM books WHERE book_id = ?";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setString(1, String.valueOf(book_id));
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					List<String> authorArray = new ArrayList<String>(Arrays.asList(resultSet.getString("authors").split(", ")));
					List<String> subjectArray = new ArrayList<String>(Arrays.asList(resultSet.getString("subjects").split(", ")));
					List<String> isbnArray = new ArrayList<String>(Arrays.asList(resultSet.getString("isbn").split(", ")));
					return new Book(resultSet.getString("title"),
									authorArray, subjectArray, 
									resultSet.getString("publisher"), 
									resultSet.getString("publish_year"), 
									resultSet.getString("edition"), 
									resultSet.getString("format_desc"),
									resultSet.getString("Source"), 
									isbnArray, 
									resultSet.getString("note"));
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to get informations", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	private void loadRecordData(int book_id) {
		// Check if any record of the book exists.
		String stmt = "SELECT * FROM borrow_records WHERE book_id = ?";
		try (PreparedStatement pstmt = system.StartSystem.db.prepareStatement(stmt)) {
			pstmt.setInt(1, book_id);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				System.out.println("Records exist");
				// Load the records.
				String sql = "SELECT b.record_id, u.name, b.borrow_date, b.borrow_days, b.due_date, b.return_date " +
						"FROM borrow_records b " +
						"JOIN users u ON b.user_id = u.user_id " +
						"WHERE b.book_id = ? " +
						"ORDER BY b.borrow_date ASC";
				try (PreparedStatement ps = system.StartSystem.db.prepareStatement(sql)) {
					ps.setInt(1, book_id);
					ResultSet resultSet = ps.executeQuery();
					model.setRowCount(0);
					while (resultSet.next()) {
						model.addRow(new Object[] {resultSet.getInt(1), 
								"*****",
								resultSet.getString(3), 
								resultSet.getInt(4), 
								resultSet.getString(5), 
								resultSet.getString(6)});
					}
				}
			} else {
				System.out.println("No existing reocrds");
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
