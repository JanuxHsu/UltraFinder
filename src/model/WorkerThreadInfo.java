package model;

import java.awt.Color;

import javax.swing.JLabel;

public class WorkerThreadInfo {
	public static enum ThreadStatus {
		Working, Idle, Init
	};

	final String threadId;
	final JLabel workerIndicator;
	String workerIndicatorText = "";
	public static Color runningColor = new Color(32, 191, 107);
	public static Color idleColor = new Color(249, 202, 36);
	public static Color initColor = new Color(149, 175, 192);

	ThreadStatus threadStatus = ThreadStatus.Init;

	public WorkerThreadInfo(String threadId) {
		this.threadId = threadId;
		JLabel theadIndicator = new JLabel(threadId);
		theadIndicator.setOpaque(true);
		theadIndicator.setHorizontalAlignment(JLabel.CENTER);
		theadIndicator.setBackground(initColor);

		this.workerIndicator = theadIndicator;
	}

	public void setThreadStatus(ThreadStatus status) {
		this.threadStatus = status;
	}

	public ThreadStatus getThreadStatus() {
		return this.threadStatus;
	}

	public void setText(String text) {
		this.workerIndicatorText = text;
	}

	public String getText() {
		return this.workerIndicatorText;
	}

	public JLabel getWorkerIndicator() {
		return this.workerIndicator;
	}

}
