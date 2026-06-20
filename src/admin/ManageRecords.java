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

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class ManageRecords extends JPanel {
	private static final long serialVersionUID = 1L;

	private final BorrowRecord borrowRecordPanel;
	private final ReservationRecord reservationRecordPanel;
	private final ReviewRecord reviewRecordPanel;
	private final JTabbedPane recordTabs;

	public ManageRecords() {
		super(new BorderLayout());
		super.setBorder(new EmptyBorder(10, 10, 10, 10));
		super.setBackground(Color.WHITE);

		borrowRecordPanel = new BorrowRecord();
		reservationRecordPanel = new ReservationRecord();
		reviewRecordPanel = new ReviewRecord();

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
}
