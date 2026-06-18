package admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import system.AppSettings;

public class Settings extends JPanel {
	private static final long serialVersionUID = 1L;

	private final JTextField normalBorrowDaysField = new JTextField(8);
	private final JTextField vipBorrowDaysField = new JTextField(8);
	private final JTextField normalBorrowBooksField = new JTextField(8);
	private final JTextField vipBorrowBooksField = new JTextField(8);
	private final JTextField normalReserveBooksField = new JTextField(8);
	private final JTextField vipReserveBooksField = new JTextField(8);
	private final JCheckBox dueNotificationBox = new JCheckBox();
	private final JTextField dueNotificationDaysField = new JTextField(8);
	private final JLabel dueNotificationDaysLabel =
			settingLabel("Days before due date");
	private final JCheckBox reservationNotificationBox = new JCheckBox();
	private final JCheckBox overdueNotificationBox = new JCheckBox();
	private final JTextField suspensionDaysField = new JTextField(8);
	private final JLabel suspensionDaysUnitLabel = settingLabel("day(s)");
	private final JCheckBox suspensionDurationBox = new JCheckBox();
	private final JCheckBox vipImmediateCancellationBox = new JCheckBox();
	private final JTextField securityKeyField = new JTextField(20);

	public Settings() {
		super(new BorderLayout());
		super.setBackground(Color.WHITE);
		super.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel content = new JPanel(new GridBagLayout());
		content.setBackground(Color.WHITE);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 12, 0);

		gbc.gridy = 0;
		content.add(createBorrowPanel(), gbc);
		gbc.gridy = 1;
		content.add(createNotificationPanel(), gbc);
		gbc.gridy = 2;
		content.add(createSuspensionPanel(), gbc);
		gbc.gridy = 3;
		content.add(createSecurityPanel(), gbc);
		gbc.gridy = 4;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		JPanel fillerPanel = new JPanel();
		fillerPanel.setBackground(Color.WHITE);
		content.add(fillerPanel, gbc);

		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setBackground(Color.WHITE);
		scrollPane.getVerticalScrollBar().setUnitIncrement(12);
		super.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel =
				new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		buttonPanel.setBackground(Color.WHITE);
		JButton defaultButton = new JButton("Set to Default");
		defaultButton.setFont(
				defaultButton.getFont().deriveFont(Font.BOLD, 15.0f));
		defaultButton.addActionListener(_ -> resetToDefaults());
		JButton saveButton = new JButton("Save Settings");
		saveButton.setFont(
				saveButton.getFont().deriveFont(Font.BOLD, 15.0f));
		saveButton.addActionListener(_ -> saveSettings());
		buttonPanel.add(defaultButton);
		buttonPanel.add(saveButton);
		super.add(buttonPanel, BorderLayout.SOUTH);

		for (JCheckBox checkBox : new JCheckBox[] {
				dueNotificationBox,
				reservationNotificationBox,
				overdueNotificationBox,
				suspensionDurationBox,
				vipImmediateCancellationBox
		}) {
			checkBox.setBackground(Color.WHITE);
		}

