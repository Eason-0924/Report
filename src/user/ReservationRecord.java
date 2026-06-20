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

public class ReservationRecord extends JPanel {
	private static final long serialVersionUID = 1L;

	private final int userId;
	private final DefaultTableModel tableModel;
	private final JTable reservationTable;

	public ReservationRecord(int userId) {
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

		if (Reservation.cancelReservation(reservationId, bookId)) {
			loadReservations();
			JOptionPane.showMessageDialog(
					this,
					"Reservation cancelled.",
					"Cancel Succeed",
					JOptionPane.PLAIN_MESSAGE);
		}
	}
}
