package com.UltraFinder;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import model.WorkerRunnable;
import model.UltraFinderConfig.UltraFinderMode;

public class FileFetchJob extends WorkerRunnable {
	final UltraFinder ultraFinder;
	final ConcurrentLinkedQueue<String> waitToCheckDirectories;
	final ConcurrentLinkedQueue<File> waitToScanFiles;
	final ConcurrentHashMap<Long, File> fileSizeMap;
	final AtomicInteger totalChecked_directories;
	final AtomicInteger totalChecked_files;

	final UltraFinderMode mode;

	final File dir;

	public FileFetchJob(UltraFinder ultraFinder, File dir) {
		super(ultraFinder);
		this.ultraFinder = ultraFinder;
		UltraFinderRepository ultraFinderRepository = this.ultraFinder.getRepository();
		this.waitToCheckDirectories = ultraFinderRepository.waitToCheckDirectories;
		this.totalChecked_directories = ultraFinderRepository.totalChecked_directories;
		this.totalChecked_files = ultraFinderRepository.totalChecked_files;
		this.waitToScanFiles = ultraFinderRepository.waitToScanFiles;
		this.fileSizeMap = ultraFinderRepository.fileSizeMap;
		this.mode = this.ultraFinder.config.ultraFinderMode;
		this.dir = dir;
	}

	public void checkFiles(File directory) {
		this.totalChecked_directories.incrementAndGet();
		for (File subFile : directory.listFiles()) {

			if (subFile.exists() && !subFile.isHidden()) {
				if (subFile.isDirectory()) {

					this.waitToCheckDirectories.add(subFile.getAbsolutePath());

				} else {

					if (subFile.exists() && subFile.isFile() && !subFile.isHidden()) {

						if (this.ultraFinder.filenameFilter.filterFileName(subFile)) {

							switch (this.mode) {
							case KEYWORD:
								this.waitToScanFiles.add(subFile);
								break;

							case FILESIZE:

								Long fileSize = subFile.length();
								synchronized (this.fileSizeMap) {
									if (fileSize > this.ultraFinder.config.min_check_size) {
										if (this.fileSizeMap.size() < this.ultraFinder.config.top_size_count) {

											this.fileSizeMap.put(fileSize, subFile);

										} else {
											Set<Long> fileSizeSet = this.fileSizeMap.keySet();
											Long minSize = Collections.min(fileSizeSet);

											if (fileSize > minSize) {
												this.fileSizeMap.remove(minSize);
												this.fileSizeMap.put(fileSize, subFile);
											}

										}
									}
								}

								break;

							default:
								break;
							}
						}

					}
					this.totalChecked_files.incrementAndGet();

				}
			}

		}
	}

	@Override
	public void runJob() {
		this.updateWokerInfoText(dir.getPath());

		try {
			checkFiles(dir);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.updateWokerInfoText("");

	}

}
