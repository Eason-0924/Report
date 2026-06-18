package admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import system.StartSystem;
import system.AppSettings;

public class Profile extends JPanel {
	private static final long serialVersionUID = 1L;

	private final String username;
	private final Runnable logoutAction;
	private JPanel notificationListPanel;
	private final List<SlideInPanel> profileAnimationItems =
			new ArrayList<>();
	private final List<SlideInPanel> notificationAnimationItems =
			new ArrayList<>();

	public Profile(String username, Runnable logoutAction) {
		this.username = username;
		this.logoutAction = logoutAction;

		super.setLayout(new GridLayout(1, 2, 15, 0));
		super.setBorder(new EmptyBorder(10, 10, 10, 10));
		super.setBackground(Color.WHITE);

		super.add(createProfilePanel());
		super.add(createNotificationPanel());
		refreshNotifications();
	}

	private JPanel createProfilePanel() {
		JPanel profilePanel = new JPanel(new GridBagLayout());
		profilePanel.setBackground(Color.WHITE);
		profilePanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY),
				"Admin Profile",
				TitledBorder.LEFT,
				TitledBorder.TOP,
				new Font("SansSerif", Font.BOLD, 16)));

		JPanel welcomePanel = new JPanel(new GridBagLayout());
		welcomePanel.setBackground(Color.WHITE);
		welcomePanel.setBorder(new EmptyBorder(20, 25, 20, 25));

		GridBagConstraints welcomeGbc = new GridBagConstraints();
		welcomeGbc.gridx = 0;
		welcomeGbc.anchor = GridBagConstraints.WEST;
		welcomeGbc.fill = GridBagConstraints.HORIZONTAL;
		welcomeGbc.weightx = 1.0;

		JLabel welcomeLabel = new JLabel("Welcome back,");
		welcomeLabel.setFont(
				welcomeLabel.getFont().deriveFont(Font.PLAIN, 18.0f));
		welcomePanel.add(welcomeLabel, welcomeGbc);

		welcomeGbc.gridy = 1;
		welcomeGbc.insets = new Insets(0, 0, 25, 0);
		JLabel usernameLabel = new JLabel(username);
		usernameLabel.setFont(
				usernameLabel.getFont().deriveFont(Font.BOLD, 28.0f));
		welcomePanel.add(usernameLabel, welcomeGbc);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(15, 15, 15, 15);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		SlideInPanel welcomeSlide = new SlideInPanel(welcomePanel);
		profileAnimationItems.add(welcomeSlide);
		profilePanel.add(welcomeSlide, gbc);

		JButton logoutButton = new JButton("Logout");
		logoutButton.setFont(
				logoutButton.getFont().deriveFont(Font.BOLD, 14.0f));
		logoutButton.setCursor(
				Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		logoutButton.addActionListener(_ -> logoutAction.run());

		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 20, 20, 20);
		gbc.weighty = 0;
		SlideInPanel logoutSlide = new SlideInPanel(logoutButton);
		profileAnimationItems.add(logoutSlide);
		profilePanel.add(logoutSlide, gbc);

		return profilePanel;
	}

	private JPanel createNotificationPanel() {
		JPanel notificationPanel = new JPanel(new BorderLayout());
		notificationPanel.setBackground(Color.WHITE);
		notificationPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY),
				"Notification",
				TitledBorder.LEFT,
				TitledBorder.TOP,
				new Font("SansSerif", Font.BOLD, 16)));

		notificationListPanel = new JPanel();
		notificationListPanel.setLayout(
				new BoxLayout(notificationListPanel, BoxLayout.Y_AXIS));
		notificationListPanel.setBackground(Color.WHITE);
		notificationListPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

		JScrollPane scrollPane = new JScrollPane(notificationListPanel);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(12);
		notificationPanel.add(scrollPane, BorderLayout.CENTER);
		return notificationPanel;
	}

	public void refreshNotifications() {
		notificationListPanel.removeAll();
		notificationAnimationItems.clear();
		if (!AppSettings.get().overdueNotificationEnabled()) {
			JLabel emptyLabel =
					new JLabel("Overdue notifications are disabled.");
			emptyLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
			addNotificationItem(emptyLabel);
			notificationListPanel.revalidate();
			notificationListPanel.repaint();
			playEntranceAnimations();
			return;
		}

		String sql =
				"SELECT b.title, u.name, u.student_no, " +
				"DATE(r.borrow_date) AS borrow_date, " +
				"DATE(r.due_date) AS due_date, " +
				"DATEDIFF(CURDATE(), DATE(r.due_date)) AS overdue_days " +
				"FROM borrow_records r " +
				"JOIN books b ON r.book_id = b.book_id " +
				"JOIN users u ON r.user_id = u.user_id " +
				"WHERE r.return_date IS NULL " +
				"AND DATE(r.due_date) < CURDATE() " +
				"ORDER BY r.due_date ASC";

		boolean hasNotification = false;
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				hasNotification = true;
				addNotificationItem(createNotificationBox(
						resultSet.getString("title"),
						resultSet.getString("name"),
						resultSet.getString("student_no"),
						resultSet.getString("borrow_date"),
						resultSet.getString("due_date"),
						resultSet.getInt("overdue_days")));
				notificationListPanel.add(
						Box.createRigidArea(new Dimension(0, 10)));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			JLabel errorLabel =
					new JLabel("Failed to load overdue notifications.");
			errorLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
			addNotificationItem(errorLabel);
			hasNotification = true;
		}

		if (!hasNotification) {
			JLabel emptyLabel = new JLabel("No overdue books.");
			emptyLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
			addNotificationItem(emptyLabel);
		}

		notificationListPanel.revalidate();
		notificationListPanel.repaint();
		playEntranceAnimations();
	}

	private void addNotificationItem(Component component) {
		SlideInPanel slidePanel = new SlideInPanel(component);
		notificationAnimationItems.add(slidePanel);
		notificationListPanel.add(slidePanel);
	}

	private void playEntranceAnimations() {
		int delay = 0;
		for (SlideInPanel item : profileAnimationItems) {
			item.play(delay);
			delay += 80;
		}

		delay = 80;
		for (SlideInPanel item : notificationAnimationItems) {
			item.play(delay);
			delay += 70;
		}
	}

	private JPanel createNotificationBox(
			String title,
			String studentName,
			String studentNumber,
			String borrowDate,
			String dueDate,
			int overdueDays) {
		JPanel box = new JPanel(new GridLayout(4, 1, 0, 5));
		Dimension boxSize = new Dimension(520, 145);
		box.setPreferredSize(boxSize);
		box.setMinimumSize(boxSize);
		box.setMaximumSize(boxSize);
		box.setAlignmentX(CENTER_ALIGNMENT);
		box.setBackground(new Color(255, 230, 230));
		box.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.RED, 2),
				new EmptyBorder(10, 10, 10, 10)));

		String dayText =
				overdueDays == 1 ? "1 day" : overdueDays + " days";
		JLabel messageLabel = new JLabel(
				"<html><b>《" + escapeHtml(title)
						+ "》</b> is overdue by " + dayText + ".</html>");
		JLabel studentLabel = new JLabel(
				"Student: " + studentName + " - " + studentNumber);
		JLabel borrowDateLabel =
				new JLabel("Borrow date: " + borrowDate);
		JLabel dueDateLabel = new JLabel("Due date: " + dueDate);

		messageLabel.setFont(
				messageLabel.getFont().deriveFont(Font.PLAIN, 15.0f));
		for (JLabel label : new JLabel[] {
				studentLabel, borrowDateLabel, dueDateLabel
		}) {
			label.setFont(label.getFont().deriveFont(Font.PLAIN, 14.0f));
		}

		box.add(messageLabel);
		box.add(studentLabel);
		box.add(borrowDateLabel);
		box.add(dueDateLabel);
		return box;
	}

	private String escapeHtml(String value) {
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}

	private static class SlideInPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private static final int EXTRA_OFFSET = 40;

		private double progress = 1.0;
		private Timer animationTimer;

		SlideInPanel(Component content) {
			super(new BorderLayout());
			super.setOpaque(false);
			super.add(content, BorderLayout.CENTER);
			super.setAlignmentX(content.getAlignmentX());

			Dimension preferredSize = content.getPreferredSize();
			if (preferredSize != null) {
				super.setPreferredSize(preferredSize);
			}

			Dimension minimumSize = content.getMinimumSize();
			if (minimumSize != null) {
				super.setMinimumSize(minimumSize);
			}

			Dimension maximumSize = content.getMaximumSize();
			if (maximumSize != null) {
				super.setMaximumSize(maximumSize);
			}
		}

		void play(int delay) {
			if (animationTimer != null && animationTimer.isRunning()) {
				animationTimer.stop();
			}
			progress = 0.0;
			repaint();

			Timer starter = new Timer(delay, _ -> startAnimation());
			starter.setRepeats(false);
			starter.start();
		}

		private void startAnimation() {
			animationTimer = new Timer(16, _ -> {
				progress = Math.min(1.0, progress + 0.06);
				repaint();
				if (progress >= 1.0) {
					animationTimer.stop();
				}
			});
			animationTimer.start();
		}

		@Override
		protected void paintChildren(Graphics graphics) {
			Graphics2D graphics2D = (Graphics2D) graphics.create();
			double eased = 1 - Math.pow(1 - progress, 3);
			int offset = (int) Math.round(
					(getWidth() + EXTRA_OFFSET) * (1 - eased));
			graphics2D.translate(offset, 0);
			super.paintChildren(graphics2D);
			graphics2D.dispose();
		}
	}
}
