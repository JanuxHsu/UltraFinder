package UltraFinderGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;

import com.gadgets.UltraFinder.UltraFinder;

public class UltraFinderForm {
	
	public static String title = "UltraFinder v1.2 (by JanuxHsu)";
	
	JFrame window;
	final UltraFinder ultraFinder;
	JLabel totalWorkCntLabel;
	JProgressBar totalWorkBar;
	JTextArea loggingBox;

	private Action[] textActions = { new DefaultEditorKit.CopyAction() };

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

		window.setPreferredSize(new Dimension(600, 300));

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());

		this.totalWorkCntLabel = new JLabel(" Total files found: 0");
		this.totalWorkBar = new JProgressBar();
		this.totalWorkBar.setStringPainted(true);

		topPanel.add(totalWorkCntLabel, BorderLayout.NORTH);
		topPanel.add(totalWorkBar, BorderLayout.CENTER);

		JPanel centrerPanel = new JPanel();
		centrerPanel.setLayout(new BorderLayout());

		this.loggingBox = new JTextArea();

		JMenu menu = new JMenu("Edit");
		for (Action textAction : textActions) {
			menu.add(new JMenuItem(textAction));
		}

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu);

		window.setJMenuBar(menuBar);

		DefaultCaret caret = (DefaultCaret) this.loggingBox.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		this.loggingBox.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(this.loggingBox);

		centrerPanel.add(scrollPane);
		window.add(topPanel, BorderLayout.NORTH);
		window.add(centrerPanel, BorderLayout.CENTER);

		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);

	}

	public void updateFoundCount() {
		this.totalWorkCntLabel.setText(" Total files found: " + this.ultraFinder.waitToScanFiles.size());
	}

	public void updateTotalProgressCount() {
		this.totalWorkBar.setMaximum(this.ultraFinder.waitToScanFiles.size());
	}

	public void updateSearchProgress(Long currentCount) {

		this.totalWorkBar.setValue(currentCount.intValue());
		this.totalWorkBar.setString(
				String.format("Scanned (%s/%s) of files.", currentCount.intValue(), this.totalWorkBar.getMaximum()));
	}

	public void appendLog(String txt) {
		this.loggingBox.append(String.format(txt + "%n"));
	}

	public void changeTitleName(String title) {
		this.window.setTitle(title);

	}

	public String getTitleName() {
		return this.window.getTitle();
	}

}
