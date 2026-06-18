package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.function.BiConsumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import system.StartSystem;
import system.AppSettings;
import system.AppSettings.Settings;
import system.SuspensionManager;
import system.SuspensionManager.SuspensionState;
import system.User;
import system.User.Role_level;
import system.User.Status;

public class Profile extends JPanel {
	private static final long serialVersionUID = 1L;

	private String name;
	private String student_no;
	private Role_level role_level;
	private Status status;
	private String suspended_until;
	private final int userId;
	private final BiConsumer<Integer, String> borrowReservedBook;
	private final BiConsumer<Integer, String> returnBorrowedBook;
	private final Runnable logoutAction;

	private JLabel titleLabel;
	private JLabel studentNoLabel;
	private JLabel levelLabel;
	private JLabel statusLabel;
	private JLabel temporarySuspensionLabel;
	private JLabel studentNoinfo;
	private JLabel levelinfo;
	private JLabel statusinfo;
	private JLabel temporarySuspensionInfo;

	private JPanel notificationListPanel;
	private final List<SlideInPanel> profileAnimationItems =
			new ArrayList<>();
	private final List<SlideInPanel> notificationAnimationItems =
			new ArrayList<>();

	public Profile(
			User user,
			int userId,
			BiConsumer<Integer, String> borrowReservedBook,
			BiConsumer<Integer, String> returnBorrowedBook,
			Runnable logoutAction) {
		name = user.getName();
		student_no = user.getStudent_no();
		role_level = user.getRole_level();
		status = user.getStatus();
		suspended_until = user.getSuspended_until();
		this.userId = userId;
		this.borrowReservedBook = borrowReservedBook;
		this.returnBorrowedBook = returnBorrowedBook;
		this.logoutAction = logoutAction;

		// Two columns: profile information and notifications.
		super.setLayout(new GridLayout(1, 2, 15, 0));
		super.setBorder(new EmptyBorder(10, 10, 10, 10));
		super.setBackground(Color.WHITE);

		super.add(createProfilePanel());
		super.add(createNotificationPanel());

		refreshNotifications();
	}

