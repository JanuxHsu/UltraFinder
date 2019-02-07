package com.gadgets.UltraFinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

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

	public void start() {
		File starting_file = new File(config.root_path);

		FileFinder fileFinder = new FileFinder(this, starting_file);

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

		Integer totalWork = waitToScanFiles.size();

		while (waitToScanFiles.size() > 0) {
			FileContentScanner fileContentScanner = new FileContentScanner(waitToScanFiles.poll());
			try {
				fileContentScanner.call();

				System.out.println(totalWork - waitToScanFiles.size() + " / " + totalWork);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// break;

		}
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
		ultraFinder.start();
	}

}
