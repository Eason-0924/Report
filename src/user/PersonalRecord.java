package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import system.StartSystem;
import system.TableStyle;

public class PersonalRecord extends JPanel {
	private static final long serialVersionUID = 1L;

	private final int userId;
	private final DefaultTableModel borrowModel;
	private final DefaultTableModel reviewModel;
	private final Reservation reservationPanel;

	public PersonalRecord(int userId) {
		this.userId = userId;

		super.setLayout(new BorderLayout());
		super.setBorder(new EmptyBorder(10, 10, 10, 10));
		super.setBackground(Color.WHITE);

		borrowModel = createReadOnlyModel(new String[] {
				"Book Title", "Borrow Date", "Due Date", "Return Date"
		});
		reviewModel = createReadOnlyModel(new String[] {
				"Book Title", "Rating", "Comment", "Review Date"
		});

		JTable borrowTable = createTable(borrowModel);
		JTable reviewTable = createTable(reviewModel);
		reservationPanel = new Reservation(userId);
		reservationPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

		JTabbedPane recordsTabs = new JTabbedPane();
		recordsTabs.setUI(new BasicTabbedPaneUI() {
			@Override
			protected void paintTabArea(
					Graphics graphics,
					int tabPlacement,
					int selectedIndex) {
				graphics.setColor(Color.WHITE);
				graphics.fillRect(
						0, 0,
						tabPane.getWidth(),
						calculateTabAreaHeight(
								tabPlacement,
								runCount,
								maxTabHeight));
				super.paintTabArea(
						graphics, tabPlacement, selectedIndex);
			}

			@Override
			protected void paintTabBackground(
					Graphics graphics,
					int tabPlacement,
					int tabIndex,
					int x,
					int y,
					int width,
					int height,
					boolean selected) {
				Graphics2D graphics2D = (Graphics2D) graphics.create();
				graphics2D.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				graphics2D.setColor(Color.WHITE);
				graphics2D.fillRect(x, y, width, height);
				if (selected) {
					graphics2D.setColor(new Color(225, 239, 252));
					graphics2D.fillRoundRect(
							x + 1, y + 1, width - 2, height - 2, 18, 18);
					graphics2D.setColor(new Color(70, 130, 180));
					graphics2D.drawRoundRect(
							x + 1, y + 1, width - 3, height - 3, 18, 18);
				}
				graphics2D.dispose();
			}

			@Override
			protected Insets getTabInsets(
					int tabPlacement, int tabIndex) {
				return new Insets(9, 20, 9, 20);
			}

			@Override
			protected void paintTabBorder(
					Graphics graphics,
					int tabPlacement,
					int tabIndex,
					int x,
					int y,
					int width,
					int height,
					boolean selected) {
				// Rounded selection replaces the default divider lines.
			}

			@Override
			protected void paintFocusIndicator(
					Graphics graphics,
					int tabPlacement,
					java.awt.Rectangle[] rectangles,
					int tabIndex,
					java.awt.Rectangle iconRectangle,
					java.awt.Rectangle textRectangle,
					boolean selected) {
				// The rounded selected-tab border provides the focus indicator.
			}

			@Override
			protected void paintContentBorder(
					Graphics graphics,
					int tabPlacement,
					int selectedIndex) {
				// The tables already provide their own visual boundary.
			}
		});
		recordsTabs.setFont(new Font("SansSerif", Font.BOLD, 15));
		recordsTabs.setBackground(Color.WHITE);
		recordsTabs.setOpaque(true);
		recordsTabs.addTab(
				"Borrow Records", new JScrollPane(borrowTable));
		recordsTabs.addTab(
				"Reservation Records", reservationPanel);
		recordsTabs.addTab(
				"Review Records", new JScrollPane(reviewTable));
		for (int index = 0; index < recordsTabs.getTabCount(); index++) {
			recordsTabs.setBackgroundAt(index, Color.WHITE);
		}
		super.add(recordsTabs, BorderLayout.CENTER);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent event) {
				loadRecords();
			}
		});

		loadRecords();
	}

	private DefaultTableModel createReadOnlyModel(String[] columns) {
		return new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
	}

	private JTable createTable(DefaultTableModel model) {
		JTable table = new JTable(model);
		table.setFont(new Font("SansSerif", Font.PLAIN, 15));
		table.getTableHeader().setFont(
				new Font("SansSerif", Font.BOLD, 15));
		table.setRowHeight(28);
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setReorderingAllowed(false);
		TableStyle.applyUserStyle(table);
		return table;
	}

	public void loadRecords() {
		loadBorrowRecords();
		reservationPanel.loadReservations();
		loadReviewRecords();
	}

	private void loadBorrowRecords() {
		String sql =
				"SELECT b.title, r.borrow_date, r.due_date, r.return_date " +
				"FROM borrow_records r " +
				"JOIN books b ON r.book_id = b.book_id " +
				"WHERE r.user_id = ? ORDER BY r.borrow_date DESC";

		borrowModel.setRowCount(0);
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					String returnDate = resultSet.getString("return_date");
					borrowModel.addRow(new Object[] {
							resultSet.getString("title"),
							resultSet.getString("borrow_date"),
							resultSet.getString("due_date"),
							returnDate == null ? "Borrowing" : returnDate
					});
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}

	private void loadReviewRecords() {
		String sql =
				"SELECT b.title, r.rating, r.comment, " +
				"DATE(r.created_at) AS review_date " +
				"FROM reviews r " +
				"JOIN books b ON r.book_id = b.book_id " +
				"WHERE r.user_id = ? ORDER BY r.created_at DESC";

		reviewModel.setRowCount(0);
		try (PreparedStatement statement =
				StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					reviewModel.addRow(new Object[] {
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
