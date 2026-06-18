package login;

import java.awt.CardLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;

import system.StartSystem;

/**
 * @ClassName login 
 * Create and show a login page (using Java Swing).
 **/
public class LoginV2 extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel base; // The main panel which is set CardLayout.
	private CardLayout card; // The manager of CardLayout
	private LoginSelect loginSelectPanel;
	private LoginUser loginUserPanel;

	public LoginV2() {
		// Settings of the login frame.
		super.setTitle("LOGIN");
		super.setSize(350, 200);
		super.setResizable(false);
		super.setLocationRelativeTo(null);
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Create a manager panel of card layout.
		card = new CardLayout(5, 5);
		base = new JPanel(card);

		// Create a user login panel.
		loginUserPanel = new LoginUser();
		
		// Create a select login panel
		loginSelectPanel = new LoginSelect();
		
		// Add the panels to base panel.
		base.add(loginSelectPanel, "p1");
		base.add(loginUserPanel, "p2");

		// Show the login page
		super.add(base);
		super.setVisible(true);
		
		// Monitor the user button (adding ActionListenser).
		loginSelectPanel.studentBotton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show the user login page.
				card.show(base, "p2");
			}
		});
		
		// Monitor the login button (adding ActionListener).
		loginUserPanel.loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get username and password.
				String username = loginUserPanel.studentText.getText();
				String password = new String(loginUserPanel.passwordText.getPassword());

				try {
					// Verify username and password.
					if (validLogin(username, password)) {
						loginSuccess();
					} else {
						loginFailure();
					}
				} catch (HeadlessException he) {
					he.printStackTrace();
				}
			}
		});
	}

	

	/**
	 *  validLogin()
	 *  To validate the username and password.
	 *
	 *  @param username The student_no enter by user.
	 *  @param password The password enter by user.
	 *  @return whether the login is valid.
	 **/
	private boolean validLogin(String username, String password) {
		// Get the informations corresponding to the username.
		String sql =
				"SELECT * FROM users WHERE BINARY student_no = BINARY ?";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)){
			statement.setString(1, username);
			// Search for the username.
			try (ResultSet resultSet = statement.executeQuery()) {
				// if the username exists, get the correct password from table.
				if (resultSet.next()) {
					String correctPassword = resultSet.getString("password");
					// The password matches, the login is valid.
					if (correctPassword.equals(password)) {
						return true;
					} else {
						// The password is wrong, show an failure message.
						JOptionPane.showMessageDialog(this, "Password incorrect.", "Login Failed", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					// The username does not matchs to any in users table, show an failure message.
					JOptionPane.showMessageDialog(this, "Account does not exist.", "Login Failed", JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "登入失敗", "錯誤", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	private static void loginSuccess() {
		System.out.println("Login Success.");
	}

	private static void loginFailure() {
		System.out.println("Login Failure.");
	}
}
