package com.gadgets.UltraFinder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import Model.ScanResult;
import Model.UltraFinderConfig;

public class UltraFinder {

	ConcurrentLinkedQueue<File> waitToScanFiles = new ConcurrentLinkedQueue<>();
	CustomFileFilter filenameFilter = null;
	UltraFinderConfig config = null;

	KeyWordHandler keyWordHandler = null;
	ConcurrentHashMap<String, ArrayList<ScanResult>> foundResult = new ConcurrentHashMap<>();
	// ConcurrentLinkedQueue<ScanResult> foundResult = new
	// ConcurrentLinkedQueue<>();
	static char seperator = File.separatorChar;

	public UltraFinder(UltraFinderConfig config) {
		CustomFileFilter customFileFilter = new CustomFileFilter(config.filter);
		this.filenameFilter = customFileFilter;
		this.config = config;
		this.keyWordHandler = new KeyWordHandler(this.config.keywords, this.config.search_caseSensitive);
		System.out.println("=================================================");
		System.out.println("Threads: " + this.config.thread_num);
		System.out.println("Filters:  " + String.join(", ", this.config.filter));
		System.out.println("Keywords: " + String.join(", ", this.config.keywords));
		System.out.println("IgnoreCase: " + this.config.search_caseSensitive);
		System.out.println("=================================================");

	}

	public void start() throws InterruptedException {

		System.out.println("Start fetching file paths...");
		File starting_file = new File(config.root_path);

		FileFinder fileFinder = new FileFinder(this, starting_file);

		ExecutorService executorService = Executors.newSingleThreadExecutor();

		Future<?> search_job = executorService.submit(fileFinder);

		// System.out.println(waitToScanFiles.size());
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.HOURS);

		System.out.println("Fetching completed. Total " + waitToScanFiles.size() + " files.");

		// System.out.println(waitToScanFiles.size());

		ThreadPoolExecutor executor = new ThreadPoolExecutor(this.config.thread_num, this.config.thread_num, 100000,
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		ThreadPoolMonitor monitor = new ThreadPoolMonitor(executor, 200);
		Thread monitorThread = new Thread(monitor);
		monitorThread.start();

		while (waitToScanFiles.size() > 0 || !search_job.isDone()) {
			if (waitToScanFiles.peek() != null) {

				FileContentScanner fileContentScanner = new FileContentScanner(waitToScanFiles.poll(), keyWordHandler,
						foundResult);
				try {
					// executorService.submit(fileContentScanner);
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

		while (monitorThread.isAlive()) {
			// System.out.println("111111");
		}

		System.out.println("Search ended.");
		System.out.println("Total " + foundResult.size() + " files match the keyword.");

		for (String key : foundResult.keySet()) {
			ArrayList<ScanResult> keyLineList = foundResult.get(key);

			System.out.println(key + " | " + keyLineList.size());
			int counter = 0;
//			for (ScanResult zz : keyLineList) {
//				System.out.println(zz.fileName + " | " + zz.lineNum + " | " + zz.lineContent);
//				counter++;
//				if (counter > 10) {
//					break;
//				}
//
//			}

		}

		// executorService.shutdown();
		// executorService.awaitTermination(100000, TimeUnit.SECONDS);
	}

	public static void main(String[] args) throws IOException {

		File configFile = new File("./config.json");

		Gson gson = new Gson();
		JsonReader jsonReader = new JsonReader(new FileReader(configFile));
		JsonObject configJSON = gson.fromJson(jsonReader, JsonObject.class);

		UltraFinderConfig config = gson.fromJson(configJSON, UltraFinderConfig.class);

		// clean up config filter case

		config.filter = config.filter.stream().map(item -> item.toLowerCase()).collect(Collectors.toSet());

		// insert Desktop path for dev

		config.root_path = config.root_path.equals("Desktop") ? System.getProperty("user.home") + seperator + "Desktop"
				: config.root_path;

		config.search_caseSensitive = configJSON.get("search_options").getAsJsonObject().get("case_sensitive")
				.getAsBoolean();

		UltraFinder ultraFinder = new UltraFinder(config);
		try {
			ultraFinder.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
