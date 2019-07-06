package com.UltraFinder;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import model.UltraFinderConfig.UltraFinderMode;

public class FileFetcher {

	final UltraFinder ultraFinder;
	final ConcurrentLinkedQueue<String> waitToCheckDirectories;
	final ConcurrentLinkedQueue<File> waitToScanFiles;
	final ConcurrentHashMap<Long, File> fileSizeMap;
	final AtomicInteger totalChecked_directories;
	final AtomicInteger totalChecked_files;

	final UltraFinderMode mode;

	public FileFetcher(UltraFinder ultraFinder) {
		this.ultraFinder = ultraFinder;
		UltraFinderRepository ultraFinderRepository = this.ultraFinder.getRepository();
		this.waitToCheckDirectories = ultraFinderRepository.waitToCheckDirectories;
		this.totalChecked_directories = ultraFinderRepository.totalChecked_directories;
		this.totalChecked_files = ultraFinderRepository.totalChecked_files;
		this.waitToScanFiles = ultraFinderRepository.waitToScanFiles;
		this.fileSizeMap = ultraFinderRepository.fileSizeMap;
		this.mode = this.ultraFinder.config.ultraFinderMode;
	}

	public Runnable newFetchJob(File dir) {

//		return new WorkerRunnable(this.ultraFinder) {
//
//			@Override
//			public void runJob() {
//
//				checkFiles(dir);
//			}
//		};

		return new FileFetchJob(this.ultraFinder, dir);

	}

	public void Start() {

		ScheduledThreadPoolExecutor backgroudWorker = this.ultraFinder.getRepository().scheduledWorker;

		ScheduledFuture<?> updater = backgroudWorker.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
//				
				ultraFinder.updateFileFetchProgress(mode);

			}
		}, 50, 100, TimeUnit.MILLISECONDS);

		ThreadPoolExecutor threadPoolExecutor = this.ultraFinder.getRepository().getThreadPool();
		for (String root_path : this.ultraFinder.config.root_paths) {
			this.waitToCheckDirectories.add(root_path);
//			File rootFile = Paths.get(root_path).toFile();
//
//			checkFiles(rootFile);

		}
		Future<?> rootJob = threadPoolExecutor.submit(newFetchJob(Paths.get(this.waitToCheckDirectories.poll()).toFile()));

		try {
			rootJob.get(60, TimeUnit.SECONDS);
			while (this.waitToCheckDirectories.size() > 0 || threadPoolExecutor.getActiveCount() != 0
					|| threadPoolExecutor.getQueue().size() > 0) {

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
		} catch (InterruptedException | ExecutionException | TimeoutException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		

		updater.cancel(true);

	}

}
