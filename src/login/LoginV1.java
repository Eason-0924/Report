/*
 * login page
 * @version 1
 */
package login;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class LoginV1 {
	public static void main(String[] args) {
		// Create a JFrame.
		JFrame frame = new JFrame("Login");
		// Setting width and height of frame.
		frame.setSize(350, 200);
		// Set the frame to the center of the screen.
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Create a JPanel.
		JPanel panel = new JPanel();
		// Add the panel to the frame.
		frame.add(panel);
		// Place components.
		placeComponents(panel);
		frame.setVisible(true);
	}

	private static void placeComponents(JPanel panel) {
		panel.setLayout(null);
		
		// Create a label of user.
		JLabel userLabel = new JLabel("User: ");
		// Set the position of component (x, y, width, height).
		userLabel.setBounds(10, 20, 80, 25);
		panel.add(userLabel);
		
		// Create a text field of user.
		JTextField userText = new JTextField(20);
		userText.setBounds(100, 20, 165, 25);
		panel.add(userText);
		
		// Create a label of password.
		JLabel passwordLabel = new JLabel("Password: ");
		passwordLabel.setBounds(10, 50, 80, 25);
		panel.add(passwordLabel);
		
		// Create a password field (alike text field but replaced by dots).
		JPasswordField passwordText = new JPasswordField(20);
		passwordText.setBounds(100, 50, 165, 25);
		panel.add(passwordText);
		
		// Create login button.
		JButton loginButton = new JButton("Login");
		loginButton.setBounds(10, 80, 80, 25);
		panel.add(loginButton);
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get username and password.
				String username = userText.getText();
				String password = new String(passwordText.getPassword());
				
				// Verify username and password.
				if(validateCredentials(username, password)) {
					loginSuccess();
				} else {
					loginFailure();
				}
			}
			
			private boolean validateCredentials(String username, String password) {
				// Simple demonstration.
				return "admin".equals(username) && "password".equals(password);
			}
			
			private void loginSuccess() {
				System.out.println("Login Success.");
			}
			
			private void loginFailure() {
				System.out.println("Login Failure.");
			}
		});
		
	}	
}
