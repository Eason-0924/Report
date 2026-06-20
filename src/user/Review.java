package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import system.StartSystem;
import system.TableStyle;

public final class Review {
	private Review() {
	}

	public static void showWriteReviewDialog(
			JFrame parent, int userId, int bookId, String bookTitle) {
		JDialog dialog = new JDialog(
				parent, "Write a Review - " + bookTitle, true);
		dialog.setLayout(new BorderLayout());
		dialog.setSize(420, 320);
		dialog.setLocationRelativeTo(parent);

		JPanel formPanel = new JPanel(new GridBagLayout());
		formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		formPanel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JComboBox<Integer> ratingBox =
				new JComboBox<>(new Integer[] {1, 2, 3, 4, 5});
		ratingBox.setSelectedItem(5);
		JTextArea commentArea = new JTextArea(6, 22);
		commentArea.setLineWrap(true);
		commentArea.setWrapStyleWord(true);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		formPanel.add(new JLabel("Rating (1-5):"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		formPanel.add(ratingBox, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		formPanel.add(new JLabel("Comment:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		formPanel.add(new JScrollPane(commentArea), gbc);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBackground(Color.WHITE);
		JButton submitButton = new JButton("Submit");
		JButton skipButton = new JButton("Skip");
		buttonPanel.add(skipButton);
		buttonPanel.add(submitButton);

		submitButton.addActionListener(_ -> {
			saveReview(
					dialog,
					userId,
					bookId,
					(Integer) ratingBox.getSelectedItem(),
					commentArea.getText().trim());
		});
		skipButton.addActionListener(_ -> dialog.dispose());

		dialog.add(formPanel, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}

	private static void saveReview(
			JDialog dialog, int userId, int bookId, int rating, String comment) {
		String insertSql =
				"INSERT INTO reviews(user_id, book_id, rating, comment) " +
				"VALUES (?, ?, ?, ?)";

		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(insertSql)) {
			statement.setInt(1, userId);
			statement.setInt(2, bookId);
			statement.setInt(3, rating);
			statement.setString(4, comment);
			statement.executeUpdate();
			JOptionPane.showMessageDialog(
					dialog,
					"Review submitted. Thank you.",
					"Review Succeed",
					JOptionPane.PLAIN_MESSAGE);
			dialog.dispose();
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					dialog,
					"Failed to submit the review.",
					"Review Failed",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static JPanel createReviewsPanel(int bookId) {
		return createReviewsPanel(bookId, false);
	}

	public static JPanel createReviewsPanel(int bookId, boolean maskUserNames) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);

		JLabel titleLabel = new JLabel("Reviews");
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
		titleLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.add(titleLabel, BorderLayout.NORTH);

		String[] columns = {"User", "Rating", "Comment", "Date"};
		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable reviewTable = new JTable(model);
		reviewTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
		reviewTable.getTableHeader().setFont(
				new Font("SansSerif", Font.BOLD, 14));
		reviewTable.setRowHeight(25);
		TableStyle.applyUserStyle(reviewTable);

		String sql =
				"SELECT u.name, r.rating, r.comment, DATE(r.created_at) review_date " +
				"FROM reviews r JOIN users u ON r.user_id = u.user_id " +
				"WHERE r.book_id = ? ORDER BY r.created_at DESC";
		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, bookId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					model.addRow(new Object[] {
							maskUserNames
									? "*****"
									: resultSet.getString("name"),
							resultSet.getInt("rating") + " / 5",
							resultSet.getString("comment"),
							resultSet.getString("review_date")
					});
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}

		if (model.getRowCount() == 0) {
			model.addRow(new Object[] {"", "", "No reviews yet.", ""});
		}

		panel.add(new JScrollPane(reviewTable), BorderLayout.CENTER);
		return panel;
	}
}
