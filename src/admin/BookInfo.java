package admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import system.StartSystem;
import system.TableStyle;

public class BookInfo extends JFrame {
	private static final long serialVersionUID = 1L;

	private final int bookId;
	private final Runnable booksChanged;

	private final JTextField titleField = new JTextField();
	private final JTextField authorsField = new JTextField();
	private final JTextField subjectsField = new JTextField();
	private final JTextField publisherField = new JTextField();
	private final JTextField publishYearField = new JTextField();
	private final JTextField editionField = new JTextField();
	private final JTextField formatDescField = new JTextField();
	private final JTextField sourceField = new JTextField();
	private final JTextField isbnField = new JTextField();
	private final JTextArea noteArea = new JTextArea(3, 20);

	private final DefaultTableModel recordModel;

	public BookInfo(int bookId, Runnable booksChanged) {
		this.bookId = bookId;
		this.booksChanged = booksChanged;

		setTitle("Modify Book Information");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
		contentPanel.setPreferredSize(new Dimension(900, 760));
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		noteArea.setLineWrap(true);
		noteArea.setWrapStyleWord(true);

		contentPanel.add(createInformationPanel(), BorderLayout.NORTH);

		String[] columns = {
				"ID", "User", "Borrow Date", "Borrow Days",
				"Due Date", "Return Date"
		};
		recordModel = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable recordTable = new JTable(recordModel);
		recordTable.setFont(recordTable.getFont().deriveFont(14.0f));
		recordTable.getTableHeader().setFont(
				recordTable.getTableHeader().getFont()
						.deriveFont(Font.BOLD, 14.0f));
		recordTable.setRowHeight(25);
		TableStyle.applyAdminStyle(recordTable);

		TableColumn idColumn = recordTable.getColumnModel().getColumn(0);
		idColumn.setMinWidth(0);
		idColumn.setMaxWidth(0);
		idColumn.setPreferredWidth(0);

		JPanel recordPanel = new JPanel(new BorderLayout());
		recordPanel.setBackground(Color.WHITE);
		JLabel recordTitle = new JLabel("Borrow Records");
		recordTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
		recordTitle.setBorder(new EmptyBorder(0, 5, 8, 5));
		recordPanel.add(recordTitle, BorderLayout.NORTH);
		recordPanel.add(new JScrollPane(recordTable), BorderLayout.CENTER);

		JPanel historyPanel = new JPanel(new GridLayout(2, 1, 0, 10));
		historyPanel.setBackground(Color.WHITE);
		historyPanel.add(recordPanel);
		historyPanel.add(user.Review.createReviewsPanel(bookId));
		contentPanel.add(historyPanel, BorderLayout.CENTER);
		contentPanel.add(createButtonPanel(), BorderLayout.SOUTH);

		if (!loadBook()) {
			dispose();
			return;
		}
		loadRecordData();

		setContentPane(contentPanel);
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private JPanel createInformationPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		panel.setBorder(createSectionBorder("Book Information"));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 6, 5, 6);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addField(panel, gbc, 0, 0, "Title:", titleField);
		addField(panel, gbc, 0, 2, "Authors:", authorsField);
		addField(panel, gbc, 1, 0, "Subjects:", subjectsField);
		addField(panel, gbc, 1, 2, "Publisher:", publisherField);
		addField(panel, gbc, 2, 0, "Publish Year:", publishYearField);
		addField(panel, gbc, 2, 2, "Edition:", editionField);
		addField(panel, gbc, 3, 0, "Format Desc:", formatDescField);
		addField(panel, gbc, 3, 2, "Source:", sourceField);
		addField(panel, gbc, 4, 0, "ISBN:", isbnField);

		JLabel noteLabel = createLabel("Notes:");
		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(noteLabel, gbc);

		gbc.gridx = 3;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		JScrollPane noteScrollPane = new JScrollPane(noteArea);
		noteScrollPane.setPreferredSize(new Dimension(280, 70));
		panel.add(noteScrollPane, gbc);

