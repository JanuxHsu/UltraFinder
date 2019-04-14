package com.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;

import com.UltraFinder.UltraFinder;

import model.WorkerThreadInfo;
import model.UltraFinderConfig.UltraFinderMode;
import model.WorkerThreadInfo.ThreadStatus;

public class UltraFinderForm {

	HashMap<String, JLabel> fileRelatedInfo = new HashMap<>();
	HashMap<String, JLabel> threadIndicators = new HashMap<>();

	public static String title = "UltraFinder v3.1 (by JanuxHsu)";

	JPanel threadPanel;

	JFrame window;
	final UltraFinder ultraFinder;
	JLabel totalWorkCntLabel;

	JProgressBar totalWorkBar;
	JTextArea loggingBox;

	JTable resultTable;
	DefaultTableModel tableModel;

	private Action[] textActions = { new DefaultEditorKit.CopyAction() };

	public UltraFinderForm(UltraFinder ultraFinder) {
		this.ultraFinder = ultraFinder;

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

			// UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.window = new JFrame(title);

		JMenu menu = new JMenu("Edit");
		for (Action textAction : textActions) {
			menu.add(new JMenuItem(textAction));
		}

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu);

		window.setJMenuBar(menuBar);

		window.setPreferredSize(new Dimension(800, 600));

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		window.setIconImage(Toolkit.getDefaultToolkit()
				.getImage(this.getClass().getClassLoader().getResource("resources/icon.png")));

		JPanel topPanel = setupTopPanel();

		JPanel centerPanel = setupCenterPanel();