		dueNotificationBox.addActionListener(_ ->
				updateDueDaysEnabled());
		suspensionDurationBox.addActionListener(_ ->
				updateSuspensionDaysEnabled());
		loadSettings();
	}

	private JPanel createBorrowPanel() {
		JPanel panel = createSection("Borrow Settings");
		GridBagConstraints gbc = constraints();

		gbc.gridx = 1;
		gbc.gridy = 0;
		panel.add(header("NORMAL"), gbc);
		gbc.gridx = 2;
		panel.add(header("VIP"), gbc);

		addPairedRow(
				panel, 1, "Maximum borrow days",
				normalBorrowDaysField, vipBorrowDaysField);
		addPairedRow(
				panel, 2, "Maximum borrowing books",
				normalBorrowBooksField, vipBorrowBooksField);
		addPairedRow(
				panel, 3, "Maximum reserve books",
				normalReserveBooksField, vipReserveBooksField);
		return panel;
	}

	private JPanel createNotificationPanel() {
		JPanel panel = createSection("Notification Settings");
		addDueNotificationRow(panel, 0);
		addCheckBoxRow(
				panel, 1,
				"Enable reservation-ready notifications",
				reservationNotificationBox);
		addCheckBoxRow(
				panel, 2,
				"Enable overdue notifications",
				overdueNotificationBox);
		return panel;
	}

	private void addDueNotificationRow(JPanel panel, int row) {
		GridBagConstraints gbc = constraints();
		gbc.gridy = row;

		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(settingLabel("Enable due date notification"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(dueNotificationDaysLabel, gbc);

		gbc.gridx = 2;
		panel.add(dueNotificationDaysField, gbc);

		gbc.gridx = 3;
		gbc.insets = new Insets(6, 20, 6, 6);
		panel.add(dueNotificationBox, gbc);
	}

	private JPanel createSuspensionPanel() {
		JPanel panel = createSection("Suspension Rules");
		GridBagConstraints gbc = constraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(settingLabel(
				"Suspension duration after overdue books are returned"), gbc);
		gbc.gridx = 1;
		panel.add(suspensionDaysField, gbc);
		gbc.gridx = 2;
		panel.add(suspensionDaysUnitLabel, gbc);
		gbc.gridx = 3;
		gbc.insets = new Insets(6, 20, 6, 6);
		panel.add(suspensionDurationBox, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		addCheckBoxRow(
				panel,
				1,
				"Allow VIP immediate suspension cancellation",
				vipImmediateCancellationBox);
		return panel;
	}

	private JPanel createSecurityPanel() {
		JPanel panel = createSection("Security Key");
		GridBagConstraints gbc = constraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(settingLabel("Admin registration security key"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(securityKeyField, gbc);
		return panel;
	}

	private JPanel createSection(String title) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createLineBorder(Color.LIGHT_GRAY),
						title,
						TitledBorder.LEFT,
						TitledBorder.TOP,
						new Font("SansSerif", Font.BOLD, 16)),
				new EmptyBorder(8, 12, 8, 12)));
		return panel;
	}

	private GridBagConstraints constraints() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(6, 6, 6, 6);
		return gbc;
	}

	private JLabel header(String text) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(Font.BOLD, 16.0f));
		return label;
	}

	private static JLabel settingLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(Font.PLAIN, 16.0f));
		return label;
	}

	private void addPairedRow(
			JPanel panel,
			int row,
			String label,
			JTextField normalField,
			JTextField vipField) {
		GridBagConstraints gbc = constraints();
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(settingLabel(label), gbc);
		gbc.gridx = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(normalField, gbc);
		gbc.gridx = 2;
		panel.add(vipField, gbc);
	}

	private void addCheckBoxRow(
			JPanel panel, int row, String label, JCheckBox checkBox) {
		GridBagConstraints gbc = constraints();
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(settingLabel(label), gbc);

		gbc.gridx = 3;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(6, 20, 6, 6);
		panel.add(checkBox, gbc);
	}

	public void loadSettings() {
		AppSettings.Settings settings = AppSettings.get();
		normalBorrowDaysField.setText(
				String.valueOf(settings.normalMaxBorrowDays()));
		vipBorrowDaysField.setText(
				String.valueOf(settings.vipMaxBorrowDays()));
		normalBorrowBooksField.setText(
				String.valueOf(settings.normalMaxBorrowBooks()));
		vipBorrowBooksField.setText(
				String.valueOf(settings.vipMaxBorrowBooks()));
		normalReserveBooksField.setText(
				String.valueOf(settings.normalMaxReserveBooks()));
		vipReserveBooksField.setText(
				String.valueOf(settings.vipMaxReserveBooks()));
		dueNotificationBox.setSelected(settings.dueNotificationEnabled());
		dueNotificationDaysField.setText(
				settings.dueNotificationDays() > 0
						? String.valueOf(settings.dueNotificationDays())
						: "");
		reservationNotificationBox.setSelected(
				settings.reservationNotificationEnabled());
		overdueNotificationBox.setSelected(
				settings.overdueNotificationEnabled());
		suspensionDurationBox.setSelected(settings.suspensionDays() > 0);
		suspensionDaysField.setText(
				settings.suspensionDays() > 0
						? String.valueOf(settings.suspensionDays())
						: "");
		vipImmediateCancellationBox.setSelected(
				settings.vipImmediateCancellation());
		securityKeyField.setText(settings.securityKey());
		updateDueDaysEnabled();
		updateSuspensionDaysEnabled();
	}

	private void updateDueDaysEnabled() {
		boolean visible = dueNotificationBox.isSelected();
		dueNotificationDaysLabel.setVisible(visible);
		dueNotificationDaysField.setVisible(visible);
		dueNotificationDaysField.getParent().revalidate();
		dueNotificationDaysField.getParent().repaint();
	}

	private void updateSuspensionDaysEnabled() {
		boolean visible = suspensionDurationBox.isSelected();
		suspensionDaysField.setVisible(visible);
		suspensionDaysUnitLabel.setVisible(visible);
		suspensionDaysField.getParent().revalidate();
		suspensionDaysField.getParent().repaint();
	}

	private void saveSettings() {
		try {
			AppSettings.Settings settings = new AppSettings.Settings(
					positive(normalBorrowDaysField, "NORMAL borrow days"),
					positive(vipBorrowDaysField, "VIP borrow days"),
					positive(normalBorrowBooksField, "NORMAL borrowing books"),
					positive(vipBorrowBooksField, "VIP borrowing books"),
					positive(normalReserveBooksField, "NORMAL reserve books"),
					positive(vipReserveBooksField, "VIP reserve books"),
					dueNotificationBox.isSelected(),
					dueNotificationBox.isSelected()
							? positive(
									dueNotificationDaysField,
									"Due notification days")
							: 0,
					reservationNotificationBox.isSelected(),
					overdueNotificationBox.isSelected(),
					suspensionDurationBox.isSelected()
							? positive(
									suspensionDaysField,
									"Suspension duration")
							: 0,
					vipImmediateCancellationBox.isSelected(),
					securityKey());
			AppSettings.save(settings);
			JOptionPane.showMessageDialog(
					this,
					"Settings saved successfully.",
					"Save Succeed",
					JOptionPane.PLAIN_MESSAGE);
		} catch (IllegalArgumentException exception) {
			JOptionPane.showMessageDialog(
					this,
					exception.getMessage(),
					"Invalid Settings",
					JOptionPane.ERROR_MESSAGE);
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to save settings.",
					"Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void resetToDefaults() {
		try {
			AppSettings.save(AppSettings.DEFAULTS);
			loadSettings();
			JOptionPane.showMessageDialog(
					this,
					"Settings restored to their default values.",
					"Defaults Restored",
					JOptionPane.PLAIN_MESSAGE);
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to restore default settings.",
					"Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private int positive(JTextField field, String name) {
		try {
			int value = Integer.parseInt(field.getText().trim());
			if (value < 1) {
				throw new NumberFormatException();
			}
			return value;
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					name + " must be a positive whole number.");
		}
	}

	private String securityKey() {
		String key = securityKeyField.getText().trim();
		if (key.isEmpty() || key.matches(".*\\s.*")) {
			throw new IllegalArgumentException(
					"Security key cannot be empty or contain spaces.");
		}
		return key;
	}
}
