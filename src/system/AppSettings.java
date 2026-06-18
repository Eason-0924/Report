package system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class AppSettings {
	public static final Settings DEFAULTS = new Settings(
			7, 14,
			3, 5,
			3, 5,
			true, 3,
			true, true,
			7, true,
			"ESOE2026");

	private static Settings current = DEFAULTS;

	private AppSettings() {}

	public static synchronized void initialize() throws SQLException {
		String createSql =
				"CREATE TABLE IF NOT EXISTS app_settings (" +
				"settings_id INT PRIMARY KEY, " +
				"normal_max_borrow_days INT NOT NULL, " +
				"vip_max_borrow_days INT NOT NULL, " +
				"normal_max_borrow_books INT NOT NULL, " +
				"vip_max_borrow_books INT NOT NULL, " +
				"normal_max_reserve_books INT NOT NULL, " +
				"vip_max_reserve_books INT NOT NULL, " +
				"due_notification_enabled BOOLEAN NOT NULL, " +
				"due_notification_days INT NOT NULL, " +
				"reservation_notification_enabled BOOLEAN NOT NULL, " +
				"overdue_notification_enabled BOOLEAN NOT NULL, " +
				"suspension_days INT NOT NULL, " +
				"vip_immediate_cancellation BOOLEAN NOT NULL, " +
				"security_key VARCHAR(255) NOT NULL)";
		try (Statement statement = StartSystem.db.createStatement()) {
			statement.executeUpdate(createSql);
		}

		String insertSql =
				"INSERT IGNORE INTO app_settings VALUES " +
				"(1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(insertSql)) {
			setValues(statement, DEFAULTS);
			statement.executeUpdate();
		}
		reload();
	}

	public static synchronized Settings get() {
		return current;
	}

	public static synchronized void reload() throws SQLException {
		String sql = "SELECT * FROM app_settings WHERE settings_id = 1";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			if (resultSet.next()) {
				current = new Settings(
						resultSet.getInt("normal_max_borrow_days"),
						resultSet.getInt("vip_max_borrow_days"),
						resultSet.getInt("normal_max_borrow_books"),
						resultSet.getInt("vip_max_borrow_books"),
						resultSet.getInt("normal_max_reserve_books"),
						resultSet.getInt("vip_max_reserve_books"),
						resultSet.getBoolean("due_notification_enabled"),
						resultSet.getInt("due_notification_days"),
						resultSet.getBoolean(
								"reservation_notification_enabled"),
						resultSet.getBoolean(
								"overdue_notification_enabled"),
						resultSet.getInt("suspension_days"),
						resultSet.getBoolean(
								"vip_immediate_cancellation"),
						resultSet.getString("security_key"));
			}
		}
	}

	public static synchronized void save(Settings settings)
			throws SQLException {
		String sql =
				"UPDATE app_settings SET " +
				"normal_max_borrow_days = ?, vip_max_borrow_days = ?, " +
				"normal_max_borrow_books = ?, vip_max_borrow_books = ?, " +
				"normal_max_reserve_books = ?, vip_max_reserve_books = ?, " +
				"due_notification_enabled = ?, due_notification_days = ?, " +
				"reservation_notification_enabled = ?, " +
				"overdue_notification_enabled = ?, suspension_days = ?, " +
				"vip_immediate_cancellation = ?, security_key = ? " +
				"WHERE settings_id = 1";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			setValues(statement, settings);
			statement.executeUpdate();
			current = settings;
		}
	}

	private static void setValues(
			PreparedStatement statement, Settings settings)
			throws SQLException {
		statement.setInt(1, settings.normalMaxBorrowDays());
		statement.setInt(2, settings.vipMaxBorrowDays());
		statement.setInt(3, settings.normalMaxBorrowBooks());
		statement.setInt(4, settings.vipMaxBorrowBooks());
		statement.setInt(5, settings.normalMaxReserveBooks());
		statement.setInt(6, settings.vipMaxReserveBooks());
		statement.setBoolean(7, settings.dueNotificationEnabled());
		statement.setInt(8, settings.dueNotificationDays());
		statement.setBoolean(9, settings.reservationNotificationEnabled());
		statement.setBoolean(10, settings.overdueNotificationEnabled());
		statement.setInt(11, settings.suspensionDays());
		statement.setBoolean(12, settings.vipImmediateCancellation());
		statement.setString(13, settings.securityKey());
	}

	public record Settings(
			int normalMaxBorrowDays,
			int vipMaxBorrowDays,
			int normalMaxBorrowBooks,
			int vipMaxBorrowBooks,
			int normalMaxReserveBooks,
			int vipMaxReserveBooks,
			boolean dueNotificationEnabled,
			int dueNotificationDays,
			boolean reservationNotificationEnabled,
			boolean overdueNotificationEnabled,
			int suspensionDays,
			boolean vipImmediateCancellation,
			String securityKey) {

		public int maxBorrowDays(String roleLevel) {
			return "VIP".equals(roleLevel)
					? vipMaxBorrowDays
					: normalMaxBorrowDays;
		}

		public int maxBorrowBooks(String roleLevel) {
			return "VIP".equals(roleLevel)
					? vipMaxBorrowBooks
					: normalMaxBorrowBooks;
		}

		public int maxReserveBooks(String roleLevel) {
			return "VIP".equals(roleLevel)
					? vipMaxReserveBooks
					: normalMaxReserveBooks;
		}
	}
}
