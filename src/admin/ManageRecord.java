package admin;

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
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import system.StartSystem;
import system.TableStyle;

public class ManageRecord extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMAT =
			DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final SearchBorrowRecord borrowRecordPanel;
	private final ReservationRecordPanel reservationRecordPanel;
	private final ManageReview reviewRecordPanel;
	private final JTabbedPane recordTabs;

	public ManageRecord() {
		super(new BorderLayout());
		super.setBorder(new EmptyBorder(10, 10, 10, 10));
		super.setBackground(Color.WHITE);

		borrowRecordPanel = new SearchBorrowRecord();
		reservationRecordPanel = new ReservationRecordPanel();
		reviewRecordPanel = new ManageReview();

		recordTabs = new JTabbedPane();
		recordTabs.setUI(createTabUI());
		recordTabs.setFont(new Font("SansSerif", Font.BOLD, 15));
		recordTabs.setBackground(Color.WHITE);
		recordTabs.setOpaque(true);
		recordTabs.addTab("Borrow Records", borrowRecordPanel);
		recordTabs.addTab("Reservation Records", reservationRecordPanel);
		recordTabs.addTab("Review Records", reviewRecordPanel);
		for (int index = 0; index < recordTabs.getTabCount(); index++) {
			recordTabs.setBackgroundAt(index, Color.WHITE);
		}
		recordTabs.addChangeListener(_ -> refreshSelectedTab());

		super.add(recordTabs, BorderLayout.CENTER);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent event) {
				refreshSelectedTab();
			}
		});
	}

	private BasicTabbedPaneUI createTabUI() {
		return new BasicTabbedPaneUI() {
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
				super.paintTabArea(graphics, tabPlacement, selectedIndex);
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
					graphics2D.setColor(new Color(255, 232, 236));
					graphics2D.fillRoundRect(
							x + 1, y + 1, width - 2, height - 2, 18, 18);
					graphics2D.setColor(new Color(210, 95, 115));
					graphics2D.drawRoundRect(
							x + 1, y + 1, width - 3, height - 3, 18, 18);
				}
				graphics2D.dispose();
			}

			@Override
			protected Insets getTabInsets(int tabPlacement, int tabIndex) {
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
				// Rounded selected tab replaces the default divider lines.
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
				// Child panels provide their own spacing.
			}
		};
	}

	public void refreshSelectedTab() {
		int selectedIndex = recordTabs.getSelectedIndex();
		if (selectedIndex == 0) {
			borrowRecordPanel.searchRecords();
		} else if (selectedIndex == 1) {
			reservationRecordPanel.loadReservations();
		} else if (selectedIndex == 2) {
			reviewRecordPanel.loadReviews();
		}
	}

	private static class ReservationRecordPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private final DefaultTableModel tableModel;

		ReservationRecordPanel() {
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

		void loadReservations() {
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
}
