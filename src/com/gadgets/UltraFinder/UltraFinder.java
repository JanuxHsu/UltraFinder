package com.gadgets.UltraFinder;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UltraFinder {

	ConcurrentLinkedQueue<File> waitToScanFiles = new ConcurrentLinkedQueue<>();
	CustomFileFilter filenameFilter = null;
	static char seperator = File.separatorChar;

	public UltraFinder(File StartingPath, CustomFileFilter filenameFilter){

		this.filenameFilter = filenameFilter;
		FileFinder fileFinder = new FileFinder(this, StartingPath);

		ExecutorService executorService = Executors.newFixedThreadPool(5);

		executorService.submit(fileFinder);

		// System.out.println(waitToScanFiles.size());
		try {
			executorService.shutdown();
			executorService.awaitTermination(100000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(waitToScanFiles.size());
	}

	public static void main(String[] args) {

		String rootPath = System.getProperty("user.home") + seperator + "Desktop";

		File root = new File(rootPath);

		HashSet<String> targetExts = new HashSet<>();
		// targetExts.add("txt");
		targetExts.add("bat");

		CustomFileFilter customFileFilter = new CustomFileFilter(targetExts);

		UltraFinder ultraFinder = new UltraFinder(root, customFileFilter);

	}

}
