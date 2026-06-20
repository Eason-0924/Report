package admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import system.StartSystem;
import system.TableStyle;

public class ReservationRecord extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMAT =
			DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final DefaultTableModel tableModel;

	public ReservationRecord() {
		super(new BorderLayout());
		super.setBorder(new EmptyBorder(10, 10, 10, 10));
		super.setBackground(Color.WHITE);

		tableModel = new DefaultTableModel(new String[] {
				"Book Title",
				"Username",
				"Student No.",
				"Reserved At",
				"Status",
				"Notified"
		}, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable table = new JTable(tableModel);
		table.setFont(new Font("SansSerif", Font.PLAIN, 14));
		table.getTableHeader().setFont(
				new Font("SansSerif", Font.BOLD, 14));
		table.setRowHeight(28);
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setReorderingAllowed(false);
		TableStyle.applyAdminStyle(table);

		TableColumnModel columns = table.getColumnModel();
		columns.getColumn(0).setPreferredWidth(260);
		columns.getColumn(1).setPreferredWidth(120);
		columns.getColumn(2).setPreferredWidth(110);
		columns.getColumn(3).setPreferredWidth(140);
		columns.getColumn(4).setPreferredWidth(120);
		columns.getColumn(5).setPreferredWidth(80);

		super.add(new JScrollPane(table), BorderLayout.CENTER);
		loadReservations();
	}

	public void loadReservations() {
		tableModel.setRowCount(0);
		String sql =
				"SELECT b.title, u.name, u.student_no, r.reserved_at, " +
				"CASE WHEN r.status = 'READY' THEN 'READY TO BORROW' " +
				"ELSE r.status END AS reservation_status, r.notified " +
				"FROM reservations r " +
				"JOIN books b ON r.book_id = b.book_id " +
				"JOIN users u ON r.user_id = u.user_id " +
				"ORDER BY r.reserved_at DESC, r.reservation_id DESC";

		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				tableModel.addRow(new Object[] {
						resultSet.getString("title"),
						resultSet.getString("name"),
						resultSet.getString("student_no"),
						formatDate(resultSet.getTimestamp("reserved_at")),
						resultSet.getString("reservation_status"),
						resultSet.getBoolean("notified") ? "YES" : "NO"
				});
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to load reservation records.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private String formatDate(Timestamp timestamp) {
		if (timestamp == null) {
			return "-";
		}
		return timestamp.toLocalDateTime().toLocalDate().format(DATE_FORMAT);
	}
}