		return panel;
	}

	private void addField(
			JPanel panel,
			GridBagConstraints gbc,
			int row,
			int labelColumn,
			String labelText,
			JTextField field) {
		gbc.gridx = labelColumn;
		gbc.gridy = row;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(createLabel(labelText), gbc);

		gbc.gridx = labelColumn + 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		field.setPreferredSize(new Dimension(280, 28));
		panel.add(field, gbc);
	}

	private JLabel createLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(Font.BOLD, 14.0f));
		return label;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
		panel.setBackground(Color.WHITE);

		JButton removeButton = new JButton("Remove Book");
		JButton saveButton = new JButton("Save Changes");
		removeButton.addActionListener(_ -> removeBook());
		saveButton.addActionListener(_ -> saveBook());

		panel.add(removeButton);
		panel.add(saveButton);
		return panel;
	}

	private boolean loadBook() {
		String sql = "SELECT * FROM books WHERE book_id = ?";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, bookId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					JOptionPane.showMessageDialog(
							this,
							"The selected book no longer exists.",
							"Book Not Found",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}

				titleField.setText(value(resultSet, "title"));
				authorsField.setText(value(resultSet, "authors"));
				subjectsField.setText(value(resultSet, "subjects"));
				publisherField.setText(value(resultSet, "publisher"));
				publishYearField.setText(value(resultSet, "publish_year"));
				editionField.setText(value(resultSet, "edition"));
				formatDescField.setText(value(resultSet, "format_desc"));
				sourceField.setText(value(resultSet, "source"));
				isbnField.setText(value(resultSet, "isbn"));
				noteArea.setText(value(resultSet, "note"));
				return true;
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			showDatabaseError("Failed to load the book information.");
			return false;
		}
	}

	private String value(ResultSet resultSet, String column)
			throws SQLException {
		String value = resultSet.getString(column);
		return value == null ? "" : value;
	}

	private void loadRecordData() {
		String sql =
				"SELECT r.record_id, u.name, r.borrow_date, r.borrow_days, " +
				"r.due_date, r.return_date " +
				"FROM borrow_records r " +
				"JOIN users u ON r.user_id = u.user_id " +
				"WHERE r.book_id = ? ORDER BY r.borrow_date ASC";

		recordModel.setRowCount(0);
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, bookId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					recordModel.addRow(new Object[] {
							resultSet.getInt("record_id"),
							resultSet.getString("name"),
							resultSet.getString("borrow_date"),
							resultSet.getInt("borrow_days"),
							resultSet.getString("due_date"),
							resultSet.getString("return_date")
					});
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			showDatabaseError("Failed to load borrow records.");
		}
	}

	private void saveBook() {
		String title = titleField.getText().trim();
		String publishYear = publishYearField.getText().trim();
		if (title.isEmpty()) {
			JOptionPane.showMessageDialog(
					this,
					"Please enter the book title.",
					"Save Failed",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (authorsField.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(
					this,
					"Please enter at least one author.",
					"Save Failed",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!publishYear.isEmpty() && !publishYear.matches("\\d{4}")) {
			JOptionPane.showMessageDialog(
					this,
					"Publish year must contain four digits.",
					"Save Failed",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String sql =
				"UPDATE books SET title = ?, authors = ?, subjects = ?, " +
				"publisher = ?, publish_year = ?, edition = ?, " +
				"format_desc = ?, source = ?, isbn = ?, note = ? " +
				"WHERE book_id = ?";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setString(1, title);
			statement.setString(2, normalizeList(authorsField.getText()));
			statement.setString(3, normalizeList(subjectsField.getText()));
			statement.setString(4, publisherField.getText().trim());
			statement.setString(5, publishYear);
			statement.setString(6, editionField.getText().trim());
			statement.setString(7, formatDescField.getText().trim());
			statement.setString(8, sourceField.getText().trim());
			statement.setString(9, normalizeList(isbnField.getText()));
			statement.setString(10, noteArea.getText().trim());
			statement.setInt(11, bookId);

			statement.executeUpdate();
			notifyBooksChanged();
			JOptionPane.showMessageDialog(
					this,
					"Book information updated successfully.",
					"Save Succeed",
					JOptionPane.PLAIN_MESSAGE);
		} catch (SQLException exception) {
			exception.printStackTrace();
			showDatabaseError("Failed to update the book.");
		}
	}

	private String normalizeList(String value) {
		return value.trim().replaceAll("\\s*,\\s*", ", ");
	}

	private void removeBook() {
		if (hasRelatedRecords()) {
			JOptionPane.showMessageDialog(
					this,
					"This book cannot be removed because it has borrow, "
							+ "reservation, or review records.",
					"Remove Book Failed",
					JOptionPane.PLAIN_MESSAGE);
			return;
		}

		int choice = JOptionPane.showConfirmDialog(
				this,
				"Remove 《" + titleField.getText().trim() + "》?",
				"Confirm Remove",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}

		String sql = "DELETE FROM books WHERE book_id = ?";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, bookId);
			if (statement.executeUpdate() > 0) {
				notifyBooksChanged();
				JOptionPane.showMessageDialog(
						this,
						"Book removed successfully.",
						"Remove Book Succeed",
						JOptionPane.PLAIN_MESSAGE);
				dispose();
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			showDatabaseError("Failed to remove the book.");
		}
	}

	private boolean hasRelatedRecords() {
		String sql =
				"SELECT (" +
				"(SELECT COUNT(*) FROM borrow_records WHERE book_id = ?) + " +
				"(SELECT COUNT(*) FROM reservations WHERE book_id = ?) + " +
				"(SELECT COUNT(*) FROM reviews WHERE book_id = ?)" +
				")";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, bookId);
			statement.setInt(2, bookId);
			statement.setInt(3, bookId);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next() && resultSet.getInt(1) > 0;
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			showDatabaseError("Failed to check the book's related records.");
			return true;
		}
	}

	private void notifyBooksChanged() {
		if (booksChanged != null) {
			booksChanged.run();
		}
	}

	private TitledBorder createSectionBorder(String title) {
		return BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY),
				title,
				TitledBorder.LEFT,
				TitledBorder.TOP,
				new Font("SansSerif", Font.BOLD, 16));
	}

	private void showDatabaseError(String message) {
		JOptionPane.showMessageDialog(
				this,
				message,
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
	}
}
