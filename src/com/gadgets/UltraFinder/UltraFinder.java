package com.gadgets.UltraFinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import Model.ScanResult;
import Model.UltraFinderConfig;

public class UltraFinder {

	ConcurrentLinkedQueue<File> waitToScanFiles = new ConcurrentLinkedQueue<>();
	CustomFileFilter filenameFilter = null;
	UltraFinderConfig config = null;
	static char seperator = File.separatorChar;

	public UltraFinder(UltraFinderConfig config) {
		CustomFileFilter customFileFilter = new CustomFileFilter(config.filter);
		this.filenameFilter = customFileFilter;
		this.config = config;

	}

	public void start() throws InterruptedException {
		File starting_file = new File(config.root_path);

		FileFinder fileFinder = new FileFinder(this, starting_file);

		ExecutorService executorService = Executors.newFixedThreadPool(5);

		Future<?> search_job = executorService.submit(fileFinder);

		// System.out.println(waitToScanFiles.size());
		executorService.shutdown();
		executorService.awaitTermination(100000, TimeUnit.SECONDS);

		executorService = Executors.newFixedThreadPool(5);
		System.out.println(waitToScanFiles.size());

		ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 100000, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		ThreadPoolMonitor monitor = new ThreadPoolMonitor(executor, 200);
		Thread monitorThread = new Thread(monitor);
		monitorThread.start();

		Integer totalWork = waitToScanFiles.size();

		while (waitToScanFiles.size() > 0 || !search_job.isDone()) {
			if (waitToScanFiles.peek() != null) {
				FileContentScanner fileContentScanner = new FileContentScanner(waitToScanFiles.poll());
				try {
//					executorService.submit(fileContentScanner);
					// jobs.add(fileContentScanner);
					executor.submit(fileContentScanner);
					// fileContentScanner.call();

					// System.out.println(totalWork - waitToScanFiles.size() + " / " + totalWork);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// break;
			}

		}
		executor.shutdown();
		// executor.awaitTermination(1000000, TimeUnit.SECONDS);

		System.out.println("waiting...");

		while (monitorThread.isAlive()) {
			// System.out.println("111111");
		}

		System.out.println("End");

		// executorService.shutdown();
		// executorService.awaitTermination(100000, TimeUnit.SECONDS);
	}

	public static void main(String[] args) throws FileNotFoundException {

		File configFile = new File("./config.json");

		Gson gson = new Gson();
		JsonReader jsonReader = new JsonReader(new FileReader(configFile));
		UltraFinderConfig config = gson.fromJson(jsonReader, UltraFinderConfig.class);

		// clean up config filter case

		config.filter = config.filter.stream().map(item -> item.toLowerCase()).collect(Collectors.toSet());

		// insert Desktop path for dev

		config.root_path = config.root_path.equals("Desktop") ? System.getProperty("user.home") + seperator + "Desktop"
				: config.root_path;

		UltraFinder ultraFinder = new UltraFinder(config);
		try {
			ultraFinder.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