	/**
	 * Create the profile information shown on the left.
	 */
	private JPanel createProfilePanel() {
		JPanel profilePanel = new JPanel(new GridBagLayout());
		profilePanel.setBackground(Color.WHITE);
		profilePanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY),
				"Student Profile",
				TitledBorder.LEFT,
				TitledBorder.TOP,
				new Font("SansSerif", Font.BOLD, 16)
		));

		titleLabel = new JLabel("Welcome back,");
		JLabel nameLabel = new JLabel(name);
		studentNoLabel = new JLabel("Student No.");
		levelLabel = new JLabel("Role Level");
		statusLabel = new JLabel("Status");
		temporarySuspensionLabel = new JLabel("Suspension Ends");

		studentNoinfo = new JLabel(student_no);
		levelinfo = createBadge(
				role_level.toString(),
				role_level == Role_level.VIP
						? new Color(255, 193, 7)
						: new Color(66, 133, 244),
				role_level == Role_level.VIP ? Color.BLACK : Color.WHITE);
		statusinfo = createBadge(
				status.toString(),
				status == Status.ACTIVE
						? new Color(46, 125, 50)
						: new Color(198, 40, 40),
				Color.WHITE);
		temporarySuspensionInfo = new JLabel(
				status == Status.SUSPENDED
						? suspended_until == null
								? "Return overdue books first"
								: suspended_until
						: "None");

		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 18.0f));
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 28.0f));

		for (JLabel label : new JLabel[] {
				studentNoLabel,
				levelLabel,
				statusLabel,
				temporarySuspensionLabel
		}) {
			label.setFont(label.getFont().deriveFont(Font.BOLD, 15.0f));
		}
		studentNoinfo.setFont(studentNoinfo.getFont().deriveFont(16.0f));
		temporarySuspensionInfo.setFont(
				temporarySuspensionInfo.getFont().deriveFont(15.0f));

		JPanel content = new JPanel(new GridBagLayout());
		content.setBackground(Color.WHITE);
		content.setBorder(new EmptyBorder(20, 25, 20, 25));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		content.add(titleLabel, gbc);

		gbc.gridy = 1;
		gbc.insets = new Insets(0, 5, 25, 5);
		content.add(nameLabel, gbc);

		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.insets = new Insets(8, 5, 8, 20);
		content.add(studentNoLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(8, 5, 8, 5);
		content.add(studentNoinfo, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.insets = new Insets(8, 5, 8, 20);
		content.add(levelLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(8, 5, 8, 5);
		content.add(levelinfo, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 5, 8, 20);
		content.add(statusLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(8, 5, 8, 5);
		content.add(statusinfo, gbc);

		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 5, 8, 20);
		content.add(temporarySuspensionLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(8, 5, 8, 5);
		content.add(temporarySuspensionInfo, gbc);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(15, 15, 15, 15);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		SlideInPanel contentSlide = new SlideInPanel(content);
		profileAnimationItems.add(contentSlide);
		profilePanel.add(contentSlide, gbc);

		JButton logoutButton = new JButton("Logout");
		logoutButton.setFont(
				logoutButton.getFont().deriveFont(Font.BOLD, 14.0f));
		logoutButton.setCursor(
				Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		logoutButton.addActionListener(_ -> logoutAction.run());

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 20, 20, 20);
		gbc.weightx = 1.0;
		gbc.weighty = 0;
		SlideInPanel logoutSlide = new SlideInPanel(logoutButton);
		profileAnimationItems.add(logoutSlide);
		profilePanel.add(logoutSlide, gbc);

		return profilePanel;
	}

	private JLabel createBadge(String text, Color background, Color foreground) {
		JLabel badge = new JLabel("  " + text + "  ");
		badge.setOpaque(true);
		badge.setBackground(background);
		badge.setForeground(foreground);
		badge.setFont(badge.getFont().deriveFont(Font.BOLD, 14.0f));
		badge.setBorder(new EmptyBorder(5, 8, 5, 8));
		return badge;
	}

	/**
	 * Create the notification area shown on the right.
	 */
	private JPanel createNotificationPanel() {
		JPanel notificationPanel = new JPanel(new BorderLayout());
		notificationPanel.setBackground(Color.WHITE);
		notificationPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY),
				"Notification",
				TitledBorder.LEFT,
				TitledBorder.TOP,
				new Font("SansSerif", Font.BOLD, 16)
		));

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

	/**
	 * Load active loans that are overdue or due in fewer than three days.
	 */
	public void refreshNotifications() {
		refreshSuspensionStatus();
		notificationListPanel.removeAll();
		notificationAnimationItems.clear();
		Settings settings = AppSettings.get();
		boolean hasNotification =
				settings.reservationNotificationEnabled()
						&& loadReadyReservations();

		String notificationCondition = null;
		if (settings.dueNotificationEnabled()
				&& settings.overdueNotificationEnabled()) {
			notificationCondition =
					"DATEDIFF(r.due_date, NOW()) <= ?";
		} else if (settings.dueNotificationEnabled()) {
			notificationCondition =
					"DATEDIFF(r.due_date, NOW()) BETWEEN 0 AND ?";
		} else if (settings.overdueNotificationEnabled()) {
			notificationCondition =
					"DATEDIFF(r.due_date, NOW()) < 0";
		}

		if (notificationCondition != null) {
			String sql =
					"SELECT r.book_id, b.title, r.borrow_date, r.due_date, " +
					"DATEDIFF(r.due_date, NOW()) AS days_left " +
					"FROM borrow_records r " +
					"JOIN books b ON r.book_id = b.book_id " +
					"JOIN users u ON r.user_id = u.user_id " +
					"WHERE u.student_no = ? " +
					"AND r.return_date IS NULL AND " +
					notificationCondition + " ORDER BY r.due_date ASC";

			try (PreparedStatement statement =
					StartSystem.db.prepareStatement(sql)) {

				statement.setString(1, student_no);
				if (notificationCondition.contains("?")) {
					statement.setInt(2, settings.dueNotificationDays());
				}

				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						hasNotification = true;

						addNotificationItem(createNotificationBox(
								resultSet.getInt("book_id"),
								resultSet.getString("title"),
								resultSet.getString("borrow_date"),
								resultSet.getString("due_date"),
								resultSet.getInt("days_left")));
						notificationListPanel.add(Box.createRigidArea(
								new Dimension(0, 10)));
					}
				}
			} catch (SQLException exception) {
				exception.printStackTrace();
				JLabel errorLabel =
						new JLabel("Failed to load notifications.");
				errorLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
				addNotificationItem(errorLabel);
				hasNotification = true;
			}
		}

		if (!hasNotification) {
			JLabel emptyLabel = new JLabel("No notifications.");
			emptyLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
			addNotificationItem(emptyLabel);
		}

		notificationListPanel.revalidate();
		notificationListPanel.repaint();
		playEntranceAnimations();
	}

	private boolean loadReadyReservations() {
		String sql =
				"SELECT r.reservation_id, r.book_id, b.title, r.reserved_at " +
				"FROM reservations r " +
				"JOIN books b ON r.book_id = b.book_id " +
				"WHERE r.user_id = ? AND r.status = 'READY' " +
				"ORDER BY r.reserved_at";
		boolean hasReservation = false;

		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					hasReservation = true;
					addNotificationItem(createReservationBox(
							resultSet.getInt("reservation_id"),
							resultSet.getInt("book_id"),
							resultSet.getString("title"),
							resultSet.getString("reserved_at")));
					notificationListPanel.add(Box.createRigidArea(
							new Dimension(0, 10)));
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return hasReservation;
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

	private JPanel createReservationBox(
			int reservationId,
			int bookId,
			String title,
			String reservedAt) {
		JPanel box = new JPanel(new BorderLayout(0, 8));
		Dimension boxSize = new Dimension(520, 125);
		box.setPreferredSize(boxSize);
		box.setMinimumSize(boxSize);
		box.setMaximumSize(boxSize);
		box.setAlignmentX(CENTER_ALIGNMENT);
		box.setBackground(new Color(232, 245, 233));
		box.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(46, 125, 50), 2),
				new EmptyBorder(10, 10, 10, 10)));

		JPanel messagePanel = new JPanel(new GridLayout(2, 1, 0, 5));
		messagePanel.setOpaque(false);
		JLabel messageLabel = new JLabel(
				"<html><b>《" + escapeHtml(title)
						+ "》</b> is ready to borrow.</html>");
		messageLabel.setFont(
				messageLabel.getFont().deriveFont(Font.PLAIN, 15.0f));
		JLabel reservedAtLabel =
				new JLabel("Reserved at: " + reservedAt);
		reservedAtLabel.setFont(
				reservedAtLabel.getFont().deriveFont(14.0f));
		messagePanel.add(messageLabel);
		messagePanel.add(reservedAtLabel);

		JPanel buttonPanel = new JPanel(
				new FlowLayout(FlowLayout.RIGHT, 5, 0));
		buttonPanel.setOpaque(false);
		JButton cancelButton = new JButton("Cancel");
		JButton borrowButton = new JButton("Borrow");
		styleLinkButton(cancelButton);
		styleLinkButton(borrowButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(borrowButton);

		cancelButton.addActionListener(_ -> {
			if (Reservation.cancelReservation(reservationId, bookId)) {
				refreshNotifications();
				JOptionPane.showMessageDialog(
						this,
						"Reservation cancelled.",
						"Cancel Succeed",
						JOptionPane.PLAIN_MESSAGE);
			}
		});
		borrowButton.addActionListener(_ ->
				borrowReservedBook.accept(bookId, title));

		box.add(messagePanel, BorderLayout.CENTER);
		box.add(buttonPanel, BorderLayout.SOUTH);
		return box;
	}

	private void styleLinkButton(JButton button) {
		button.setText("<html><u>" + button.getText() + "</u></html>");
		button.setFont(button.getFont().deriveFont(Font.BOLD, 15.0f));
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setOpaque(false);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	private void refreshSuspensionStatus() {
		String sql = "SELECT user_id FROM users WHERE student_no = ?";

		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql)) {
			statement.setString(1, student_no);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					SuspensionState suspension =
							SuspensionManager.refreshUserStatus(
									resultSet.getInt("user_id"));
					status = suspension.suspended()
							? Status.SUSPENDED
							: Status.ACTIVE;
					suspended_until = suspension.suspendedUntil() == null
							? null
							: suspension.suspendedUntil().toString();

					statusinfo.setText("  " + status + "  ");
					statusinfo.setBackground(
							status == Status.ACTIVE
									? new Color(46, 125, 50)
									: new Color(198, 40, 40));

					if (!suspension.suspended()) {
						temporarySuspensionInfo.setText("None");
					} else if (suspension.hasOverdueBooks()) {
						temporarySuspensionInfo.setText(
								"Return overdue books first");
					} else if (suspension.suspendedUntil() != null) {
						temporarySuspensionInfo.setText(
								suspension.suspendedUntil().toString());
					} else {
						temporarySuspensionInfo.setText(
								"Contact administrator");
					}
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Create one bordered notification box.
	 */
	private JPanel createNotificationBox(
			int bookId,
			String title,
			String borrowDate,
			String dueDate,
			int daysLeft) {

		JPanel box = new JPanel(new BorderLayout(0, 5));
		Dimension boxSize = new Dimension(520, 130);
		box.setPreferredSize(boxSize);
		box.setMinimumSize(boxSize);
		box.setMaximumSize(boxSize);
		box.setAlignmentX(CENTER_ALIGNMENT);

		boolean overdue = daysLeft < 0;
		Color borderColor = overdue
				? Color.RED
				: new Color(245, 183, 0);
		Color backgroundColor = overdue
				? new Color(255, 230, 230)
				: new Color(255, 248, 204);
		box.setBackground(backgroundColor);
		box.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(borderColor, 2),
				new EmptyBorder(10, 10, 10, 10)
		));

		String message;
		if (overdue) {
			message = "You need to return <b>《" + escapeHtml(title)
					+ "》</b> as soon as possible.";
		} else {
			String dayText = daysLeft == 1 ? "1 day" : daysLeft + " days";
			message = "You need to return <b>《" + escapeHtml(title)
					+ "》</b> in " + dayText + ".";
		}

		JLabel messageLabel = new JLabel("<html>" + message + "</html>");
		JLabel borrowDateLabel =
				new JLabel("Borrow date: " + borrowDate);
		JLabel dueDateLabel =
				new JLabel("Due date: " + dueDate);

		messageLabel.setFont(
				messageLabel.getFont().deriveFont(Font.PLAIN, 15.0f));
		borrowDateLabel.setFont(
				borrowDateLabel.getFont().deriveFont(Font.PLAIN, 14.0f));
		dueDateLabel.setFont(
				dueDateLabel.getFont().deriveFont(Font.PLAIN, 14.0f));

		JPanel messagePanel = new JPanel(new GridLayout(2, 1, 0, 5));
		messagePanel.setOpaque(false);
		messagePanel.add(messageLabel);
		messagePanel.add(borrowDateLabel);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setOpaque(false);
		bottomPanel.add(dueDateLabel, BorderLayout.WEST);

		JButton returnButton = new JButton("Return");
		styleLinkButton(returnButton);
		bottomPanel.add(returnButton, BorderLayout.EAST);
		returnButton.addActionListener(_ ->
				returnBorrowedBook.accept(bookId, title));

		box.add(messagePanel, BorderLayout.CENTER);
		box.add(bottomPanel, BorderLayout.SOUTH);

		return box;
	}

	private String escapeHtml(String value) {
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}

	public String getName() {
		return name;
	}

	public String getStudent_no() {
		return student_no;
	}

	public Role_level getRole_level() {
		return role_level;
	}

	public Status getStatus() {
		return status;
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
