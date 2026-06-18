package admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import system.StartSystem;
import system.SuspensionManager;
import system.TableStyle;

public class ManageUser extends JPanel {
	private static final long serialVersionUID = 1L;

	private final DefaultTableModel tableModel;
	private final JTable userTable;
	private final JComboBox<String> roleLevelBox;
	private final JComboBox<String> statusBox;
	private final JLabel selectedUserLabel;
	private final JButton clearTemporaryButton;

	public ManageUser() {
		super.setLayout(new BorderLayout(10, 10));
		super.setBorder(new EmptyBorder(10, 10, 10, 10));
		super.setBackground(Color.WHITE);

		String[] columns = {
				"User ID",
				"User Name",
				"Student No.",
				"Role Level",
				"Status",
				"Suspended Until"
		};

		tableModel = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		userTable = new JTable(tableModel);
		userTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
		userTable.getTableHeader().setFont(
				new Font("SansSerif", Font.BOLD, 15));
		userTable.setRowHeight(28);
		userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userTable.setAutoCreateRowSorter(true);
		userTable.getTableHeader().setReorderingAllowed(false);
		TableStyle.applyAdminStyle(userTable);

		TableColumn userIdColumn = userTable.getColumnModel().getColumn(0);
		userIdColumn.setMinWidth(0);
		userIdColumn.setMaxWidth(0);
		userIdColumn.setPreferredWidth(0);

		TableColumn nameColumn = userTable.getColumnModel().getColumn(1);
		nameColumn.setPreferredWidth(200);

		TableColumn studentNumberColumn =
				userTable.getColumnModel().getColumn(2);
		studentNumberColumn.setPreferredWidth(200);

		TableColumn roleColumn = userTable.getColumnModel().getColumn(3);
		roleColumn.setPreferredWidth(200);

		TableColumn statusColumn = userTable.getColumnModel().getColumn(4);
		statusColumn.setPreferredWidth(160);

		TableColumn suspendedUntilColumn =
				userTable.getColumnModel().getColumn(5);
		suspendedUntilColumn.setPreferredWidth(160);

		JScrollPane scrollPane = new JScrollPane(userTable);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(Color.WHITE);
		JLabel usersTitle = new JLabel("Users");
		usersTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
		usersTitle.setBorder(new EmptyBorder(0, 5, 8, 5));
		tablePanel.add(usersTitle, BorderLayout.NORTH);
		tablePanel.add(scrollPane, BorderLayout.CENTER);
		super.add(tablePanel, BorderLayout.CENTER);

		JPanel editPanel = new JPanel(new GridBagLayout());
		editPanel.setBackground(Color.WHITE);
		editPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createLineBorder(Color.LIGHT_GRAY),
						"Modify User",
						TitledBorder.LEFT,
						TitledBorder.TOP,
						new Font("SansSerif", Font.BOLD, 16)),
				new EmptyBorder(10, 10, 10, 10)));

		selectedUserLabel = new JLabel("Select a user from the table");
		selectedUserLabel.setFont(
				selectedUserLabel.getFont().deriveFont(Font.BOLD, 14.0f));
		Dimension selectedUserSize = new Dimension(250, 30);
		selectedUserLabel.setPreferredSize(selectedUserSize);
		selectedUserLabel.setMinimumSize(selectedUserSize);
		selectedUserLabel.setMaximumSize(selectedUserSize);

		roleLevelBox = new JComboBox<>(
				new String[] {"NORMAL", "VIP"});
		statusBox = new JComboBox<>(
				new String[] {"ACTIVE", "SUSPENDED"});
		Dimension comboBoxSize = new Dimension(150, 30);
		roleLevelBox.setPreferredSize(comboBoxSize);
		statusBox.setPreferredSize(comboBoxSize);
		roleLevelBox.setFont(
				roleLevelBox.getFont().deriveFont(Font.PLAIN, 14.0f));
		statusBox.setFont(
				statusBox.getFont().deriveFont(Font.PLAIN, 14.0f));

		roleLevelBox.setEnabled(false);
		statusBox.setEnabled(false);

		JButton saveButton = new JButton("Save Changes");
		saveButton.setEnabled(false);
		clearTemporaryButton = new JButton("Lift Suspension");
		clearTemporaryButton.setEnabled(false);

		GridBagConstraints editGbc = new GridBagConstraints();
		editGbc.gridy = 0;
		editGbc.insets = new Insets(0, 10, 0, 10);
		editGbc.anchor = GridBagConstraints.WEST;
		editGbc.fill = GridBagConstraints.HORIZONTAL;

		editGbc.gridx = 0;
		editGbc.weightx = 0;
		editPanel.add(selectedUserLabel, editGbc);

		JLabel roleLevelLabel = new JLabel("Role Level:");
		roleLevelLabel.setFont(
				roleLevelLabel.getFont().deriveFont(Font.BOLD, 15.0f));
		JLabel statusLabel = new JLabel("Status:");
		statusLabel.setFont(
				statusLabel.getFont().deriveFont(Font.BOLD, 15.0f));

		editGbc.gridx = 1;
		editGbc.weightx = 0;
		editGbc.fill = GridBagConstraints.NONE;
		editPanel.add(roleLevelLabel, editGbc);

		editGbc.gridx = 2;
		editGbc.fill = GridBagConstraints.HORIZONTAL;
		editPanel.add(roleLevelBox, editGbc);

		editGbc.gridx = 3;
		editGbc.fill = GridBagConstraints.NONE;
		editPanel.add(statusLabel, editGbc);

		editGbc.gridx = 4;
		editGbc.fill = GridBagConstraints.HORIZONTAL;
		editPanel.add(statusBox, editGbc);

		editGbc.gridx = 5;
		editGbc.insets = new Insets(0, 15, 0, 5);
		editPanel.add(saveButton, editGbc);

		editGbc.gridx = 6;
		editGbc.weightx = 0;
		editGbc.insets = new Insets(0, 5, 0, 5);
		editPanel.add(clearTemporaryButton, editGbc);

		editGbc.gridx = 7;
		editGbc.weightx = 1.0;
		editPanel.add(new JLabel(), editGbc);

		super.add(editPanel, BorderLayout.SOUTH);

		userTable.getSelectionModel().addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				loadSelectedUser(saveButton);
			}
		});

		saveButton.addActionListener(_ -> updateSelectedUser());
		clearTemporaryButton.addActionListener(_ ->
				clearTemporarySuspension());

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent event) {
				loadUsers();
			}
		});

		loadUsers();
	}

	public void loadUsers() {
		String sql =
				"SELECT user_id, name, student_no, role_level, status, " +
				"suspended_until " +
				"FROM users ORDER BY user_id";

		tableModel.setRowCount(0);

		try {
			SuspensionManager.refreshAllUsers();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}

		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				tableModel.addRow(new Object[] {
						resultSet.getInt("user_id"),
						resultSet.getString("name"),
						resultSet.getString("student_no"),
						resultSet.getString("role_level"),
						resultSet.getString("status"),
						resultSet.getString("suspended_until")
				});
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to load users.",
					"Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void loadSelectedUser(JButton saveButton) {
		int selectedRow = userTable.getSelectedRow();
		if (selectedRow == -1) {
			selectedUserLabel.setText("Select a user from the table");
			roleLevelBox.setEnabled(false);
			statusBox.setEnabled(false);
			saveButton.setEnabled(false);
			clearTemporaryButton.setEnabled(false);
			return;
		}

		int modelRow = userTable.convertRowIndexToModel(selectedRow);
		String name = tableModel.getValueAt(modelRow, 1).toString();
		String studentNumber = tableModel.getValueAt(modelRow, 2).toString();
		String roleLevel = tableModel.getValueAt(modelRow, 3).toString();
		String status = tableModel.getValueAt(modelRow, 4).toString();

		selectedUserLabel.setText(name + " - " + studentNumber);
		roleLevelBox.setSelectedItem(roleLevel);
		statusBox.setSelectedItem(status);
		roleLevelBox.setEnabled(true);
		statusBox.setEnabled(true);
		saveButton.setEnabled(true);
		clearTemporaryButton.setEnabled("SUSPENDED".equals(status));
	}

	private void clearTemporarySuspension() {
		int selectedRow = userTable.getSelectedRow();
		if (selectedRow == -1) {
			return;
		}

		int modelRow = userTable.convertRowIndexToModel(selectedRow);
		int userId = Integer.parseInt(
				tableModel.getValueAt(modelRow, 0).toString());

		try {
			SuspensionManager.setAdminStatus(userId, "ACTIVE");
			loadUsers();
			selectUserById(userId);
			JOptionPane.showMessageDialog(
					this,
					"Suspension lifted.",
					"Update Succeed",
					JOptionPane.PLAIN_MESSAGE);
		} catch (IllegalStateException exception) {
			JOptionPane.showMessageDialog(
					this,
					"The user still has overdue books. The suspension "
							+ "cannot be lifted yet.",
					"Update Failed",
					JOptionPane.WARNING_MESSAGE);
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to lift the suspension.",
					"Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void updateSelectedUser() {
		int selectedRow = userTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(
					this,
					"Please select a user first.",
					"Update Failed",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		int modelRow = userTable.convertRowIndexToModel(selectedRow);
		int userId = Integer.parseInt(
				tableModel.getValueAt(modelRow, 0).toString());
		String roleLevel = roleLevelBox.getSelectedItem().toString();
		String status = statusBox.getSelectedItem().toString();

		String sql =
				"UPDATE users SET role_level = ? WHERE user_id = ?";

		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setString(1, roleLevel);
			statement.setInt(2, userId);

			if (statement.executeUpdate() > 0) {
				SuspensionManager.setAdminStatus(userId, status);
				loadUsers();
				selectUserById(userId);
				JOptionPane.showMessageDialog(
						this,
						"User information updated successfully.",
						"Update Succeed",
						JOptionPane.PLAIN_MESSAGE);
			}
		} catch (IllegalStateException exception) {
			JOptionPane.showMessageDialog(
					this,
					"The user still has overdue books and cannot be "
							+ "changed to ACTIVE.",
					"Update Failed",
					JOptionPane.WARNING_MESSAGE);
			loadUsers();
			selectUserById(userId);
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to update user information.",
					"Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void selectUserById(int userId) {
		for (int modelRow = 0; modelRow < tableModel.getRowCount(); modelRow++) {
			int rowUserId = Integer.parseInt(
					tableModel.getValueAt(modelRow, 0).toString());
			if (rowUserId == userId) {
				int viewRow = userTable.convertRowIndexToView(modelRow);
				userTable.setRowSelectionInterval(viewRow, viewRow);
				userTable.scrollRectToVisible(
						userTable.getCellRect(viewRow, 0, true));
				return;
			}
		}
	}
}
