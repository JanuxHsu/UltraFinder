package com.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class UltraFinderControlForm {

	JFrame mainForm;

	public UltraFinderControlForm() {
		this.initForm();
	}

	private void initForm() {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

			// UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JFrame window = new JFrame("UltraFinder");
		window.setPreferredSize(new Dimension(400, 600));
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		;

		JPanel mainContainer = new JPanel(new BorderLayout());

		// mainContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JPanel topBar = new JPanel(new BorderLayout());

		topBar.setOpaque(true);
		topBar.setPreferredSize(new Dimension(0, 30));
		topBar.setBackground(new Color(5, 196, 107));

		mainContainer.add(topBar, BorderLayout.NORTH);

		JPanel basicSettingsPanel = new JPanel();
		basicSettingsPanel.setLayout(new BoxLayout(basicSettingsPanel, BoxLayout.Y_AXIS));
		//basicSettingsPanel.setPreferredSize(new Dimension(0, 100));

		// basicSettingsPanel.setSize(new Dimension(0, 100));

		basicSettingsPanel.add(new BasicSettingItem("1111", true));
		basicSettingsPanel.add(new BasicSettingItem("2222", true));
		basicSettingsPanel.add(new BasicSettingItem("3333", false));
		basicSettingsPanel.add(new BasicSettingItem("4444", false));
		
		
		mainContainer.add(basicSettingsPanel, BorderLayout.CENTER);

		window.add(mainContainer);

		window.pack();

		this.mainForm = window;

	}

	public void start() {
		this.mainForm.setVisible(true);
	}

	public static void main(String[] args) {
		UltraFinderControlForm ultraFinderControlForm = new UltraFinderControlForm();

		ultraFinderControlForm.start();
	}

}
