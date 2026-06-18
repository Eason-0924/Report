package admin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;

import user.TabButton;

public class FunctionBar extends JPanel {
	private static final long serialVersionUID = 1L;

	public static final Color BACKGROUND = new Color(255, 182, 193);

	private final TabButton profileButton;
	private final TabButton bookManagementButton;
	private final TabButton userManagementButton;
	private final TabButton manageRecordButton;
	private final TabButton analysisButton;
	private final TabButton settingsButton;
	private final TabButton[] buttonsList;

	public FunctionBar() {
		super.setLayout(new GridLayout(1, 6, 0, 10));
		super.setPreferredSize(new Dimension(0, 50));

		profileButton = new TabButton("Admin Profile", BACKGROUND);
		bookManagementButton = new TabButton("Manage Books", BACKGROUND);
		userManagementButton = new TabButton("Manage Users", BACKGROUND);
		manageRecordButton = new TabButton("Manage Record", BACKGROUND);
		analysisButton = new TabButton("Analysis", BACKGROUND);
		settingsButton = new TabButton("Settings", BACKGROUND);

		buttonsList = new TabButton[] {
				profileButton,
				bookManagementButton,
				userManagementButton,
				manageRecordButton,
				analysisButton,
				settingsButton
		};

		for (TabButton button : buttonsList) {
			button.setFont(button.getFont().deriveFont(15.0f));
			super.add(button);
		}

		setSelectedButton(profileButton);
	}

	public TabButton getProfileButton() {
		return profileButton;
	}

	public TabButton getBookManagementButton() {
		return bookManagementButton;
	}

	public TabButton getUserManagementButton() {
		return userManagementButton;
	}

	public TabButton getManageRecordButton() {
		return manageRecordButton;
	}

	public TabButton getAnalysisButton() {
		return analysisButton;
	}

	public TabButton getSettingsButton() {
		return settingsButton;
	}

	public void setSelectedButton(TabButton selectedButton) {
		for (int i = 0; i < buttonsList.length; i++) {
			buttonsList[i].setSelectedTab(buttonsList[i] == selectedButton);

			boolean rightSelected =
					(i < buttonsList.length - 1
							&& buttonsList[i + 1] == selectedButton)
					|| i == buttonsList.length - 1;
			buttonsList[i].setBesides(rightSelected);
		}
		super.repaint();
	}
}
