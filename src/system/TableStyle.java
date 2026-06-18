package system;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public final class TableStyle {
	private TableStyle() {
	}

	public static void applyUserStyle(JTable table) {
		apply(table, new Color(232, 243, 255), new Color(215, 234, 252));
	}

	public static void applyAdminStyle(JTable table) {
		apply(table, new Color(255, 238, 241), new Color(255, 224, 230));
	}

	private static void apply(
			JTable table,
			Color alternateColor,
			Color hoverColor) {
		table.setShowGrid(false);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(hoverColor);
		table.setSelectionForeground(Color.BLACK);

		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		header.setBackground(Color.WHITE);
		header.setBorder(new EmptyBorder(6, 6, 6, 6));

		table.putClientProperty("hoverRow", -1);
		table.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent event) {
				int row = table.rowAtPoint(event.getPoint());
				if ((int) table.getClientProperty("hoverRow") != row) {
					table.putClientProperty("hoverRow", row);
					table.repaint();
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent event) {
				table.putClientProperty("hoverRow", -1);
				table.repaint();
			}
		});

		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(
					JTable table,
					Object value,
					boolean selected,
					boolean focused,
					int row,
					int column) {
				Component component =
						super.getTableCellRendererComponent(
								table, value, selected, focused, row, column);
				component.setFont(table.getFont());
				if (selected) {
					component.setBackground(table.getSelectionBackground());
					component.setForeground(table.getSelectionForeground());
				} else if (row == (int) table.getClientProperty("hoverRow")) {
					component.setBackground(hoverColor);
					component.setForeground(Color.BLACK);
				} else if (row % 2 == 1) {
					component.setBackground(alternateColor);
					component.setForeground(Color.BLACK);
				} else {
					component.setBackground(Color.WHITE);
					component.setForeground(Color.BLACK);
				}
					if (component instanceof DefaultTableCellRenderer renderer) {
						renderer.setBorder(createCellBorder(
								selected,
								table,
								column,
								table.getColumnCount()));
					}
					return component;
				}
			});

		Font headerFont = header.getFont();
		if (headerFont != null) {
			header.setFont(headerFont.deriveFont(Font.BOLD));
		}
	}

	private static Border createCellBorder(
			boolean selected,
			JTable table,
			int column,
			int columnCount) {
		if (!selected) {
			return new EmptyBorder(0, 8, 0, 8);
		}

		int left = isFirstVisibleColumn(table, column) ? 1 : 0;
		int right = isLastVisibleColumn(table, column, columnCount) ? 1 : 0;
		return new CompoundBorder(
				new MatteBorder(
				1,
				left,
				1,
				right,
				new Color(120, 120, 120)),
				new EmptyBorder(0, 8 - left, 0, 8 - right));
	}

	private static boolean isFirstVisibleColumn(JTable table, int column) {
		for (int index = column - 1; index >= 0; index--) {
			if (table.getColumnModel().getColumn(index).getWidth() > 0) {
				return false;
			}
		}
		return true;
	}

	private static boolean isLastVisibleColumn(
			JTable table,
			int column,
			int columnCount) {
		for (int index = column + 1; index < columnCount; index++) {
			if (table.getColumnModel().getColumn(index).getWidth() > 0) {
				return false;
			}
		}
		return true;
	}
}
