package UltraFinderGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;

import com.gadgets.UltraFinder.UltraFinder;

public class UltraFinderForm {

	public static String title = "UltraFinder v1.2 (by JanuxHsu)";

	JPanel threadPanel;
	HashMap<String, JLabel> threadIndicators = new HashMap<>();

	public static enum ThreadAction {
		ThreadWorkStart, ThreadWorkEnd
	};

	JFrame window;
	final UltraFinder ultraFinder;
	JLabel totalWorkCntLabel;
	JProgressBar totalWorkBar;
	JTextArea loggingBox;

	JTable resultTable;
	DefaultTableModel tableModel;

	private Action[] textActions = { new DefaultEditorKit.CopyAction() };

	Color runningColor = new Color(32, 191, 107);
	Color initColor = new Color(249, 202, 36);

	public UltraFinderForm(UltraFinder ultraFinder) {
		this.ultraFinder = ultraFinder;

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
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
		this.totalWorkBar = new JProgressBar();
		this.totalWorkBar.setStringPainted(true);

		this.threadPanel = new JPanel();

		topPanel.add(totalWorkCntLabel, BorderLayout.NORTH);
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
		this.tableModel.addColumn("Cnt");
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

	public void updateFoundCount() {
		this.totalWorkCntLabel.setText(" Total files found: " + this.ultraFinder.waitToScanFiles.size());
	}

	public void updateTotalProgressCount() {
		this.totalWorkBar.setMaximum(this.ultraFinder.waitToScanFiles.size());
	}

	public void updateSearchProgress(Integer curr_cnt) {
		// minus thread pool check job count
		curr_cnt = curr_cnt - this.threadIndicators.size();

		this.totalWorkBar.setValue(curr_cnt);
		this.totalWorkBar
				.setString(String.format("Scanned (%s/%s) of files.", curr_cnt, this.totalWorkBar.getMaximum()));
	}

	public void appendLog(String line) {
		this.loggingBox.append(String.format(line + "%n"));
	}

	public void updateResultTable(Integer count, String filePath) {
		this.tableModel.addRow(new Object[] { this.resultTable.getRowCount() + 1, count, filePath });

		if (this.resultTable.getRowCount() % 10 == 1) {
			resizeColumnWidth(this.resultTable);
		}

	}

	public void changeTitleName(String title) {
		this.window.setTitle(title);

	}

	public String getTitleName() {
		return this.window.getTitle();
	}

	public void initWorkerThreadId(ThreadPoolExecutor executor) throws Exception {

		Integer thread_num = executor.getCorePoolSize();

		System.out.println(thread_num);

		this.threadPanel.setLayout(new GridLayout(1, thread_num, 1, 3));

		ArrayList<String> thread_ids = new ArrayList<>();

		for (int i = 0; i < thread_num; i++) {
			Future<String> thead_future = executor.submit(new Callable<String>() {

				@Override
				public String call() throws Exception {
					return Thread.currentThread().getName();
				}
			});

			thread_ids.add(thead_future.get(1, TimeUnit.SECONDS));
		}

		for (String thread_id : thread_ids) {

			JLabel theadIndicator = new JLabel(thread_id);
			theadIndicator.setOpaque(true);
			theadIndicator.setHorizontalAlignment(JLabel.CENTER);
			theadIndicator.setBackground(this.initColor);

			this.threadPanel.add(theadIndicator);
			this.threadIndicators.put(thread_id, theadIndicator);
		}
		// this.threadPanel.setPreferredSize(new Dimension(800, 100));
		this.threadPanel.revalidate();
		this.threadPanel.repaint();

		// this.window.repaint();

	}

	public void updateWorkerThreadStatus(String thread_id, ThreadAction workingStatus, String absolutePath) {
		JLabel threadIndicator = this.threadIndicators.get(thread_id);

		switch (workingStatus) {
		case ThreadWorkStart:
			threadIndicator.setBackground(this.runningColor);
			break;

		case ThreadWorkEnd:
			threadIndicator.setBackground(Color.RED);
			break;

		default:
			break;
		}

		threadIndicator.setToolTipText(absolutePath);

	}

	private void resizeColumnWidth(JTable table) {
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 40; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			if (width > 1000)
				width = 1000;
			columnModel.getColumn(column).setPreferredWidth(width);
		}
	}

}
