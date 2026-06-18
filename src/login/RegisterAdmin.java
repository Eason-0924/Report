package login;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class RegisterAdmin extends JPanel {
	private static final long serialVersionUID = 1L;
	public JLabel adminLabel;
	public JTextField adminText;
	public JLabel passwordLabel;
	public JPasswordField passwordText;
	public JLabel securityKeyLabel;
	public JTextField securityKeyText;
	public JButton registerButton;
	public JButton backButton;
	public JLabel titleLabel;

	/**
	 * registerUser() Place the components onto the user register panel.
	 **/
	public RegisterAdmin() {
		// Set the layout of login page to GridBaglayout.
		super.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		// Set the constraints of GridBagLayout.
		gbc.insets = new Insets(5, 10, 5, 10);
		gbc.weighty = 1.0;

		// 1. Create a title label.
		titleLabel = new JLabel("Register a admin account...");
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		super.add(titleLabel, gbc);

		// Create a icon of user.
		ImageIcon idPNG = new ImageIcon("lib/Icons/cog.png");
		// Rescale the image.
		Image scale1 = idPNG.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
		ImageIcon idIcon = new ImageIcon(scale1);

		// Create a label of user with the user icon.
		adminLabel = new JLabel("Admin username: ", idIcon, JLabel.LEFT);
		adminLabel.setIconTextGap(8);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTH;
		super.add(adminLabel, gbc);

		// Create a text field of user.
		adminText = new JTextField(20);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		super.add(adminText, gbc);

		// Create a icon of password.
		ImageIcon pwPNG = new ImageIcon("lib/Icons/key.png");
		// Rescale the image.
		Image scale2 = pwPNG.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
		ImageIcon pwIcon = new ImageIcon(scale2);

		// Create a label of password.
		passwordLabel = new JLabel("Password: ", pwIcon, JLabel.LEFT);
		passwordLabel.setIconTextGap(8);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		super.add(passwordLabel, gbc);

		// Create a password field (alike text field but replaced by dots).
		passwordText = new JPasswordField(20);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		super.add(passwordText, gbc);
		
		// Create a icon of security key.
		ImageIcon skPNG = new ImageIcon("lib/Icons/shield.png");
		// Rescale the image.
		Image scale3 = skPNG.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
		ImageIcon skIcon = new ImageIcon(scale3);
		
		// Create a lable of security key.
		securityKeyLabel = new JLabel("Security Key: ", skIcon, JLabel.LEFT);
		securityKeyLabel.setIconTextGap(8);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		super.add(securityKeyLabel, gbc);
		
		// Create a text field for security key.
		securityKeyText = new JTextField(20);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		super.add(securityKeyText, gbc);

		// Create a back button.
		backButton = new JButton("Back");
		backButton.setFocusPainted(false);
		backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		super.add(backButton, gbc);

		// Create register button.
		registerButton = new JButton("Register");
		registerButton.setFocusPainted(false);
		registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.EAST;
		super.add(registerButton, gbc);

		// Create a border.
		EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
		super.setBorder(emptyBorder);
	}
}
