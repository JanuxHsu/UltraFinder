package com.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class BasicSettingItem extends JPanel {

	private static final long serialVersionUID = 9148707768191951356L;
	JLabel itemLabel;
	JTextField itemInput;
	JTextArea itemInput2;
	JButton itemActionBtn;

	public BasicSettingItem(String itemName, Boolean withBtn) {
		this.setLayout(new BorderLayout());
		// this.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3));
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		this.itemLabel = new JLabel(itemName);
		this.itemLabel.setOpaque(true);
		this.itemLabel.setBackground(new Color(5, 196, 107));
		// this.itemLabel.setForeground(Color.LIGHT_GRAY);
		this.itemLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));

		this.add(this.itemLabel, BorderLayout.WEST);

		this.itemInput2 = new JTextArea();
		this.itemInput2.setLineWrap(true);
		this.itemInput2.setMaximumSize(new Dimension(200, 40));
		this.add(this.itemInput2, BorderLayout.CENTER);

		if (withBtn) {
			this.itemActionBtn = new JButton("Test");
			this.itemActionBtn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

			this.itemActionBtn.setFocusPainted(false);
			// this.itemActionBtn.setContentAreaFilled(false);
			this.itemActionBtn.setMaximumSize(this.itemInput2.getMaximumSize());

			this.add(this.itemActionBtn, BorderLayout.EAST);
		}

	}
}
