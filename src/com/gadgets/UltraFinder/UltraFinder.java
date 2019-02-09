package com.gadgets.UltraFinder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import Model.ScanResult;
import Model.UltraFinderConfig;
import UltraFinderGUI.UltraFinderForm;

public class UltraFinder {

	UltraFinderForm gui_form = null;

	public ConcurrentLinkedQueue<File> waitToScanFiles = new ConcurrentLinkedQueue<>();
	CustomFileFilter filenameFilter = null;
	public UltraFinderConfig config = null;

	KeyWordHandler keyWordHandler = null;
	ConcurrentHashMap<String, ArrayList<ScanResult>> foundResult = new ConcurrentHashMap<>();
	// ConcurrentLinkedQueue<ScanResult> foundResult = new
	// ConcurrentLinkedQueue<>();
	static char seperator = File.separatorChar;

	String spliter = "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";

	public UltraFinder(UltraFinderConfig config) {
		CustomFileFilter customFileFilter = new CustomFileFilter(config.filter);
		this.filenameFilter = customFileFilter;
		this.config = config;
		this.keyWordHandler = new KeyWordHandler(this.config.keywords, this.config.search_caseSensitive);
		System.out.println(spliter);
		System.out.println("Threads: " + this.config.thread_num);
		System.out.println("Filters:  " + String.join(", ", this.config.filter));
		System.out.println("Keywords: " + String.join(", ", this.config.keywords));
		System.out.println("IgnoreCase: " + this.config.search_caseSensitive);
		System.out.println(spliter);
		if (this.config.gui_mode) {
			this.gui_form = new UltraFinderForm(this);

			Gson gson_pretty = new GsonBuilder().setPrettyPrinting().create();
			this.writeSysLog(gson_pretty.toJson(this.config));
		}

	}

	public void start() throws InterruptedException {
		this.writeSysLog("Start fetching file paths...");
		File starting_file = new File(config.root_path);

		FileFinder fileFinder = new FileFinder(this, starting_file);

		ExecutorService executorService = Executors.newSingleThreadExecutor();

		Future<?> search_job = executorService.submit(fileFinder);

		// System.out.println(waitToScanFiles.size());
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.HOURS);

		this.writeSysLog("Fetching completed. Total need to scan " + waitToScanFiles.size() + " files.");

		this.updateTotalFileCount();

		// System.out.println(waitToScanFiles.size());

		ThreadPoolExecutor executor = new ThreadPoolExecutor(this.config.thread_num, this.config.thread_num, 100000,
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		ThreadPoolMonitor monitor = new ThreadPoolMonitor(this, executor, 200);
		Thread monitorThread = new Thread(monitor);
		monitorThread.start();

		this.writeSysLog("Scan start...");

		while (waitToScanFiles.size() > 0 || !search_job.isDone()) {
			if (waitToScanFiles.peek() != null) {

				FileContentScanner fileContentScanner = new FileContentScanner(waitToScanFiles.poll(), this);
				try {

					executor.submit(fileContentScanner);

				} catch (Exception e) {
					e.printStackTrace();
					this.writeSysLog(e.getMessage());

				}
			}

		}
		executor.shutdown();

		while (monitorThread.isAlive()) {

		}

		this.writeSysLog("Scan ended.");

		summaryResult();

	}

	public void updateTotalFileCount() {

		if (this.gui_form != null) {

			this.gui_form.updateTotalProgressCount();
		}

	}

	public void updateSearchResult(String resultkey) {

		if (this.foundResult.containsKey(resultkey)) {

			this.gui_form.updateResultTable(this.foundResult.get(resultkey).size(), resultkey);
		}

	}

	public void updateThreadStatus(Integer runningThreadCnt) {
		this.gui_form.updateThreadLight(runningThreadCnt);
	}

	public void summaryResult() {
		this.writeSysLog("Total " + foundResult.size() + " files match the keyword.");

		String resultTxtPath = System.getProperty("user.dir") + UltraFinder.seperator + "Result.txt";
		File resultTxtFile = new File(resultTxtPath);
		String message = "";
		try {
			message = String.format("Total " + foundResult.keySet().size() + " files match the keyword.%n");
			FileUtils.writeStringToFile(resultTxtFile, message, "UTF-8", false);

			for (String key : foundResult.keySet()) {
				FileUtils.writeStringToFile(resultTxtFile, String.format(this.spliter + "%n"), "UTF-8", true);

				ArrayList<ScanResult> keyLineList = foundResult.get(key);

				message = String.format("Count:(%s) %s%n", keyLineList.size(), key);

				// this.writeSysLog(message);
				FileUtils.writeStringToFile(resultTxtFile, message, "UTF-8", true);

				if (this.config.detail_mode) {
					for (ScanResult result : keyLineList) {

						message = String.format("Line:[%s] %s%n", result.lineNum, result.lineContent);

						// this.writeSysLog(message);
						FileUtils.writeStringToFile(resultTxtFile, message, "UTF-8", true);

					}

				}

			}

		} catch (IOException e) {

			e.printStackTrace();
		}

		this.writeSysLog("Reult file save to : " + resultTxtFile.getAbsolutePath());
	}

	public void writeSysLog(String logText) {

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

		logText = String.format("[%s] %s", timeStamp, logText.trim());

		System.out.println(logText);
		if (this.gui_form != null) {
			this.gui_form.appendLog(logText);
		}

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
