package user;

import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.Timer;

import librarySystem.LibrarySystemUser;

public class TabButton extends JButton {
	private static final long serialVersionUID = 1L;
	private boolean selected = false;
	private boolean rightButtonSelected = false;
	private final Color backgroundColor;
	private float selectedProgress = 0.0f;
	private Timer animationTimer;

	/**
	 *  Create a JButton with a text, then set it to unpaint.
	 *  @param text
	 **/
	public TabButton(String text) {
		this(text, LibrarySystemUser.background);
	}

	public TabButton(String text, Color backgroundColor) {
		super(text);
		this.backgroundColor = backgroundColor;
		setFocusPainted(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setOpaque(false);
	}

	public void setSelectedTab(boolean selected) {
		this.selected = selected;
		animateSelection();
	}

	public void setBesides(boolean rightSelected) {
		this.rightButtonSelected = rightSelected;
	}

	private void animateSelection() {
		if (animationTimer != null && animationTimer.isRunning()) {
			animationTimer.stop();
		}
		animationTimer = new Timer(16, _ -> {
			float target = selected ? 1.0f : 0.0f;
			if (Math.abs(selectedProgress - target) <= 0.08f) {
				selectedProgress = target;
				animationTimer.stop();
			} else if (selectedProgress < target) {
				selectedProgress += 0.08f;
			} else {
				selectedProgress -= 0.08f;
			}
			repaint();
		});
		animationTimer.start();
	}

	private Color blend(Color from, Color to, float progress) {
		float eased = 1 - (float) Math.pow(1 - progress, 3);
		int red = Math.round(from.getRed()
				+ (to.getRed() - from.getRed()) * eased);
		int green = Math.round(from.getGreen()
				+ (to.getGreen() - from.getGreen()) * eased);
		int blue = Math.round(from.getBlue()
				+ (to.getBlue() - from.getBlue()) * eased);
		return new Color(red, green, blue);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();
		g2.setStroke(new BasicStroke(2f));

		// Paint background
		g2.setColor(backgroundColor);
		g2.fillRect(0, 0, w, h);

		if (selectedProgress > 0.0f) {
			// Selected tab (rounded top)
			g2.setColor(blend(backgroundColor, Color.WHITE, selectedProgress));

			// extend bottom to hide curve
			g2.fillRoundRect(0, 0, w, h + 10, 20, 20);

			// Remove bottom border effect
			g2.setColor(Color.GRAY);
			g2.drawRoundRect(1, 0, w - 2, h + 10, 20, 20);
		}

		if (selectedProgress < 1.0f) {
			// Normal tab
			if (selectedProgress == 0.0f) {
				g2.setColor(backgroundColor);
				g2.fillRect(0, 0, w, h);
			}

			// Short separator line (centered)
			g2.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 1.0f - selectedProgress));
			g2.setColor(Color.GRAY);
			int lineHeight = 20;
			int x = w - 1;
			int y = (h - lineHeight) / 2;

			if (!rightButtonSelected) {
				g2.drawLine(x, y, x, y + lineHeight);
			}

			g2.drawLine(0, h - 1, w, h - 1);
		}

		g2.dispose();
		
		// draw text
		super.paintComponent(g);
	}
}
