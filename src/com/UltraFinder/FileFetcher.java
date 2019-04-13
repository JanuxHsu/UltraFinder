package com.UltraFinder;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import model.WorkerRunnable;

public class FileFetcher {

	final UltraFinder ultraFinder;
	final ConcurrentLinkedQueue<String> waitToCheckDirectories;
	final ConcurrentLinkedQueue<File> waitToScanFiles;

	final AtomicInteger totalChecked_directories;
	final AtomicInteger totalChecked_files;

	public FileFetcher(UltraFinder ultraFinder) {
		this.ultraFinder = ultraFinder;
		UltraFinderRepository ultraFinderRepository = this.ultraFinder.getRepository();
		this.waitToCheckDirectories = ultraFinderRepository.waitToCheckDirectories;
		this.totalChecked_directories = ultraFinderRepository.totalChecked_directories;
		this.totalChecked_files = ultraFinderRepository.totalChecked_files;
		this.waitToScanFiles = ultraFinderRepository.waitToScanFiles;
	}

	public Runnable newFetchJob(File dir) {
		return new WorkerRunnable(this.ultraFinder) {

			@Override
			public void runJob() {
				checkFiles(dir);
			}
		};

	}

	public void checkFiles(File directory) {
		this.totalChecked_directories.incrementAndGet();

		for (File subFile : directory.listFiles()) {

			if (subFile.exists() && !subFile.isHidden()) {
				if (subFile.isDirectory()) {

					this.waitToCheckDirectories.add(subFile.getAbsolutePath());

				} else {

					if (subFile.exists() && subFile.isFile() && !subFile.isDirectory()) {

						if (this.ultraFinder.filenameFilter.filterFileName(subFile)) {
							this.waitToScanFiles.add(subFile);
						}
					}
					this.totalChecked_files.incrementAndGet();

				}
			}

		}
	}

	public void Start() {

		ScheduledThreadPoolExecutor backgroudWorker = this.ultraFinder.getRepository().scheduledWorker;

		ScheduledFuture<?> updater = backgroudWorker.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				ultraFinder.updateFileFetchProgress();

			}
		}, 100, 50, TimeUnit.MILLISECONDS);

		ThreadPoolExecutor threadPoolExecutor = this.ultraFinder.getRepository().getThreadPool();
		for (String root_path : this.ultraFinder.config.root_paths) {
			File rootFile = Paths.get(root_path).toFile();

			checkFiles(rootFile);

			threadPoolExecutor.execute(newFetchJob(Paths.get(this.waitToCheckDirectories.poll()).toFile()));

		}
		while (this.waitToCheckDirectories.size() > 0 || threadPoolExecutor.getActiveCount() != 0) {

			while (this.waitToCheckDirectories.size() > 0) {

				if (!this.waitToCheckDirectories.isEmpty()) {

					threadPoolExecutor.execute(newFetchJob(Paths.get(this.waitToCheckDirectories.poll()).toFile()));

				}

			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		updater.cancel(true);

	}

}
