package user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import system.AppSettings;
import system.StartSystem;

public final class Reservation {
	private Reservation() {
	}

	public static boolean cancelReservation(int reservationId, int bookId) {
		String sql =
				"UPDATE reservations SET status = 'CANCEL', notified = FALSE " +
				"WHERE reservation_id = ? AND status IN ('WAITING', 'READY')";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, reservationId);
			if (statement.executeUpdate() > 0) {
				if (!isBookBorrowed(bookId)) {
					markNextAvailable(bookId);
				}
				return true;
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return false;
	}

	public static void reserveBook(
			int userId, int bookId, String bookTitle, java.awt.Component parent) {
		if (!isBookBorrowed(bookId)) {
			JOptionPane.showMessageDialog(
					parent,
					"This book is currently available. You can borrow it directly.",
					"Reservation Not Needed",
					JOptionPane.PLAIN_MESSAGE);
			return;
		}

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
							"You are already borrowing this book.",
							"Reserve Failed",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			return;
		}

		String roleLevel = getRoleLevel(userId);
		int maximum = AppSettings.get().maxReserveBooks(roleLevel);
		String countSql =
				"SELECT COUNT(*) FROM reservations " +
				"WHERE user_id = ? AND status IN ('WAITING', 'READY')";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(countSql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next() && resultSet.getInt(1) >= maximum) {
					JOptionPane.showMessageDialog(
							parent,
							"You have reached the maximum of "
									+ maximum + " active reservations.",
							"Reservation Limit Reached",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			return;
		}

		String duplicateSql =
				"SELECT COUNT(*) FROM reservations " +
				"WHERE user_id = ? AND book_id = ? " +
				"AND status IN ('WAITING', 'READY')";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(duplicateSql)) {
			statement.setInt(1, userId);
			statement.setInt(2, bookId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next() && resultSet.getInt(1) > 0) {
					JOptionPane.showMessageDialog(
							parent,
							"You already reserved this book.",
							"Reserve Failed",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			return;
		}

		String sql =
				"INSERT INTO reservations(user_id, book_id, status, notified) " +
				"VALUES (?, ?, 'WAITING', FALSE)";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			statement.setInt(2, bookId);
			statement.executeUpdate();
			JOptionPane.showMessageDialog(
					parent,
					"Reserved 《" + bookTitle + "》 successfully.",
					"Reservation Succeed",
					JOptionPane.PLAIN_MESSAGE);
		} catch (SQLException exception) {
			if ("23000".equals(exception.getSQLState())) {
				JOptionPane.showMessageDialog(
						parent,
						"You already reserved this book.",
						"Reserve Failed",
						JOptionPane.ERROR_MESSAGE);
			} else {
				exception.printStackTrace();
			}
		}
	}

	public static String getBorrowRestriction(int userId, int bookId) {
		if (isBookBorrowed(bookId)) {
			return "This book is currently borrowed. You can reserve it instead.";
		}

		String sql =
				"SELECT user_id FROM reservations WHERE book_id = ? " +
				"AND status IN ('WAITING', 'READY') " +
				"ORDER BY reserved_at, reservation_id LIMIT 1";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, bookId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next() && resultSet.getInt("user_id") != userId) {
					return "This book is reserved for another user.";
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			return "Unable to verify the reservation queue.";
		}
		return null;
	}

	public static void completeReservation(int userId, int bookId) {
		String sql =
				"UPDATE reservations SET status = 'BORROWED', notified = FALSE " +
				"WHERE user_id = ? AND book_id = ? " +
				"AND status IN ('WAITING', 'READY')";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			statement.setInt(2, bookId);
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}

	public static void markNextAvailable(int bookId) {
		String resetSql =
				"UPDATE reservations SET status = 'WAITING', notified = FALSE " +
				"WHERE book_id = ? AND status IN ('WAITING', 'READY')";
		String updateSql =
				"UPDATE reservations SET status = 'READY', notified = TRUE " +
				"WHERE reservation_id = (" +
				"SELECT reservation_id FROM (" +
				"SELECT reservation_id FROM reservations WHERE book_id = ? " +
				"AND status = 'WAITING' " +
				"ORDER BY reserved_at, reservation_id LIMIT 1" +
				") next_reservation)";

		try (PreparedStatement reset = StartSystem.db.prepareStatement(resetSql);
				PreparedStatement update = StartSystem.db.prepareStatement(updateSql)) {
			reset.setInt(1, bookId);
			reset.executeUpdate();
			update.setInt(1, bookId);
			update.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}

	public static void showReadyNotifications(
			int userId, java.awt.Component parent) {
		if (!AppSettings.get().reservationNotificationEnabled()) {
			return;
		}

		String sql =
				"SELECT b.title FROM reservations r " +
				"JOIN books b ON r.book_id = b.book_id " +
				"WHERE r.user_id = ? AND r.status = 'READY' " +
				"ORDER BY r.reserved_at";
		StringBuilder message = new StringBuilder();

		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					message.append("- 《")
							.append(resultSet.getString("title"))
							.append("》\n");
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}

		if (message.length() > 0) {
			JOptionPane.showMessageDialog(
					parent,
					"Your reserved book is ready to borrow:\n" + message,
					"Reservation Available",
					JOptionPane.PLAIN_MESSAGE);
		}
	}

	private static boolean isBookBorrowed(int bookId) {
		String sql =
				"SELECT COUNT(*) FROM borrow_records " +
				"WHERE book_id = ? AND return_date IS NULL";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, bookId);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next() && resultSet.getInt(1) > 0;
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			return true;
		}
	}

	private static String getRoleLevel(int userId) {
		String sql = "SELECT role_level FROM users WHERE user_id = ?";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getString("role_level");
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return "NORMAL";
	}
}
