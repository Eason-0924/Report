package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import system.StartSystem;
import system.TableStyle;

public class ReviewRecord extends JPanel {
	private static final long serialVersionUID = 1L;

	private final int userId;
	private final DefaultTableModel tableModel;

	public ReviewRecord(int userId) {
		this.userId = userId;

		super.setLayout(new BorderLayout());
		super.setBorder(new EmptyBorder(0, 0, 0, 0));
		super.setBackground(Color.WHITE);

		tableModel = new DefaultTableModel(new String[] {
				"Book Title", "Rating", "Comment", "Review Date"
		}, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable table = new JTable(tableModel);
		table.setFont(new Font("SansSerif", Font.PLAIN, 15));
		table.getTableHeader().setFont(
				new Font("SansSerif", Font.BOLD, 15));
		table.setRowHeight(28);
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setReorderingAllowed(false);
		TableStyle.applyUserStyle(table);

		super.add(new JScrollPane(table), BorderLayout.CENTER);
		loadRecords();
	}

	public void loadRecords() {
		String sql =
				"SELECT b.title, r.rating, r.comment, " +
				"DATE(r.created_at) AS review_date " +
				"FROM reviews r " +
				"JOIN books b ON r.book_id = b.book_id " +
				"WHERE r.user_id = ? ORDER BY r.created_at DESC";

		tableModel.setRowCount(0);
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					tableModel.addRow(new Object[] {
							resultSet.getString("title"),
							resultSet.getInt("rating") + " / 5",
							resultSet.getString("comment"),
							resultSet.getString("review_date")
					});
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}
}
