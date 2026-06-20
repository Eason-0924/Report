package user;

import java.awt.Component;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import system.AppSettings;
import system.StartSystem;
import system.SuspensionManager;
import system.SuspensionManager.SuspensionState;

public final class BorrowBook {
	private BorrowBook() {
	}

	public static void showBorrowDialog(
			Component parent, int userId, int bookId, String bookTitle) {
		SuspensionState suspension = getSuspensionState(parent, userId);
		if (suspension != null && suspension.suspended()) {
			String message;
			if (suspension.hasOverdueBooks()) {
				message = "Your account is suspended because you "
						+ "have overdue books.\nPlease return all "
						+ "overdue books first.";
			} else if (suspension.suspendedUntil() != null) {
				message = "Your account is suspended until "
						+ suspension.suspendedUntil()
						+ " after returning all overdue books.\n"
						+ "If you want to end the suspension early, "
						+ "please contact the administrator.";
			} else {
				message = "Your account is suspended. "
						+ "Please contact the administrator.";
			}
			JOptionPane.showMessageDialog(
					parent, message, "Borrow Failed",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (!canStartBorrow(parent, userId, bookId, bookTitle)) {
			return;
		}

		int acceptableBorrowDays = AppSettings.get().maxBorrowDays(
				getLevel(userId));
		String input = JOptionPane.showInputDialog(
				parent,
				"You are borrowing: 《" + bookTitle
						+ "》\nEnter borrow days (1-"
						+ acceptableBorrowDays + "):",
				"Confirm Borrow",
				JOptionPane.PLAIN_MESSAGE);

		if (input == null || input.trim().isEmpty()) {
			return;
		}

		try {
			int days = Integer.parseInt(input.trim());
			if (days < 1 || days > acceptableBorrowDays) {
				JOptionPane.showMessageDialog(
						parent,
						"Days must be between 1 and "
								+ acceptableBorrowDays + ".",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			executeBorrowAction(parent, userId, bookId, bookTitle, days);
		} catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(
					parent,
					"Please enter a valid integer number.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static boolean canStartBorrow(
			Component parent, int userId, int bookId, String bookTitle) {
		String ownLoanSql =
				"SELECT COUNT(*) FROM borrow_records " +
				"WHERE user_id = ? AND book_id = ? AND return_date IS NULL";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(ownLoanSql)) {
			statement.setInt(1, userId);
			statement.setInt(2, bookId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next() && resultSet.getInt(1) > 0) {
					JOptionPane.showMessageDialog(
							parent,
							"You already borrowed " + bookTitle,
							"Borrow Failed",
							JOptionPane.WARNING_MESSAGE);
					return false;
				}
			}

			if (hasReachedBorrowLimit(parent, userId, bookTitle)) {
				return false;
			}

			String reservationRestriction =
					Reservation.getBorrowRestriction(userId, bookId);
			if (reservationRestriction != null) {
				showBorrowRestriction(
						parent, userId, bookId, bookTitle, reservationRestriction);
				return false;
			}
			return true;
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					parent,
					"Failed to check whether the book can be borrowed.",
					"Database Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private static boolean hasReachedBorrowLimit(
			Component parent, int userId, String bookTitle)
			throws SQLException {
		String roleLevel = getLevel(userId);
		int maximum = AppSettings.get().maxBorrowBooks(roleLevel);
		String sql =
				"SELECT COUNT(*) FROM borrow_records " +
				"WHERE user_id = ? AND return_date IS NULL";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next() && resultSet.getInt(1) >= maximum) {
					JOptionPane.showMessageDialog(
							parent,
							"You have reached the maximum of "
									+ maximum + " borrowed books.\n"
									+ "Return a book before borrowing 《"
									+ bookTitle + "》.",
							"Borrow Limit Reached",
							JOptionPane.WARNING_MESSAGE);
					return true;
				}
			}
		}
		return false;
	}

	private static void showBorrowRestriction(
			Component parent,
			int userId,
			int bookId,
			String bookTitle,
			String restriction) {
		if (restriction.contains("currently borrowed")) {
			Object[] options = {"Reserve", "Cancel"};
			int choice = JOptionPane.showOptionDialog(
					parent,
					"This book is currently borrowed.\n"
							+ "Would you like to reserve 《"
							+ bookTitle + "》?",
					"Book Is Borrowed",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					options,
					options[0]);
			if (choice == 0) {
				Reservation.reserveBook(userId, bookId, bookTitle, parent);
			}
			return;
		}

		JOptionPane.showMessageDialog(
				parent,
				restriction,
				"Borrow Failed",
				JOptionPane.PLAIN_MESSAGE);
	}

	private static void executeBorrowAction(
			Component parent,
			int userId,
			int bookId,
			String bookTitle,
			int borrowDays) {
		try {
			if (hasReachedBorrowLimit(parent, userId, bookTitle)) {
				return;
			}
			String reservationRestriction =
					Reservation.getBorrowRestriction(userId, bookId);
			if (reservationRestriction != null) {
				showBorrowRestriction(
						parent, userId, bookId, bookTitle, reservationRestriction);
				return;
			}

			String checkSql =
					"SELECT COUNT(*) FROM borrow_records " +
					"WHERE user_id = ? AND book_id = ? AND return_date IS NULL";
			try (PreparedStatement checkStatement =
					StartSystem.db.prepareStatement(checkSql)) {
				checkStatement.setInt(1, userId);
				checkStatement.setInt(2, bookId);
				try (ResultSet resultSet = checkStatement.executeQuery()) {
					if (resultSet.next() && resultSet.getInt(1) > 0) {
						JOptionPane.showMessageDialog(
								parent,
								"You already borrowed " + bookTitle,
								"Borrow Failed",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
			}

			system.BorrowRecord newRecord =
					new system.BorrowRecord(userId, bookId, borrowDays);
			newRecord.insert();
			Reservation.completeReservation(userId, bookId);
			JOptionPane.showMessageDialog(
					parent,
					"<html>Success! You borrowed: <b>《"
							+ escapeHtml(bookTitle)
							+ "》</b><br>Please return before "
							+ newRecord.getDue_date() + ".</html>",
					"Borrow Succeed",
					JOptionPane.PLAIN_MESSAGE);
		} catch (Exception exception) {
			JOptionPane.showMessageDialog(
					parent,
					"Database error: " + exception.getMessage(),
					"Failed During Creating Borrow Record",
					JOptionPane.ERROR_MESSAGE);
			exception.printStackTrace();
		}
	}

	private static String getLevel(int userId) {
		String sql = "SELECT role_level FROM users WHERE user_id = ?";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getString(1);
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return "NORMAL";
	}

	private static SuspensionState getSuspensionState(
			Component parent, int userId) {
		try {
			return SuspensionManager.refreshUserStatus(userId);
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					parent,
					"Failed to check your account status.",
					"Database Error",
					JOptionPane.ERROR_MESSAGE);
			return new SuspensionState(true, null, false);
		}
	}

	private static String escapeHtml(String value) {
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}
}
