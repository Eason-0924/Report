package admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import system.StartSystem;
import system.TableStyle;

public class BorrowRecord extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMAT =
			DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final JComboBox<ComboItem> bookBox;
	private final JComboBox<ComboItem> userBox;
	private final DefaultTableModel tableModel;
	private final JLabel resultCountLabel;

	public BorrowRecord() {
		super.setLayout(new BorderLayout(10, 10));
		super.setBorder(new EmptyBorder(10, 10, 10, 10));
		super.setBackground(Color.WHITE);

		Font titleFont = new Font("SansSerif", Font.BOLD, 16);
		JPanel searchPanel = new JPanel(new GridBagLayout());
		searchPanel.setBackground(Color.WHITE);
		searchPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY),
				"Search Borrow Record",
				TitledBorder.LEFT,
				TitledBorder.TOP,
				titleFont));

		bookBox = new JComboBox<>(loadBookItems().toArray(new ComboItem[0]));
		userBox = new JComboBox<>(loadUserItems().toArray(new ComboItem[0]));
		bookBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
		userBox.setFont(new Font("SansSerif", Font.PLAIN, 14));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		searchPanel.add(new JLabel("Book:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		searchPanel.add(bookBox, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		searchPanel.add(new JLabel("User:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		searchPanel.add(userBox, gbc);

		bookBox.addActionListener(_ -> searchRecords());
		userBox.addActionListener(_ -> searchRecords());

		super.add(searchPanel, BorderLayout.NORTH);

		String[] columns = {
				"Record ID",
				"Book Title",
				"User Name",
				"Student No.",
				"Borrow Date",
				"Due Date",
				"Return Date",
				"Status"
		};

		tableModel = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable recordTable = new JTable(tableModel);
		recordTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
		recordTable.getTableHeader().setFont(
				new Font("SansSerif", Font.BOLD, 14));
		recordTable.setRowHeight(26);
		recordTable.setAutoCreateRowSorter(true);
		recordTable.getTableHeader().setReorderingAllowed(false);
		TableStyle.applyAdminStyle(recordTable);

		TableColumn recordIdColumn = recordTable.getColumnModel().getColumn(0);
		recordIdColumn.setMinWidth(0);
		recordIdColumn.setMaxWidth(0);
		recordIdColumn.setPreferredWidth(0);

		TableColumn titleColumn = recordTable.getColumnModel().getColumn(1);
		titleColumn.setMaxWidth(250);
		titleColumn.setPreferredWidth(80);

		TableColumn userNameColumn = recordTable.getColumnModel().getColumn(2);
		userNameColumn.setMinWidth(80);
		userNameColumn.setMaxWidth(125);
		userNameColumn.setPreferredWidth(100);

		TableColumn studentNumberColumn = recordTable.getColumnModel().getColumn(3);
		studentNumberColumn.setMinWidth(100);
		studentNumberColumn.setMaxWidth(140);
		studentNumberColumn.setPreferredWidth(115);

		for (int index = 4; index <= 6; index++) {
			TableColumn dateColumn = recordTable.getColumnModel().getColumn(index);
			dateColumn.setMinWidth(130);
			dateColumn.setMaxWidth(170);
			dateColumn.setPreferredWidth(150);
		}

		TableColumn statusColumn = recordTable.getColumnModel().getColumn(7);
		statusColumn.setMinWidth(100);
		statusColumn.setMaxWidth(135);
		statusColumn.setPreferredWidth(115);

		JScrollPane scrollPane = new JScrollPane(recordTable);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		super.add(scrollPane, BorderLayout.CENTER);

		resultCountLabel = new JLabel("0 record(s)");
		resultCountLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		super.add(resultCountLabel, BorderLayout.SOUTH);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent event) {
				searchRecords();
			}
		});

		searchRecords();
	}

	public void searchRecords() {
		ComboItem selectedBook = (ComboItem) bookBox.getSelectedItem();
		ComboItem selectedUser = (ComboItem) userBox.getSelectedItem();

		StringBuilder sql = new StringBuilder(
				"SELECT r.record_id, b.title, u.name, u.student_no, " +
				"r.borrow_date, r.due_date, r.return_date, " +
				"CASE " +
				"WHEN r.return_date IS NOT NULL THEN 'RETURNED' " +
				"WHEN r.due_date < NOW() THEN 'OVERDUE' " +
				"ELSE 'BORROWING' END AS record_status " +
				"FROM borrow_records r " +
				"JOIN books b ON r.book_id = b.book_id " +
				"JOIN users u ON r.user_id = u.user_id " +
				"WHERE 1=1 ");

		List<Integer> parameters = new ArrayList<>();
		if (selectedBook != null && selectedBook.id() != null) {
			sql.append("AND r.book_id = ? ");
			parameters.add(selectedBook.id());
		}
		if (selectedUser != null && selectedUser.id() != null) {
			sql.append("AND r.user_id = ? ");
			parameters.add(selectedUser.id());
		}
		sql.append("ORDER BY r.borrow_date DESC");

		tableModel.setRowCount(0);

		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql.toString())) {
			for (int index = 0; index < parameters.size(); index++) {
				statement.setInt(index + 1, parameters.get(index));
			}

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					tableModel.addRow(new Object[] {
							resultSet.getInt("record_id"),
							resultSet.getString("title"),
							resultSet.getString("name"),
							resultSet.getString("student_no"),
							formatDate(resultSet.getTimestamp("borrow_date")),
							formatDate(resultSet.getTimestamp("due_date")),
							formatDate(resultSet.getTimestamp("return_date")),
							resultSet.getString("record_status")
					});
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to search borrow records.",
					"Search Error",
					JOptionPane.ERROR_MESSAGE);
		}

		resultCountLabel.setText(tableModel.getRowCount() + " record(s)");
	}

	private List<ComboItem> loadBookItems() {
		List<ComboItem> items = new ArrayList<>();
		items.add(new ComboItem(null, "All Books"));

		String sql = "SELECT book_id, title FROM books ORDER BY title";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				items.add(new ComboItem(
						resultSet.getInt("book_id"),
						resultSet.getString("title")));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return items;
	}

	private List<ComboItem> loadUserItems() {
		List<ComboItem> items = new ArrayList<>();
		items.add(new ComboItem(null, "All Users"));

		String sql =
				"SELECT user_id, name, student_no FROM users " +
				"ORDER BY user_id ASC";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				items.add(new ComboItem(
						resultSet.getInt("user_id"),
						resultSet.getString("name") + " - "
								+ resultSet.getString("student_no")));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return items;
	}

	private String formatDate(Timestamp timestamp) {
		if (timestamp == null) {
			return "-";
		}
		return timestamp.toLocalDateTime().toLocalDate().format(DATE_FORMAT);
	}

	private record ComboItem(Integer id, String label) {
		@Override
		public String toString() {
			return label;
		}
	}
}
