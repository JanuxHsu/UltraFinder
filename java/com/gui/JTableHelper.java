package com.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class JTableHelper {
	public static void resizeColumnWidth(JTable table, int minWidth, int maxWidth) {

		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = minWidth;
			// Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			if (width > maxWidth) {
				width = maxWidth;
			}

			columnModel.getColumn(column).setPreferredWidth(width);
		}

	}
}
