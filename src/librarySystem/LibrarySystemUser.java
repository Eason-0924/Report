package librarySystem;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import login.Login;
import system.StartSystem;
import system.SuspensionManager;
import system.User;
import user.*;

/**
 * 
 * @ClassName librarySystemUser
 * 
 *            Create library system page for student.
 * 
 **/

public class LibrarySystemUser extends JFrame {

	private static final long serialVersionUID = 1L;

	private FunctionBar functionBarPanel;

	private Profile profilePanel;

	private JPanel contentPanel;

	private CardLayout cardLayout;

	public static final int panelSize_x = 1200;

	public static final int panelSize_y = 800;

	public static final Color background = new Color(153, 204, 255);

	public LibrarySystemUser(int user_id) {

		// Get the information of the user.

		User user = getUser(user_id);

		System.out.println("User's name: " + user.getName());

		// Settings of the login frame.

		super.setTitle("Library System - student");

		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create the content panel.

		// Create the content panel.
		cardLayout = new CardLayout();
		contentPanel = new JPanel(cardLayout);

		// 1. Pass user_id to SearchBook so it knows who is borrowing
		SearchBook searchPanel = new SearchBook(user_id);
		ReturnBook returnBookPanel = new ReturnBook(user_id);
		profilePanel = new Profile(user, user_id, (bookId, title) -> {
			cardLayout.show(contentPanel, "search");
			functionBarPanel.setSelectedButton(
					functionBarPanel.getSearchButton());
			searchPanel.openBorrowDialog(bookId, title);
		}, (bookId, _) -> {
			returnBookPanel.openReturnBook(bookId);
			cardLayout.show(contentPanel, "return");
			functionBarPanel.setSelectedButton(
					functionBarPanel.getReturnBookButton());
		}, () -> {
			new Login();
			dispose();
		});
		PersonalRecord personalRecordPanel =
				new PersonalRecord(user_id);

		contentPanel.add(profilePanel, "profile");
		contentPanel.add(searchPanel, "search"); // Use the updated searchPanel
		// REMOVED: contentPanel.add(new BorrowBook(user_id), "borrow"); // No longer
		// needed
		contentPanel.add(returnBookPanel, "return");
		contentPanel.add(personalRecordPanel, "personal");
		contentPanel.add(new BookList(), "book");

		// Create the left function bar.
		functionBarPanel = new FunctionBar();
		// Add action listener to the buttons.
		addActionListener(functionBarPanel.getProfileButton(), "profile");
		functionBarPanel.getProfileButton().addActionListener(_ ->
				profilePanel.refreshNotifications());
		addActionListener(functionBarPanel.getSearchButton(), "search");
		// REMOVED: AddActionListener(functionBarPanel.getBorrowBookButton(), "borrow");
		// // No longer needed
		addActionListener(functionBarPanel.getReturnBookButton(), "return");
		addActionListener(functionBarPanel.getPersonalRecordButton(), "personal");
		functionBarPanel.getPersonalRecordButton().addActionListener(_ ->
				personalRecordPanel.loadRecords());
		addActionListener(functionBarPanel.getBookRecordButton(), "book");

		// Create the border panel.

		JPanel borderPanel = new JPanel(new BorderLayout());

		// borderPanel.setBounds(0, 0, panelSize_x, panelSize_y);

		borderPanel.setPreferredSize(new Dimension(panelSize_x, panelSize_y));

		// Add the pages to the border panel

		borderPanel.add(functionBarPanel, BorderLayout.NORTH);

		borderPanel.add(contentPanel, BorderLayout.CENTER);

		// Show the login page

		super.setContentPane(borderPanel);

		// Make the frame to fix the panel size.

		super.pack();

		super.setResizable(false);

		super.setLocationRelativeTo(null);

		super.setVisible(true);

	}

//  public static void main(String[] args) {

//      new librarySystemUser(1);

//  }

	/**
	 * 
	 * Addd action listener to the buttton, changing the content panel and the
	 * selected button in function bar.
	 *
	 * 
	 * 
	 * @param btn   The button that be added an action listener.
	 * 
	 * @param panel The corresponding panel to change;
	 * 
	 **/

	private void addActionListener(TabButton btn, String panel) {
		btn.addActionListener(_ -> {
			cardLayout.show(contentPanel, panel);
			functionBarPanel.setSelectedButton(btn);
		});
	}

	private User getUser(int user_id) {

		// Get the user's imformation.

		String sql = "SELECT * FROM users WHERE user_id = ?";

		try {
			SuspensionManager.refreshUserStatus(user_id);
		} catch (SQLException exception) {
			exception.printStackTrace();
		}

		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {

			statement.setString(1, String.valueOf(user_id));

			// Search for the user_id.

			try (ResultSet resultSet = statement.executeQuery()) {

				// if the user_id exists, get the corresponding name.

				if (resultSet.next()) {

					return new User(resultSet.getString("student_no"),

							resultSet.getString("name"),

							resultSet.getString("password"),

							system.User.Role_level.valueOf(resultSet.getString("role_level")),

							resultSet.getString("created_at"),

							system.User.Status.valueOf(resultSet.getString("status")),

							resultSet.getString("suspended_until")

					);

				}

			}

		} catch (SQLException ex) {

			ex.printStackTrace();

			JOptionPane.showMessageDialog(this, "Failed to get user informations", "ERROR", JOptionPane.ERROR_MESSAGE);

		}

		return null;

	}

}
