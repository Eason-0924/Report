package system;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class SuspensionManager {
	private SuspensionManager() {}

	public static SuspensionState refreshUserStatus(int userId)
			throws SQLException {
		boolean hasOverdueBooks = hasOverdueBooks(userId);

		String selectSql =
				"SELECT status, suspended_until FROM users WHERE user_id = ?";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(selectSql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return new SuspensionState(false, null, false);
				}

				String status = resultSet.getString("status");
				Date storedDate = resultSet.getDate("suspended_until");
				LocalDate suspendedUntil =
						storedDate == null ? null : storedDate.toLocalDate();

				if (hasOverdueBooks) {
					updateStatus(userId, "SUSPENDED", null);
					return new SuspensionState(true, null, true);
				}

				if ("ACTIVE".equals(status)) {
					if (suspendedUntil != null) {
						updateStatus(userId, "ACTIVE", null);
					}
					return new SuspensionState(false, null, false);
				}

				if (suspendedUntil != null
						&& !LocalDate.now().isBefore(suspendedUntil)) {
					updateStatus(userId, "ACTIVE", null);
					return new SuspensionState(false, null, false);
				}

				return new SuspensionState(
						true, suspendedUntil, false);
			}
		}
	}

	public static SuspensionState updateAfterReturn(
			int userId, boolean returnedBookWasOverdue) throws SQLException {
		if (hasOverdueBooks(userId)) {
			updateStatus(userId, "SUSPENDED", null);
			return new SuspensionState(true, null, true);
		}

		if (returnedBookWasOverdue) {
			if ((isVip(userId)
					&& AppSettings.get().vipImmediateCancellation())
					|| AppSettings.get().suspensionDays() <= 0) {
				updateStatus(userId, "ACTIVE", null);
				return new SuspensionState(false, null, false);
			}

			LocalDate suspendedUntil =
					LocalDate.now().plusDays(
							AppSettings.get().suspensionDays());
			updateStatus(userId, "SUSPENDED", suspendedUntil);
			return new SuspensionState(true, suspendedUntil, false);
		}

		return refreshUserStatus(userId);
	}

	public static void refreshAllUsers() throws SQLException {
		List<Integer> userIds = new ArrayList<>();
		String sql = "SELECT user_id FROM users";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				userIds.add(resultSet.getInt("user_id"));
			}
		}

		for (int userId : userIds) {
			refreshUserStatus(userId);
		}
	}

	private static boolean isVip(int userId) throws SQLException {
		String sql = "SELECT role_level FROM users WHERE user_id = ?";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next()
						&& "VIP".equals(resultSet.getString("role_level"));
			}
		}
	}

	public static boolean hasOverdueBooks(int userId) throws SQLException {
		String sql =
				"SELECT 1 FROM borrow_records " +
				"WHERE user_id = ? AND return_date IS NULL " +
				"AND due_date < NOW() LIMIT 1";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	public static void setAdminStatus(int userId, String status)
			throws SQLException {
		if ("ACTIVE".equals(status) && hasOverdueBooks(userId)) {
			throw new IllegalStateException(
					"The user still has overdue books.");
		}

		updateStatus(userId, status, null);
	}

	private static void updateStatus(
			int userId, String status, LocalDate suspendedUntil)
			throws SQLException {
		String sql =
				"UPDATE users SET status = ?, suspended_until = ? " +
				"WHERE user_id = ?";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql)) {
			statement.setString(1, status);
			if (suspendedUntil == null) {
				statement.setNull(2, java.sql.Types.DATE);
			} else {
				statement.setDate(2, Date.valueOf(suspendedUntil));
			}
			statement.setInt(3, userId);
			statement.executeUpdate();
		}
	}

	public record SuspensionState(
			boolean suspended,
			LocalDate suspendedUntil,
			boolean hasOverdueBooks) {
	}
}
