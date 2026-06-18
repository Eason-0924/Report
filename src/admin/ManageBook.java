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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import system.StartSystem;
import system.TableStyle;

public class ManageBook extends JPanel {
	private static final long serialVersionUID = 1L;

	private final DefaultTableModel tableModel;
	private final JTable bookTable;

	private final JTextField titleField;
	private final MultiValueField authorsField;
	private final MultiValueField subjectsField;
	private final JTextField publisherField;
	private final JTextField publishYearField;
	private final JTextField editionField;
	private final JTextField formatDescField;
	private final JTextField sourceField;
	private final MultiValueField isbnField;
	private final JTextArea noteArea;

	public ManageBook() {
		super.setLayout(new BorderLayout());
		super.setBorder(new EmptyBorder(10, 10, 10, 10));
		super.setBackground(Color.WHITE);

		String[] columns = {"Book ID", "Title", "Authors", "Year"};
		tableModel = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		bookTable = new JTable(tableModel);
		bookTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
		bookTable.getTableHeader().setFont(
				new Font("SansSerif", Font.BOLD, 14));
		bookTable.setRowHeight(26);
		bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bookTable.setAutoCreateRowSorter(true);
		bookTable.getTableHeader().setReorderingAllowed(false);
		TableStyle.applyAdminStyle(bookTable);

		TableColumn idColumn = bookTable.getColumnModel().getColumn(0);
		idColumn.setMinWidth(0);
		idColumn.setMaxWidth(0);
		idColumn.setPreferredWidth(0);

		bookTable.getColumnModel().getColumn(1).setPreferredWidth(220);
		bookTable.getColumnModel().getColumn(2).setPreferredWidth(150);
		bookTable.getColumnModel().getColumn(3).setPreferredWidth(60);

		JScrollPane tableScrollPane = new JScrollPane(bookTable);
		tableScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setBackground(Color.WHITE);
		JLabel existingBooksTitle = new JLabel("Existing Books");
		existingBooksTitle.setFont(
				new Font("SansSerif", Font.BOLD, 16));
		existingBooksTitle.setBorder(new EmptyBorder(0, 5, 8, 5));
		leftPanel.add(existingBooksTitle, BorderLayout.NORTH);
		leftPanel.add(tableScrollPane, BorderLayout.CENTER);

		bookTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent event) {
				if (event.getClickCount() == 2
						&& bookTable.getSelectedRow() != -1) {
					openSelectedBook();
				}
			}
		});

		titleField = new JTextField();
		authorsField = new MultiValueField("Add Author");
		subjectsField = new MultiValueField("Add Subject");
		publisherField = new JTextField();
		publishYearField = new JTextField();
		editionField = new JTextField();
		formatDescField = new JTextField();
		sourceField = new JTextField();
		isbnField = new MultiValueField("Add ISBN");
		noteArea = new JTextArea(4, 20);
		noteArea.setLineWrap(true);
		noteArea.setWrapStyleWord(true);

		JPanel formPanel = createFormPanel();
		JScrollPane formScrollPane = new JScrollPane(formPanel);
		formScrollPane.setBorder(null);
		formScrollPane.getVerticalScrollBar().setUnitIncrement(12);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBackground(Color.WHITE);
		rightPanel.setBorder(createSectionBorder("Add New Book"));
		rightPanel.add(formScrollPane, BorderLayout.CENTER);

		JPanel managementPanel = new JPanel(new GridLayout(1, 2, 15, 0));
		managementPanel.setBackground(Color.WHITE);
		managementPanel.add(leftPanel);
		managementPanel.add(rightPanel);

		super.add(managementPanel, BorderLayout.CENTER);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent event) {
				loadBooks();
			}
		});

		loadBooks();
	}

	private JPanel createFormPanel() {
		JPanel formPanel = new JPanel(new GridBagLayout());
		formPanel.setBackground(Color.WHITE);
		formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		int row = 0;
		addFormRow(formPanel, gbc, row++, "Title:", titleField);
		addFormRow(formPanel, gbc, row++, "Authors:", authorsField);
		addFormRow(formPanel, gbc, row++, "Subjects:", subjectsField);
		addFormRow(formPanel, gbc, row++, "Publisher:", publisherField);
		addFormRow(formPanel, gbc, row++, "Publish Year:", publishYearField);
		addFormRow(formPanel, gbc, row++, "Edition:", editionField);
		addFormRow(formPanel, gbc, row++, "Format Desc:", formatDescField);
		addFormRow(formPanel, gbc, row++, "Source:", sourceField);
		addFormRow(formPanel, gbc, row++, "ISBN:", isbnField);

		JScrollPane noteScrollPane = new JScrollPane(noteArea);
		noteScrollPane.setPreferredSize(new Dimension(300, 100));
		addFormRow(formPanel, gbc, row++, "Notes:", noteScrollPane);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		buttonPanel.setBackground(Color.WHITE);

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(_ -> clearForm());

		JButton addButton = new JButton("Add Book");
		addButton.addActionListener(_ -> addBook());

		buttonPanel.add(clearButton);
		buttonPanel.add(addButton);

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 2;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTH;
		formPanel.add(buttonPanel, gbc);

		return formPanel;
	}

	private void addFormRow(
			JPanel panel,
			GridBagConstraints gbc,
			int row,
			String labelText,
			java.awt.Component component) {
		JLabel label = new JLabel(labelText);
		label.setFont(label.getFont().deriveFont(Font.BOLD, 14.0f));

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(label, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(component, gbc);
	}

	private TitledBorder createSectionBorder(String title) {
		return BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY),
				title,
				TitledBorder.LEFT,
				TitledBorder.TOP,
				new Font("SansSerif", Font.BOLD, 16));
	}

	public void loadBooks() {
		String sql =
				"SELECT book_id, title, authors, publish_year " +
				"FROM books ORDER BY title";

		tableModel.setRowCount(0);

		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				tableModel.addRow(new Object[] {
						resultSet.getInt("book_id"),
						resultSet.getString("title"),
						resultSet.getString("authors"),
						resultSet.getString("publish_year")
				});
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			showDatabaseError("Failed to load books.");
		}
	}

	private void addBook() {
		String title = titleField.getText().trim();
		List<String> authors = authorsField.getValues();
		List<String> subjects = subjectsField.getValues();
		List<String> isbnValues = isbnField.getValues();
		String publishYear = publishYearField.getText().trim();

		if (title.isEmpty()) {
			JOptionPane.showMessageDialog(
					this,
					"Please enter the book title.",
					"Add Book Failed",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (authors.isEmpty()) {
			JOptionPane.showMessageDialog(
					this,
					"Please add at least one author.",
					"Add Book Failed",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!publishYear.isEmpty() && !publishYear.matches("\\d{4}")) {
			JOptionPane.showMessageDialog(
					this,
					"Publish year must contain four digits.",
					"Add Book Failed",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String sql =
				"INSERT INTO books(" +
				"title, authors, subjects, publisher, publish_year, edition, " +
				"format_desc, source, isbn, note" +
				") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement statement = StartSystem.db.prepareStatement(
				sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, title);
			statement.setString(2, String.join(", ", authors));
			statement.setString(3, String.join(", ", subjects));
			statement.setString(4, publisherField.getText().trim());
			statement.setString(5, publishYear);
			statement.setString(6, editionField.getText().trim());
			statement.setString(7, formatDescField.getText().trim());
			statement.setString(8, sourceField.getText().trim());
			statement.setString(9, String.join(", ", isbnValues));
			statement.setString(10, noteArea.getText().trim());

			if (statement.executeUpdate() > 0) {
				Integer newBookId = null;
				try (ResultSet keys = statement.getGeneratedKeys()) {
					if (keys.next()) {
						newBookId = keys.getInt(1);
					}
				}

				loadBooks();
				if (newBookId != null) {
					selectBookById(newBookId);
				}
				clearForm();
				JOptionPane.showMessageDialog(
						this,
						"Book added successfully.",
						"Add Book Succeed",
						JOptionPane.PLAIN_MESSAGE);
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			showDatabaseError("Failed to add the book.");
		}
	}

	private void openSelectedBook() {
		int selectedRow = bookTable.getSelectedRow();
		if (selectedRow == -1) {
			return;
		}

		int modelRow = bookTable.convertRowIndexToModel(selectedRow);
		int bookId = Integer.parseInt(
				tableModel.getValueAt(modelRow, 0).toString());
		new BookInfo(bookId, () -> refreshBookAfterChange(bookId));
	}

	private void refreshBookAfterChange(int bookId) {
		loadBooks();
		selectBookById(bookId);
	}

	private void clearForm() {
		titleField.setText("");
		authorsField.clear();
		subjectsField.clear();
		publisherField.setText("");
		publishYearField.setText("");
		editionField.setText("");
		formatDescField.setText("");
		sourceField.setText("");
		isbnField.clear();
		noteArea.setText("");
		titleField.requestFocusInWindow();
	}

	private void selectBookById(int bookId) {
		for (int modelRow = 0; modelRow < tableModel.getRowCount(); modelRow++) {
			int rowBookId = Integer.parseInt(
					tableModel.getValueAt(modelRow, 0).toString());
			if (rowBookId == bookId) {
				int viewRow = bookTable.convertRowIndexToView(modelRow);
				bookTable.setRowSelectionInterval(viewRow, viewRow);
				bookTable.scrollRectToVisible(
						bookTable.getCellRect(viewRow, 0, true));
				return;
			}
		}
	}

	private void showDatabaseError(String message) {
		JOptionPane.showMessageDialog(
				this,
				message,
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
	}

	private static class MultiValueField extends JPanel {
		private static final long serialVersionUID = 1L;

		private final List<JTextField> inputFields;
		private final JPanel additionalFieldsPanel;
		private final Dimension addButtonSize;

		MultiValueField(String addButtonText) {
			super(new BorderLayout(5, 5));
			setBackground(Color.WHITE);

			inputFields = new ArrayList<>();
			JTextField firstField = new JTextField();
			inputFields.add(firstField);

			JButton addButton = new JButton("+");
			addButton.setToolTipText(addButtonText);
			addButtonSize = addButton.getPreferredSize();

			JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
			inputPanel.setBackground(Color.WHITE);
			inputPanel.add(firstField, BorderLayout.CENTER);
			inputPanel.add(addButton, BorderLayout.EAST);

			additionalFieldsPanel = new JPanel();
			additionalFieldsPanel.setLayout(
					new BoxLayout(additionalFieldsPanel, BoxLayout.Y_AXIS));
			additionalFieldsPanel.setBackground(Color.WHITE);

			add(inputPanel, BorderLayout.NORTH);
			add(additionalFieldsPanel, BorderLayout.CENTER);

			addButton.addActionListener(_ -> addTextField());
		}

		private void addTextField() {
			JTextField newField = new JTextField();
			inputFields.add(newField);

			JPanel fieldRow = new JPanel(new BorderLayout(5, 0));
			fieldRow.setBackground(Color.WHITE);
			fieldRow.setMaximumSize(new Dimension(
					Integer.MAX_VALUE,
					newField.getPreferredSize().height));
			fieldRow.add(newField, BorderLayout.CENTER);

			JButton removeButton = new JButton("-");
			removeButton.setToolTipText("Remove this field");
			removeButton.setPreferredSize(addButtonSize);
			fieldRow.add(removeButton, BorderLayout.EAST);

			JPanel rowContainer = new JPanel(new BorderLayout());
			rowContainer.setBackground(Color.WHITE);
			rowContainer.setBorder(new EmptyBorder(5, 0, 0, 0));
			rowContainer.add(fieldRow, BorderLayout.CENTER);
			additionalFieldsPanel.add(rowContainer);

			removeButton.addActionListener(_ -> {
				inputFields.remove(newField);
				additionalFieldsPanel.remove(rowContainer);
				additionalFieldsPanel.revalidate();
				additionalFieldsPanel.repaint();
			});

			additionalFieldsPanel.revalidate();
			additionalFieldsPanel.repaint();
			newField.requestFocusInWindow();
		}

		List<String> getValues() {
			List<String> values = new ArrayList<>();
			for (JTextField field : inputFields) {
				String value = field.getText().trim();
				if (!value.isEmpty() && !values.contains(value)) {
					values.add(value);
				}
			}
			return values;
		}

		void clear() {
			JTextField firstField = inputFields.get(0);
			firstField.setText("");
			inputFields.clear();
			inputFields.add(firstField);
			additionalFieldsPanel.removeAll();
			additionalFieldsPanel.revalidate();
			additionalFieldsPanel.repaint();
		}
	}
}
