package com.gadgets.UltraFinder;

import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolMonitor implements Runnable {
	private ThreadPoolExecutor executor;

	private int milliseconds;

	private boolean run = true;

	final UltraFinder ultraFinder;

	public ThreadPoolMonitor(UltraFinder ultraFinder, ThreadPoolExecutor executor, int delay) {
		this.executor = executor;
		this.milliseconds = delay;
		this.ultraFinder = ultraFinder;
	}

	public void shutdown() {
		this.run = false;
	}

	@Override
	public void run() {
		while (run) {

			if (this.ultraFinder.config.demo_mode) {

				System.out.println((String.format(
						"[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s\r",
						this.executor.getPoolSize(), this.executor.getCorePoolSize(), this.executor.getActiveCount(),
						this.executor.getCompletedTaskCount(), this.executor.getTaskCount(), this.executor.isShutdown(),
						this.executor.isTerminated())));

			}

			this.ultraFinder.updateSearchProgress(this.executor.getCompletedTaskCount());

			if (this.executor.getCompletedTaskCount() == this.executor.getTaskCount() && this.executor.isTerminated()) {
				this.run = false;
			}
			try {
				Thread.sleep(this.milliseconds);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// System.out.println("Monitor Stop");

	}
}