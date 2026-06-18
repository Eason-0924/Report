package admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import system.StartSystem;

public class Analysis extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int MAX_TOPICS = 10;
	private static final int MAX_TOP_USERS = 3;

	private final AnalysisChartPanel chartPanel = new AnalysisChartPanel();
	public Analysis() {
		super(new BorderLayout(10, 10));
		super.setBorder(new EmptyBorder(10, 10, 10, 10));
		super.setBackground(Color.WHITE);

		super.add(chartPanel, BorderLayout.CENTER);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent event) {
				loadTopics();
			}
		});
		loadTopics();
	}

	public void loadTopics() {
		Map<String, Integer> subjectCounts = new HashMap<>();
		int totalBorrowRecords = 0;
		String sql =
				"SELECT b.subjects " +
				"FROM borrow_records r " +
				"JOIN books b ON r.book_id = b.book_id";

		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				totalBorrowRecords++;
				String subjects = resultSet.getString("subjects");
				if (subjects == null || subjects.isBlank()) {
					subjectCounts.merge("Unclassified", 1, Integer::sum);
					continue;
				}
				for (String subject : subjects.split(",")) {
					String normalizedSubject = subject.trim();
					if (!normalizedSubject.isEmpty()) {
						subjectCounts.merge(
								normalizedSubject, 1, Integer::sum);
					}
				}
			}
			chartPanel.setData(subjectCounts, totalBorrowRecords);
			loadTopUsers();
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to load hot topic data.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void loadTopUsers() {
		List<UserBorrowCount> topUsers = new ArrayList<>();
		String sql =
				"SELECT u.name, u.student_no, COUNT(*) AS borrow_count " +
				"FROM borrow_records r " +
				"JOIN users u ON r.user_id = u.user_id " +
				"GROUP BY u.user_id, u.name, u.student_no " +
				"ORDER BY borrow_count DESC, u.user_id ASC " +
				"LIMIT ?";

		try (PreparedStatement statement = StartSystem.db.prepareStatement(sql)) {
			statement.setInt(1, MAX_TOP_USERS);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					topUsers.add(new UserBorrowCount(
							resultSet.getString("name"),
							resultSet.getString("student_no"),
							resultSet.getInt("borrow_count")));
				}
			}
			chartPanel.setTopUsers(topUsers);
		} catch (SQLException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Failed to load top borrowing users.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static class AnalysisChartPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private static final Color[] COLORS = {
				new Color(255, 153, 153),
				new Color(255, 204, 153),
				new Color(255, 229, 153),
				new Color(204, 235, 197),
				new Color(179, 217, 255),
				new Color(204, 204, 255),
				new Color(229, 204, 255),
				new Color(255, 204, 229),
				new Color(194, 224, 224),
				new Color(224, 224, 224)
		};

		private final List<TopicSlice> slices = new ArrayList<>();
		private final List<UserBorrowCount> topUsers = new ArrayList<>();
		private int totalCount = 0;
		private int totalBorrowRecords = 0;
		private double animationProgress = 1.0;
		private Timer animationTimer;

		AnalysisChartPanel() {
			super.setBackground(Color.WHITE);
			super.setPreferredSize(new Dimension(1050, 700));
		}

		void setData(Map<String, Integer> data, int borrowRecords) {
			slices.clear();
			totalCount = 0;
			totalBorrowRecords = borrowRecords;
			int colorIndex = 0;
			List<Map.Entry<String, Integer>> entries =
					new ArrayList<>(data.entrySet());
			entries.sort((first, second) ->
					Integer.compare(second.getValue(), first.getValue()));
			int displayedTopics = Math.min(MAX_TOPICS, entries.size());
			for (int index = 0; index < displayedTopics; index++) {
				Map.Entry<String, Integer> entry = entries.get(index);
				totalCount += entry.getValue();
				slices.add(new TopicSlice(
						entry.getKey(),
						entry.getValue(),
						COLORS[colorIndex % COLORS.length]));
				colorIndex++;
			}
			int otherCount = 0;
			for (int index = displayedTopics; index < entries.size(); index++) {
				otherCount += entries.get(index).getValue();
			}
			if (otherCount > 0) {
				totalCount += otherCount;
				slices.add(new TopicSlice(
						"Others",
						otherCount,
						new Color(180, 180, 180)));
			}
		}

		void setTopUsers(List<UserBorrowCount> users) {
			topUsers.clear();
			topUsers.addAll(users);
			startAnimation();
		}

		private void startAnimation() {
			if (animationTimer != null && animationTimer.isRunning()) {
				animationTimer.stop();
			}
			animationProgress = 0.0;
			animationTimer = new Timer(16, _ -> {
				animationProgress = Math.min(1.0, animationProgress + 0.035);
				repaint();
				if (animationProgress >= 1.0) {
					animationTimer.stop();
				}
			});
			animationTimer.start();
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			Graphics2D graphics2D = (Graphics2D) graphics.create();
			graphics2D.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			graphics2D.setFont(new Font("SansSerif", Font.PLAIN, 14));

			graphics2D.setColor(Color.DARK_GRAY);
			graphics2D.setFont(new Font("SansSerif", Font.BOLD, 18));
			graphics2D.drawString("Hot Topic", 120, 35);
			graphics2D.setFont(new Font("SansSerif", Font.PLAIN, 14));
			graphics2D.drawString(
					totalBorrowRecords + " borrow record(s)",
					120,
					60);

			if (slices.isEmpty() || totalCount == 0) {
				graphics2D.setColor(Color.DARK_GRAY);
				graphics2D.drawString(
						"No borrow records to display.",
						120,
						105);
				graphics2D.dispose();
				return;
			}

			int diameter = 300;
			int pieX = Math.max(80, getWidth() / 4 - diameter / 2);
			int pieY = 90;
			int startAngle = 0;
			int visibleAngle =
					(int) Math.round(360 * easedProgress());

			for (TopicSlice slice : slices) {
				int fullArcAngle = (int) Math.round(
						slice.count() * 360.0 / totalCount);
				int remainingAngle = visibleAngle - startAngle;
				if (remainingAngle <= 0) {
					break;
				}
				int arcAngle = Math.min(fullArcAngle, remainingAngle);
				graphics2D.setColor(slice.color());
				graphics2D.fillArc(
						pieX,
						pieY,
						diameter,
						diameter,
						startAngle,
						arcAngle);
				startAngle += arcAngle;
			}

			graphics2D.setColor(Color.WHITE);
			graphics2D.fillOval(pieX + 105, pieY + 105, 90, 90);
			graphics2D.setColor(Color.DARK_GRAY);
			graphics2D.setFont(new Font("SansSerif", Font.BOLD, 18));
			graphics2D.drawString(
					String.valueOf(totalCount),
					pieX + 136,
					pieY + 145);
			graphics2D.setFont(new Font("SansSerif", Font.PLAIN, 13));
			graphics2D.drawString("subject", pieX + 130, pieY + 167);

			drawLegend(graphics2D, Math.max(getWidth() / 2 + 25, 570), 85);
			drawTopUsers(graphics2D, 120, 465);
			graphics2D.dispose();
		}

		private void drawLegend(Graphics2D graphics2D, int x, int y) {
			graphics2D.setFont(new Font("SansSerif", Font.BOLD, 16));
			graphics2D.setColor(Color.DARK_GRAY);
			graphics2D.drawString("Subject Borrow Amount", x, y);

			graphics2D.setFont(new Font("SansSerif", Font.PLAIN, 14));
			int currentY = y + 28;
			for (TopicSlice slice : slices) {
				double percent = slice.count() * 100.0 / totalCount;
				graphics2D.setColor(slice.color());
				graphics2D.fillRoundRect(x, currentY - 12, 18, 18, 6, 6);
				graphics2D.setColor(Color.DARK_GRAY);
				graphics2D.drawString(
						slice.subject() + " - " + slice.count()
								+ " (" + String.format("%.1f", percent) + "%)",
						x + 30,
						currentY + 2);
				currentY += 25;
			}
		}

		private void drawTopUsers(Graphics2D graphics2D, int x, int y) {
			graphics2D.setFont(new Font("SansSerif", Font.BOLD, 17));
			graphics2D.setColor(Color.DARK_GRAY);
			graphics2D.drawString("Top Borrowing Users", x, y);

			if (topUsers.isEmpty()) {
				graphics2D.setFont(new Font("SansSerif", Font.PLAIN, 14));
				graphics2D.drawString(
						"No borrowing volume to display.",
						x,
						y + 35);
				return;
			}

			int maxCount = topUsers.stream()
					.mapToInt(UserBorrowCount::count)
					.max()
					.orElse(1);
			int labelWidth = 230;
			int maxBarWidth = 540;
			int barHeight = 28;
			int gap = 22;
			int currentY = y + 40;

			graphics2D.setFont(new Font("SansSerif", Font.PLAIN, 14));
			for (int index = 0; index < topUsers.size(); index++) {
				UserBorrowCount user = topUsers.get(index);
				int fullBarWidth = Math.max(
						8,
						(int) Math.round(
								user.count() * maxBarWidth / (double) maxCount));
				int barWidth = (int) Math.round(
						fullBarWidth * easedProgress());
				String label = (index + 1) + ". " + user.name()
						+ " - " + user.studentNo();

				graphics2D.setColor(Color.DARK_GRAY);
				graphics2D.drawString(label, x, currentY + 19);

				int barX = x + labelWidth;
				graphics2D.setColor(new Color(255, 182, 193));
				graphics2D.fillRoundRect(
						barX, currentY, barWidth, barHeight, 12, 12);
				graphics2D.setColor(new Color(210, 95, 115));
				graphics2D.drawRoundRect(
						barX, currentY, barWidth, barHeight, 12, 12);
				graphics2D.setColor(Color.DARK_GRAY);
				graphics2D.drawString(
						String.valueOf(user.count()),
						barX + barWidth + 12,
						currentY + 19);

					currentY += barHeight + gap;
				}
			}

			private double easedProgress() {
				return 1 - Math.pow(1 - animationProgress, 3);
			}
		}

	private record TopicSlice(String subject, int count, Color color) {
	}

	private record UserBorrowCount(String name, String studentNo, int count) {
	}
}
