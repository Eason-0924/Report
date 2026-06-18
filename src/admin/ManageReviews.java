package admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import system.StartSystem;
import system.TableStyle;

public class ManageReviews extends JPanel {
	private static final long serialVersionUID = 1L;

	private final DefaultTableModel tableModel;
	private final JTable reviewTable;
	private final JLabel countLabel;
	private final List<Integer> reviewIds = new ArrayList<>();

	public ManageReviews() {
		super(new BorderLayout(10, 10));
		super.setBorder(new EmptyBorder(10, 10, 10, 10));
		super.setBackground(Color.WHITE);

		String[] columns = {
				"Book Title",
				"Username",
				"Student No.",
				"Rating",
				"Comment",
				"Created At"
		};
		tableModel = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		reviewTable = new JTable(tableModel);
		reviewTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
		reviewTable.getTableHeader().setFont(
				new Font("SansSerif", Font.BOLD, 14));
		reviewTable.setRowHeight(28);
		reviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reviewTable.setAutoCreateRowSorter(true);
		TableStyle.applyAdminStyle(reviewTable);
		configureColumnWidths();

		JScrollPane scrollPane = new JScrollPane(reviewTable);
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(Color.WHITE);
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		countLabel = new JLabel("0 review(s)");
		countLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

		JButton deleteButton = new JButton("Delete Selected Review");
		deleteButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
		deleteButton.addActionListener(_ -> deleteSelectedReview());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(Color.WHITE);
		buttonPanel.add(countLabel);
		buttonPanel.add(deleteButton);

		super.add(tablePanel, BorderLayout.CENTER);
		super.add(buttonPanel, BorderLayout.SOUTH);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent event) {
				loadReviews();
			}
		});
		loadReviews();
	}

	private void configureColumnWidths() {
		TableColumnModel columns = reviewTable.getColumnModel();
		columns.getColumn(0).setPreferredWidth(240);
		columns.getColumn(1).setPreferredWidth(120);
		columns.getColumn(2).setPreferredWidth(110);
		columns.getColumn(3).setPreferredWidth(70);
		columns.getColumn(4).setPreferredWidth(360);
		columns.getColumn(5).setPreferredWidth(150);
	}

	public void loadReviews() {
		tableModel.setRowCount(0);
		reviewIds.clear();
		String sql =
				"SELECT r.review_id, b.title, u.name, u.student_no, " +
				"r.rating, r.comment, r.created_at " +
				"FROM reviews r " +
				"JOIN books b ON r.book_id = b.book_id " +
				"JOIN users u ON r.user_id = u.user_id " +
				"ORDER BY r.created_at DESC, r.review_id DESC";

		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql);
			ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				reviewIds.add(resultSet.getInt("review_id"));
				tableModel.addRow(new Object[] {
						resultSet.getString("title"),
						resultSet.getString("name"),
						resultSet.getString("student_no"),
						resultSet.getInt("rating"),
						resultSet.getString("comment"),
						resultSet.getString("created_at")
				});
			}
			countLabel.setText(tableModel.getRowCount() + " review(s)");
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to load review records.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void deleteSelectedReview() {
		int selectedRow = reviewTable.getSelectedRow();
		if (selectedRow < 0) {
			JOptionPane.showMessageDialog(
					this,
					"Please select a review first.",
					"Manage Review",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		int modelRow = reviewTable.convertRowIndexToModel(selectedRow);
		int reviewId = reviewIds.get(modelRow);
		String bookTitle = String.valueOf(tableModel.getValueAt(modelRow, 0));

		int option = JOptionPane.showOptionDialog(
				this,
				"Delete the selected review for \"" + bookTitle + "\"?",
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				new Object[] {"Delete", "Cancel"},
				"Delete");
		if (option != JOptionPane.YES_OPTION) {
			return;
		}

		String sql = "DELETE FROM reviews WHERE review_id = ?";
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, reviewId);
			statement.executeUpdate();
			loadReviews();
			JOptionPane.showMessageDialog(
					this,
					"Review deleted.",
					"Manage Review",
					JOptionPane.PLAIN_MESSAGE);
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to delete the selected review.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