		window.add(topPanel, BorderLayout.NORTH);
		window.add(centerPanel, BorderLayout.CENTER);

		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);

	}

	private JPanel setupTopPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());

		this.totalWorkCntLabel = new JLabel(" Total files found: 0");
		this.totalWorkCntLabel.setOpaque(true);
		this.totalWorkCntLabel.setBackground(Color.LIGHT_GRAY);

		this.totalWorkBar = new JProgressBar();
		this.totalWorkBar.setStringPainted(true);

		this.threadPanel = new JPanel();

		JPanel searchInfoPanel = new JPanel();
		searchInfoPanel.setLayout(new GridLayout(1, 3));

		JLabel totalFolderLabel = new JLabel();
		JLabel totalFileLabel = new JLabel();

		this.fileRelatedInfo.put("directories", totalFolderLabel);
		this.fileRelatedInfo.put("files", totalFileLabel);

		for (String label_key : this.fileRelatedInfo.keySet()) {
			JLabel label = this.fileRelatedInfo.get(label_key);
			searchInfoPanel.add(label);

		}

		searchInfoPanel.add(totalWorkCntLabel);

		topPanel.add(searchInfoPanel, BorderLayout.NORTH);
		topPanel.add(totalWorkBar, BorderLayout.CENTER);
		topPanel.add(threadPanel, BorderLayout.SOUTH);

		return topPanel;
	}

	private JPanel setupCenterPanel() {
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		// splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

		this.loggingBox = new JTextArea();

		DefaultCaret caret = (DefaultCaret) this.loggingBox.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		this.loggingBox.setEditable(false);
		// this.loggingBox.setPreferredSize(new Dimension(600, 300));

		this.tableModel = new DefaultTableModel();
		this.tableModel.addColumn("No.");

		if (this.ultraFinder.getConfig().ultraFinderMode == UltraFinderMode.FILESIZE) {
			this.tableModel.addColumn("Size");
		} else {
			this.tableModel.addColumn("Cnt");
		}

		this.tableModel.addColumn("Path");

		this.resultTable = new JTable(this.tableModel);
		this.resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.resultTable.setAutoCreateRowSorter(true);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		this.resultTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		this.resultTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

		this.resultTable.setPreferredScrollableViewportSize(new Dimension(600, 300));

		this.resultTable.addKeyListener(new ClipboardKeyAdapter(this.resultTable));

		splitPane.add(new JScrollPane(this.resultTable));
		splitPane.add(new JScrollPane(this.loggingBox));
		// centerPanel.add(new JScrollPane(this.resultTable), BorderLayout.NORTH);
		// centerPanel.add(new JScrollPane(this.loggingBox), BorderLayout.CENTER);
		centerPanel.add(splitPane, BorderLayout.CENTER);
		return centerPanel;
	}

	public void updateFileCount(Integer folderCnt, Integer fileCnt, Integer foundFileCnt, int queuedJobCount) {

		SwingUtilities.invokeLater(() -> {

			this.fileRelatedInfo.get("directories").setText(" Dir : " + folderCnt);
			this.fileRelatedInfo.get("files").setText(" Files : " + fileCnt);

			this.totalWorkCntLabel.setText(" Total files found: " + foundFileCnt);
			if (queuedJobCount == 0) {
				this.window.setTitle(title);
			} else {
				this.window.setTitle(title + " Jobs : [" + queuedJobCount + "]");
			}

		});

	}

	public void updateTotalProgressCount(int needToScanCount) {

		this.totalWorkBar.setMaximum(needToScanCount);
	}

	public void updateSearchProgress(Integer curr_cnt) {
		// minus thread pool check job count

		SwingUtilities.invokeLater(() -> {
			this.totalWorkBar.setValue(curr_cnt);
			this.totalWorkBar
					.setString(String.format("Completed (%s/%s) of Jobs.", curr_cnt, this.totalWorkBar.getMaximum()));
		});
	}

	public void appendLog(String line) {

		SwingUtilities.invokeLater(() -> {
			this.loggingBox.append(String.format(line + "%n"));
		});

	}

	public void updateResultTable(Integer count, String filePath) {
		SwingUtilities.invokeLater(() -> {
			this.tableModel.addRow(new Object[] { this.resultTable.getRowCount() + 1, count, filePath });
			if (this.resultTable.getRowCount() % 10 == 1) {
				JTableHelper.resizeColumnWidth(this.resultTable, 40, 1000);
			}
		});

	}

	public void changeTitleName(String title) {
		this.window.setTitle(title);

	}

	public String getTitleName() {
		return this.window.getTitle();
	}

	public void initWorkerThreadId(LinkedHashMap<String, WorkerThreadInfo> threadIndicators) {

		Integer rowNum = (int) Math.sqrt(Double.valueOf(threadIndicators.size()));

		this.threadPanel.setLayout(new GridLayout(rowNum, 0, 1, 3));

		SwingUtilities.invokeLater(() -> {
			for (String thread_id : threadIndicators.keySet()) {
				WorkerThreadInfo workerThreadInfo = threadIndicators.get(thread_id);
				this.threadPanel.add(workerThreadInfo.getWorkerIndicator());
			}

			this.threadPanel.revalidate();
			this.threadPanel.repaint();
		});

	}

	public void triggerClose() {
		this.window.dispose();
	}

	public void updateThreadPanel(LinkedHashMap<String, WorkerThreadInfo> threadIndicators) {
		SwingUtilities.invokeLater(() -> {
			for (String thread_id : threadIndicators.keySet()) {
				WorkerThreadInfo workerThreadInfo = threadIndicators.get(thread_id);
				JLabel indicator = workerThreadInfo.getWorkerIndicator();

				ThreadStatus status = workerThreadInfo.getThreadStatus();
				indicator.setToolTipText(workerThreadInfo.getText());

				switch (status) {
				case Working:

					indicator.setBackground(WorkerThreadInfo.runningColor);
					break;

				case Idle:

					indicator.setBackground(WorkerThreadInfo.idleColor);
					break;
				default:

					indicator.setBackground(WorkerThreadInfo.initColor);
					break;
				}

			}
		});

	}

	public void updateWorkerThreadStatus(String thread_id, ThreadStatus workingStatus, String absolutePath) {

		SwingUtilities.invokeLater(() -> {
			JLabel threadIndicator = this.threadIndicators.get(thread_id);

			switch (workingStatus) {
			case Working:
				threadIndicator.setBackground(WorkerThreadInfo.runningColor);
				break;

			case Idle:
				threadIndicator.setBackground(WorkerThreadInfo.idleColor);
				break;

			default:

				break;
			}

			threadIndicator.setToolTipText(absolutePath);
		});

	}

	public void refreshTable(List<Object[]> rowList) {
		SwingUtilities.invokeLater(() -> {
			DefaultTableModel model = (DefaultTableModel) this.resultTable.getModel();
			JTable table = this.resultTable;

			model.setRowCount(0);
			for (Object[] object : rowList) {
				model.addRow(object);
			}

			TableColumnAdjuster tt = new TableColumnAdjuster(table);

			tt.adjustColumns();
		});

	}

}
