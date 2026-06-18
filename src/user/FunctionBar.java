package user;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;

public class FunctionBar extends JPanel{
	private static final long serialVersionUID = 1L;
	private TabButton profileButton;
	private TabButton searchButton;
	private TabButton returnBookButton;
	private TabButton personalRecordButton;
	private TabButton bookRecordButton;
	private TabButton[] buttonsList;
	
	/**
	 *	Place the components onto the function bar.
	 **/
	public FunctionBar() {
		// Set the layout type to Grid Layout.
		super.setLayout(new GridLayout(1, 5, 0, 10));
		super.setPreferredSize(new Dimension(0, 50));
		
		// Create the function buttons.
		profileButton = new TabButton("My Profile");
		searchButton = new TabButton("Search Book");
		returnBookButton = new TabButton("Return Book");
		personalRecordButton = new TabButton("My Records");
		bookRecordButton = new TabButton("Book List");
		buttonsList = new TabButton[] {
				profileButton,
				searchButton,
				returnBookButton,
				personalRecordButton,
				bookRecordButton
		};
		for (TabButton button : buttonsList) {
			button.setFont(button.getFont().deriveFont(15.0f));
		}
		
		setSelectedButton(profileButton);
		
		super.add(profileButton);
		super.add(searchButton);
		super.add(returnBookButton);
		super.add(personalRecordButton);
		super.add(bookRecordButton);
	}

	public TabButton getProfileButton() {return profileButton;}
	public TabButton getSearchButton() {return searchButton;}
	public TabButton getReturnBookButton() {return returnBookButton;}
	public TabButton getPersonalRecordButton() {return personalRecordButton;}
	public TabButton getBookRecordButton() {return bookRecordButton;}

	/**
	 *  Set the button btn selected, and update all buttons of its left and right state.\
	 *  @param btn the button being selected.
	 **/
	public void setSelectedButton(TabButton btn) {
		for (int i = 0; i < buttonsList.length; i++) {
			buttonsList[i].setSelectedTab(buttonsList[i] == btn);
			
			boolean right = (i < buttonsList.length - 1 && buttonsList[i+1] == btn) || (i == buttonsList.length - 1);
			buttonsList[i].setBesides(/* left, */ right);
		}
		super.repaint();
	}
}
