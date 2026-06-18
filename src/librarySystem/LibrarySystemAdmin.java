package librarySystem;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import admin.FunctionBar;
import admin.Analysis;
import admin.ManageBook;
import admin.ManageRecord;
import admin.ManageUser;
import admin.Profile;
import admin.Settings;
import login.Login;
import system.StartSystem;
import user.TabButton;

public class LibrarySystemAdmin extends JFrame {
	private static final long serialVersionUID = 1L;

	public static final int PANEL_SIZE_X = 1200;
	public static final int PANEL_SIZE_Y = 800;

	private final FunctionBar functionBarPanel;
	private final JPanel contentPanel;
	private final CardLayout cardLayout;
	private final Profile profilePanel;
	private final ManageRecord manageRecordPanel;
	private final Analysis analysisPanel;
	private final Settings settingsPanel;

	public LibrarySystemAdmin(int adminId) {
		String username = getAdminUsername(adminId);

		super.setTitle("Library System - administrator");
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		cardLayout = new CardLayout();
		contentPanel = new JPanel(cardLayout);

		profilePanel = new Profile(username, () -> {
			new Login();
			dispose();
		});
		contentPanel.add(profilePanel, "profile");
		contentPanel.add(new ManageBook(), "books");
		contentPanel.add(new ManageUser(), "users");
		manageRecordPanel = new ManageRecord();
		contentPanel.add(manageRecordPanel, "records");
		analysisPanel = new Analysis();
		contentPanel.add(analysisPanel, "analysis");
		settingsPanel = new Settings();
		contentPanel.add(settingsPanel, "settings");

		functionBarPanel = new FunctionBar();
		addTabAction(functionBarPanel.getProfileButton(), "profile");
		functionBarPanel.getProfileButton().addActionListener(_ ->
				profilePanel.refreshNotifications());
		addTabAction(functionBarPanel.getBookManagementButton(), "books");
		addTabAction(functionBarPanel.getUserManagementButton(), "users");
		addTabAction(functionBarPanel.getManageRecordButton(), "records");
		functionBarPanel.getManageRecordButton().addActionListener(_ ->
				manageRecordPanel.refreshSelectedTab());
		addTabAction(functionBarPanel.getAnalysisButton(), "analysis");
		functionBarPanel.getAnalysisButton().addActionListener(_ ->
				analysisPanel.loadTopics());
		addTabAction(functionBarPanel.getSettingsButton(), "settings");
		functionBarPanel.getSettingsButton().addActionListener(_ ->
				settingsPanel.loadSettings());

		JPanel borderPanel = new JPanel(new BorderLayout());
		borderPanel.setPreferredSize(new Dimension(PANEL_SIZE_X, PANEL_SIZE_Y));
		borderPanel.add(functionBarPanel, BorderLayout.NORTH);
		borderPanel.add(contentPanel, BorderLayout.CENTER);

		super.setContentPane(borderPanel);
		super.pack();
		super.setResizable(false);
		super.setLocationRelativeTo(null);
		super.setVisible(true);
	}

	private void addTabAction(TabButton button, String panelName) {
		button.addActionListener(_ -> {
			cardLayout.show(contentPanel, panelName);
			functionBarPanel.setSelectedButton(button);
		});
	}

	private String getAdminUsername(int id) {
		String sql = "SELECT username FROM admins WHERE admin_id = ?";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getString("username");
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to get administrator information.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return "Administrator";
	}
}
