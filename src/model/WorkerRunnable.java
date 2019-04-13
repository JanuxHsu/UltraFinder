package model;

import java.util.concurrent.TimeUnit;

import com.UltraFinder.UltraFinder;

import model.WorkerThreadInfo.ThreadStatus;

public abstract class WorkerRunnable implements Runnable {
	public final UltraFinder ultraFinder;

	WorkerThreadInfo workerThreadInfo;

	public WorkerRunnable(UltraFinder ultraFinder) {
		this.ultraFinder = ultraFinder;

	}

	@Override
	public void run() {
		this.workerThreadInfo = this.ultraFinder.getRepository().getThreadIndicators()
				.get(Thread.currentThread().getName());

		workerStart();
		runJob();
		workerEnd();
		demoDelay(100);

	}

	public void updateWokerInfoText(String text) {
		this.workerThreadInfo.setText(text);
	}

	public void workerStart() {
		this.workerThreadInfo.setThreadStatus(ThreadStatus.Working);
	}

	public void workerEnd() {
		this.workerThreadInfo.setThreadStatus(ThreadStatus.Idle);
	}

	public void demoDelay(Integer delay) {

		if (this.ultraFinder.getConfig().demo_mode) {
			try {
				TimeUnit.MILLISECONDS.sleep(delay);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

	}

	public abstract void runJob();

}
