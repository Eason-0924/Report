package login;

import system.Admin;
import system.AppSettings;
import system.StartSystem;
import system.User;
import java.awt.*;
import java.awt.event.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;

/**
 * @ClassName login Create and show a login page (using Java Swing).
 **/
public class Login extends JFrame {
	private static final long serialVersionUID = 1L;
	private JLayeredPane layeredPane;
	private LoginSelect loginSelectPanel;
	private LoginUser loginUserPanel;
	private LoginAdmin loginAdminPanel;
	private RegisterUser registerUserPanel;
	private RegisterAdmin registerAdminPanel;
	private int user_id = 0;
	private int admin_id = 0;

	public static final int panelSize_x = 400;
	public static final int panelSize_y = 250;

	public Login() {
		// Settings of the login frame.
		super.setTitle("LOGIN");
		super.setResizable(false);
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create a manager panel of layered layout.
		layeredPane = new JLayeredPane();
		layeredPane.setLayout(null);
		layeredPane.setPreferredSize(new Dimension(panelSize_x, panelSize_y));

		// Create a select panel.
		loginSelectPanel = new LoginSelect();
		loginSelectPanel.setBounds(0, 0, panelSize_x, panelSize_y);

		// Create a user login panel.
		loginUserPanel = new LoginUser();
		loginUserPanel.setBounds(0, 0, panelSize_x, panelSize_y);

		// Create a admin login panel.
		loginAdminPanel = new LoginAdmin();
		loginAdminPanel.setBounds(0, 0, panelSize_x, panelSize_y);

		// Create a user register panel.
		registerUserPanel = new RegisterUser();
		registerUserPanel.setBounds(0, 0, panelSize_x, panelSize_y);

		// Create a user register panel.
		registerAdminPanel = new RegisterAdmin();
		registerAdminPanel.setBounds(0, 0, panelSize_x, panelSize_y);

		// Add the panels to base panel.
		layeredPane.add(loginSelectPanel, Integer.valueOf(0));
		layeredPane.add(loginUserPanel, Integer.valueOf(1));
		layeredPane.add(loginAdminPanel, Integer.valueOf(2));
		layeredPane.add(registerUserPanel, Integer.valueOf(3));
		layeredPane.add(registerAdminPanel, Integer.valueOf(4));

		// Initially show select login panel only.
		loginSelectPanel.setVisible(true);
		loginUserPanel.setVisible(false);
		loginAdminPanel.setVisible(false);
		registerUserPanel.setVisible(false);
		registerAdminPanel.setVisible(false);

		// Show the login page
		super.setContentPane(layeredPane);
		// Make the frame to fix the panel size.
		super.pack();
		super.setLocationRelativeTo(null);
		super.setVisible(true);

		// Monitor the user button (adding ActionListenser).
		loginSelectPanel.studentBotton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show the user login page.
				switchFromTo(loginSelectPanel, loginUserPanel, true);
			}
		});

		// Monitor the admin button (adding Action Listener).
		loginSelectPanel.adminButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show the admin login page.
				switchFromTo(loginSelectPanel, loginAdminPanel, true);
			}
		});

		// Monitor the login button for user (adding ActionListener).
		loginUserPanel.loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get student id and password.
				String studentNo = loginUserPanel.studentText.getText();
				String password = new String(loginUserPanel.passwordText.getPassword());

				try {
					// Verify student id and password.
					switch (validUserLogin(studentNo, password)) {
					case VALID:
						loginSuccessUser();
						break;
					case PASSWORD_INCORRECT:
						loginUserPanel.passwordText.setText("");
						break;
					case ACCOUNT_NOT_FOUND:
					default:
						loginUserPanel.studentText.setText("");
						loginUserPanel.passwordText.setText("");
						break;
					}
				} catch (HeadlessException he) {
					he.printStackTrace();
				}
			}
		});

		// Monitor the register button for user (add ActionListener).
		loginUserPanel.registerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show the user register page.
				switchFromTo(loginUserPanel, registerUserPanel, true);
				loginUserPanel.studentText.setText("");
				loginUserPanel.passwordText.setText("");
			}
		});

		// Monitor the login button for admin (adding ActionListener).
		loginAdminPanel.loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get username and password
				String username = loginAdminPanel.adminText.getText();
				String password = new String(loginAdminPanel.passwordText.getPassword());

				try {
					// Verify username and password.
					switch (validAdminLogin(username, password)) {
					case VALID:
						loginSuccessAdmin();
						break;
					case PASSWORD_INCORRECT:
						loginAdminPanel.passwordText.setText("");
						break;
					case ACCOUNT_NOT_FOUND:
					default:
						loginAdminPanel.adminText.setText("");
						loginAdminPanel.passwordText.setText("");
						break;
					}
				} catch (HeadlessException he) {
					he.printStackTrace();
				}
			}
		});

		// Monitor the register button for admin (add ActionListener).
		loginAdminPanel.registerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show the admin register page.
				switchFromTo(loginAdminPanel, registerAdminPanel, true);
				loginAdminPanel.adminText.setText("");
				loginAdminPanel.passwordText.setText("");
			}
		});

		// Monitor the back button on user login page (adding ActionListener).
		loginUserPanel.backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show the login select page.
				switchFromTo(loginUserPanel, loginSelectPanel, false);
				loginUserPanel.studentText.setText("");
				loginUserPanel.passwordText.setText("");
			}
		});

		// Monitor the back button on admin login page (adding ActionListener).
		loginAdminPanel.backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show the login selecct page.
				switchFromTo(loginAdminPanel, loginSelectPanel, false);
				loginAdminPanel.adminText.setText("");
				loginAdminPanel.passwordText.setText("");
			}
		});

		// Monitor the back button on user register page (adding ActionListener).
		registerUserPanel.backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show the user login page.
				switchFromTo(registerUserPanel, loginUserPanel, false);
				registerUserPanel.studentText.setText("");
				registerUserPanel.passwordText.setText("");
			}
		});

		// Monitor the back button on admin register page (adding ActionListener).
		registerAdminPanel.backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show the user login page.
				switchFromTo(registerAdminPanel, loginAdminPanel, false);
				registerAdminPanel.adminText.setText("");
				registerAdminPanel.passwordText.setText("");
			}
		});

		// Monitor the register button on user register page (adding ActionListener).
		registerUserPanel.registerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get student_no and password
				String studentNo = registerUserPanel.studentText.getText();
				String name = registerUserPanel.nameText.getText();
				String password = new String(registerUserPanel.passwordText.getPassword());
				
				try {
					switch (validUserRegister(studentNo, name, password)) {
					case VALID:
						userRegister(studentNo, name, password);
						break;
					case ACCOUNT_EXISTS:
						registerUserPanel.studentText.setText("");
						registerUserPanel.nameText.setText("");
						registerUserPanel.passwordText.setText("");
						break;
					default:
						break;
					}
				} catch (HeadlessException he) {
					he.printStackTrace();
				}
			}
		});

		// Monitor the register button on admin register page (adding ActionListener).
		registerAdminPanel.registerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get username, password, and security key.
				String username = registerAdminPanel.adminText.getText();
				String password = new String(registerAdminPanel.passwordText.getPassword());
				String security = registerAdminPanel.securityKeyText.getText();

				try {
					switch (validAdminRegister(username, password, security)) {
					case VALID:
						adminRegister(username, password);
						break;
					case ACCOUNT_EXISTS:
						registerAdminPanel.adminText.setText("");
						registerAdminPanel.passwordText.setText("");
						break;
					default:
						break;
					}
				} catch (HeadlessException he) {
					he.printStackTrace();
				}
			}
		});
	}

	/**
	 * Switch from the current panel to the next panel.
	 *
	 * @param panelCurr Current panel
	 * @param panelNext Next panel
	 * @param forward   true for swtich forward; false for switch backward.
	 **/
	private void switchFromTo(JPanel panelCurr, JPanel panelNext, Boolean forward) {
		// The initial x index of the next panel.
		int initX;
		// The increment of x index.
		int increment;
		// Set the two variable in different forward cases.
		if (forward) {
			initX = 350;
			increment = -10;
		} else {
			initX = -350;
			increment = 10;
		}

		// Set the panel initially off-screen.
		panelNext.setLocation(initX, 0);

		// Set a timer for next panel.
		Timer timerNext = new Timer(5, null);
		final int[] positionNext = { initX };

		timerNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Add a animation.
				positionNext[0] += increment;
				panelNext.setLocation(positionNext[0], 0);

				if (forward) {
					if (positionNext[0] <= 0) {
						timerNext.stop();
					}
				} else {
					if (positionNext[0] >= 0) {
						timerNext.stop();
					}
				}
			}
		});

		// Set a timer for current panelCurr.
		Timer timerCurr = new Timer(5, null);
		final int[] positionCurr = { 0 };

		timerCurr.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Add a animation.
				positionCurr[0] += increment;
				panelCurr.setLocation(positionCurr[0], 0);

				if (forward) {
					if (positionCurr[0] <= -initX) {
						panelCurr.setVisible(false);
						timerCurr.stop();
					}
				} else {
					if (positionCurr[0] >= -initX) {
						panelCurr.setVisible(false);
						timerCurr.stop();
					}
				}
			}
		});

		// Show the panel.
		panelNext.setVisible(true);
		// Switch from current panel to next panel.
		timerCurr.start();
		timerNext.start();
	}

	enum validation {
		VALID, ACCOUNT_NOT_FOUND, PASSWORD_INCORRECT, ACCOUNT_EXISTS, 
		ACCOUNT_EMPTY, NAME_EMPTY, PASSWORD_EMPTY, SECURITY_KEY_EMPTY, SECURITY_INCORRECT
	}

	/**
	 * validUserLogin() To validate the student_no and password.
	 *
	 * @param studentNo The student_no entered edby user.
	 * @param password  The password entered by user.
	 * @return enum validation
	 **/
	private validation validUserLogin(String studentNo, String password) {
		// Get the informations corresponding to the studentID.
		String sql =
				"SELECT * FROM users WHERE BINARY student_no = BINARY ?";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setString(1, studentNo);
			// Search for the student_no.
			try (ResultSet resultSet = statement.executeQuery()) {
				// if the student_no exists, get the correct password from table.
				if (resultSet.next()) {
					String correctPassword = resultSet.getString("password");
					// The password matches, the login is valid.
					if (correctPassword.equals(password)) {
						this.user_id = resultSet.getInt("user_id");
						return validation.VALID;
					} else {
						// The password is wrong, show an failure message.
						JOptionPane.showMessageDialog(this, "Password incorrect.", "Login Failed",
								JOptionPane.ERROR_MESSAGE);
						return validation.PASSWORD_INCORRECT;
					}
				} else {
					// The studentID does not matchs to any in users table, show an failure message.
					JOptionPane.showMessageDialog(this, "Account does not exist.", "Login Failed",
							JOptionPane.ERROR_MESSAGE);
					return validation.ACCOUNT_NOT_FOUND;
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "登入失敗", "錯誤", JOptionPane.ERROR_MESSAGE);
		}
		return validation.ACCOUNT_NOT_FOUND;
	}

	/**
	 * validAdminLogin() To validate the username and password.
	 *
	 * @param username The username entered by user.
	 * @param password The password entered by user.
	 * @return enum validtion
	 **/
	private validation validAdminLogin(String username, String password) {
		// Get the informations corresponding to the studentID.
		String sql =
				"SELECT * FROM admins WHERE BINARY username = BINARY ?";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setString(1, username);
			// Search for the username.
			try (ResultSet resultSet = statement.executeQuery()) {
				// if the username exists, get the correct password from table.
				if (resultSet.next()) {
					String correctPassword = resultSet.getString("password");
					// The password matches, the login is valid.
					if (correctPassword.equals(password)) {
						admin_id = resultSet.getInt("admin_id");
						return validation.VALID;
					} else {
						// The password is wrong, show an failure message.
						JOptionPane.showMessageDialog(this, "Password incorrect.", "Login Failed",
								JOptionPane.ERROR_MESSAGE);
						return validation.PASSWORD_INCORRECT;
					}
				} else {
					// The studentID does not matchs to any in users table, show an failure message.
					JOptionPane.showMessageDialog(this, "Account does not exist.", "Login Failed",
							JOptionPane.ERROR_MESSAGE);
					return validation.ACCOUNT_NOT_FOUND;
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "登入失敗", "錯誤", JOptionPane.ERROR_MESSAGE);
		}
		return validation.ACCOUNT_NOT_FOUND;
	}

	/**
	 * validUserRegister() To validate whether the user register is acceptable.
	 *
	 * @param studentNo The student No. entered by user.
	 * @param name      The name entered by user.
	 * @param password  The password entered by user.
	 * @return enum validation
	 **/
	private validation validUserRegister(String studentNo, String name, String password) {
		// Check studentNo, name, and password is not blank.
		if (studentNo.equals("") || studentNo.matches(".*\\s.*")) {
			JOptionPane.showMessageDialog(this, "Please enter your student number.", "Register Failed",
					JOptionPane.ERROR_MESSAGE);
			return validation.ACCOUNT_EMPTY;
		}
		if (name.equals("") || name.matches(".*\\s.*")) {
			JOptionPane.showMessageDialog(this, "Please enter your name.", "Register Railed",
					JOptionPane.ERROR_MESSAGE);
			return validation.NAME_EMPTY;
		}
		if (password.equals("") || password.matches(".*\\s.*")) {
			JOptionPane.showMessageDialog(this, "Please enter your password.", "Register Failed",
					JOptionPane.ERROR_MESSAGE);
			return validation.PASSWORD_EMPTY;
		}

		// Get the informations corresponding to the studentID.
		String sql = "SELECT * FROM users WHERE student_no = ?";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setString(1, studentNo);
			// Search for the student_no.
			try (ResultSet resultSet = statement.executeQuery()) {
				// if the student_no exists, show error message.
				if (resultSet.next()) {
					JOptionPane.showMessageDialog(this, "Student No. already exists. Please try another one", "Register Failed",
							JOptionPane.ERROR_MESSAGE);
					return validation.ACCOUNT_EXISTS;
				} else {
					return validation.VALID;
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "登入失敗", "錯誤", JOptionPane.ERROR_MESSAGE);
		}
		return validation.ACCOUNT_EXISTS;
	}

	/**
	 * To validate whether the admin register is acceptable.
	 *
	 * @param username The username entered by user.
	 * @param password The password entered by user.
	 * @return
	 **/
	private validation validAdminRegister(String username, String password, String security) {
		// Check studentNo, name, and password is not blank.
		if (username.equals("") || username.matches(".*\\s.*")) {
			JOptionPane.showMessageDialog(this, "Please enter your username.", "Register Failed",
					JOptionPane.ERROR_MESSAGE);
			return validation.ACCOUNT_EMPTY;
		}
		if (password.equals("") || password.matches(".*\\s.*")) {
			JOptionPane.showMessageDialog(this, "Please enter your password.", "Register Failed",
					JOptionPane.ERROR_MESSAGE);
			return validation.ACCOUNT_EMPTY;
		}
		if (security.equals("") || security.matches(".*\\s.*")) {
			JOptionPane.showMessageDialog(this, "Please enter your security key.", "Register Failed",
					JOptionPane.ERROR_MESSAGE);
			return validation.SECURITY_KEY_EMPTY;
		}

		// Get the informations corresponding to the username.
		String sql = "SELECT * FROM admins WHERE username = ?";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setString(1, username);
			// Search for the username.
			try (ResultSet resultSet = statement.executeQuery()) {
				// if the username exists, show error message.
				if (resultSet.next()) {
					JOptionPane.showMessageDialog(this, "Username already exists. Please try another one,", "Register Failed",
							JOptionPane.ERROR_MESSAGE);
					return validation.ACCOUNT_EXISTS;
				} else {
					if (security.equals(AppSettings.get().securityKey())) {
						return validation.VALID;
					} else {
						JOptionPane.showMessageDialog(this, "Security key inccorect, please contact the administrator.", "Register failed", 
								JOptionPane.ERROR_MESSAGE);
						return validation.SECURITY_INCORRECT;
					}
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "登入失敗", "錯誤", JOptionPane.ERROR_MESSAGE);
		}
		return validation.ACCOUNT_EXISTS;
	}

	/**
	 * Register a new user and insert into the users table.
	 *
	 * @param studentNo The student No. entered by user.
	 * @param name      The name entered by user.
	 * @param password  The password entered by user.
	 **/
	private void userRegister(String studentNo, String name, String password) {
		if (studentNo.equals("") || studentNo.matches(".*\\s.*") || 
				name.equals("") || name.matches(".*\\s.*") || 
				password.equals("") || password.matches(".*\\s.*")) {
			JOptionPane.showMessageDialog(this, "Invalid Format", "Register Failed", JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			User u = new User(studentNo, name, password, "NORMAL");
			u.insert();
			JOptionPane.showMessageDialog(this, "Register Succeed. Please login again.", "Register Succeed",
					JOptionPane.PLAIN_MESSAGE);
			switchFromTo(registerUserPanel, loginUserPanel, false);
			loginUserPanel.studentText.setText(studentNo);
		}
	}

	/**
	 *  Register a new admin and insert into the admins table.
	 *
	 *  @param username The username entered by user.
	 *  @param password The password entered by user.
	 **/
	private void adminRegister(String username, String password) {
		if (username.equals("") || username.matches(".*\\s.*") ||
				password.equals("") || password.matches(".*\\s.*")) {
			JOptionPane.showMessageDialog(this, "Invalid Format", "Register Failed", JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			Admin a = new Admin(username, password);
			a.insert();
			JOptionPane.showMessageDialog(this, "Register Succeed. Please login again.", "Register Succeed",
					JOptionPane.PLAIN_MESSAGE);
			switchFromTo(registerAdminPanel, loginAdminPanel, false);
			loginUserPanel.studentText.setText(username);
		}
	}

	private void loginSuccessUser() {
		System.out.println("Login as Student - Succeed");
		this.dispose();
		StartSystem.launchSystemUser(this.user_id);
	}
	
	private void loginSuccessAdmin() {
		System.out.println("Login as Admin - Succeed");
		this.dispose();
		StartSystem.launchSystemAdmin(this.admin_id);
	}
}
