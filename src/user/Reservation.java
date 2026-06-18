package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import system.StartSystem;
import system.TableStyle;
import system.AppSettings;

public class Reservation extends JPanel {
	private static final long serialVersionUID = 1L;

	private final int userId;
	private final DefaultTableModel tableModel;
	private final JTable reservationTable;

	public Reservation(int userId) {
		this.userId = userId;

		super.setLayout(new BorderLayout(0, 5));
		super.setBorder(new EmptyBorder(0, 0, 0, 0));
		super.setBackground(Color.WHITE);

		String[] columns = {
				"Reservation ID", "Book ID", "Book Title", "Reserved At", "Status"
		};
		tableModel = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		reservationTable = new JTable(tableModel);
		reservationTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
		reservationTable.getTableHeader().setFont(
				new Font("SansSerif", Font.BOLD, 15));
		reservationTable.setRowHeight(28);
		reservationTable.setAutoCreateRowSorter(true);
		TableStyle.applyUserStyle(reservationTable);

		hideColumn(0);
		hideColumn(1);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBackground(Color.WHITE);
		JButton cancelButton = new JButton("Cancel Selected Reservation");
		cancelButton.addActionListener(_ -> cancelSelectedReservation());
		buttonPanel.add(cancelButton);

		super.add(new JScrollPane(reservationTable), BorderLayout.CENTER);
		super.add(buttonPanel, BorderLayout.SOUTH);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent event) {
				loadReservations();
			}
		});

		loadReservations();
	}

	private void hideColumn(int index) {
		TableColumn column = reservationTable.getColumnModel().getColumn(index);
		column.setMinWidth(0);
		column.setMaxWidth(0);
		column.setPreferredWidth(0);
	}

	public void loadReservations() {
		String sql =
				"SELECT r.reservation_id, r.book_id, b.title, r.reserved_at, " +
				"CASE WHEN r.status = 'READY' THEN 'READY TO BORROW' " +
				"ELSE r.status END AS reservation_status " +
				"FROM reservations r " +
				"JOIN books b ON r.book_id = b.book_id " +
				"WHERE r.user_id = ? ORDER BY r.reserved_at";

		tableModel.setRowCount(0);
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					tableModel.addRow(new Object[] {
							resultSet.getInt("reservation_id"),
							resultSet.getInt("book_id"),
							resultSet.getString("title"),
							resultSet.getString("reserved_at"),
							resultSet.getString("reservation_status")
					});
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}

	private void cancelSelectedReservation() {
		int selectedRow = reservationTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(
					this,
					"Please select a reservation to cancel.",
					"Cancel Failed",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		int modelRow = reservationTable.convertRowIndexToModel(selectedRow);
		int reservationId = Integer.parseInt(
				tableModel.getValueAt(modelRow, 0).toString());
		int bookId = Integer.parseInt(
				tableModel.getValueAt(modelRow, 1).toString());
		String status = tableModel.getValueAt(modelRow, 4).toString();
		if (!"WAITING".equals(status)
				&& !"READY TO BORROW".equals(status)) {
			JOptionPane.showMessageDialog(
					this,
					"This reservation is no longer active.",
					"Cancel Failed",
					JOptionPane.PLAIN_MESSAGE);
			return;
		}

		if (cancelReservation(reservationId, bookId)) {
				loadReservations();
				JOptionPane.showMessageDialog(
						this,
						"Reservation cancelled.",
						"Cancel Succeed",
						JOptionPane.PLAIN_MESSAGE);
		}
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
