package com.gadgets.UltraFinder;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileFinder implements Runnable {

	UltraFinder caller = null;
	File root = null;

	Integer totalFileCount = 0;
	Integer totalFolderCount = 0;

	public FileFinder(UltraFinder ultraFinder, File startingPath) {
		this.caller = ultraFinder;
		this.root = startingPath;
	}

	protected void fileFetcher(File currentPath) {

		if (!currentPath.isHidden()) {
			if (currentPath.isDirectory()) {
				this.totalFolderCount++;
				for (File subFile : currentPath.listFiles()) {

					fileFetcher(subFile);

				}

			} else if (currentPath.isFile() && !currentPath.isDirectory() && currentPath.exists()) {

				if (caller.filenameFilter.filterFileName(currentPath)) {
					caller.waitToScanFiles.add(currentPath);
				}
				this.totalFileCount++;
			}

		}

	}

	public void updateGui() {
		this.caller.updateCurrentSearchingStatus(this.totalFolderCount, this.totalFileCount);
	}

	@Override
	public void run() {

		class UpdateJob implements Runnable {
			final FileFinder fileFinder;

			public UpdateJob(FileFinder fileFinder) {
				this.fileFinder = fileFinder;
			}

			public void run() {
				this.fileFinder.updateGui();

			}
		}

		UpdateJob job = new UpdateJob(this);

		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(job, 0, 200, TimeUnit.MILLISECONDS);

		fileFetcher(root);
		scheduler.shutdownNow();
		try {
			scheduler.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		updateGui();

	}

}
