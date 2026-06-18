package login;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginSelect extends JPanel {
	private static final long serialVersionUID = 1L;
	public JButton studentBotton;
	public JButton adminButton;
	
	/**
	 *  loginSelect()
	 *  Place the components onto the select login panel.
	 **/
	public LoginSelect() {
		
		// Set the layout type to GridLayout
		super.setLayout(new GridLayout(1, 2, 10, 10));
		
		// Create a button of user.
		ImageIcon userPNG = new ImageIcon("lib/Icons/user.png");
		// Rescale the image.
		Image scale1 = userPNG.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
		ImageIcon userIcon = new ImageIcon(scale1);
		studentBotton = new JButton("Student Login", userIcon);
		studentBotton.setFocusPainted(false);
		
		// Create a button of admin.
		ImageIcon adminPNG = new ImageIcon("lib/Icons/setting.png");
		// Rescale the image.
		Image scale2 =  adminPNG.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
		ImageIcon adminIcon = new ImageIcon(scale2);
		adminButton = new JButton("Admin Login", adminIcon);
		adminButton.setFocusPainted(false);
		
		// Set the position of Text in the buttons.
		studentBotton.setHorizontalTextPosition(SwingConstants.CENTER);
        studentBotton.setVerticalTextPosition(SwingConstants.BOTTOM);
        adminButton.setHorizontalTextPosition(SwingConstants.CENTER);
        adminButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        
        // Set a Hand cursor of the buttons
        studentBotton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        adminButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        //Add the buttons to the panel.
		super.add(studentBotton);
        super.add(adminButton);
		
		// Create a border.
		EmptyBorder emptyBorder = new EmptyBorder(20, 20, 20, 20);
		super.setBorder(emptyBorder);
	}
}
