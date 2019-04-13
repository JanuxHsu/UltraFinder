package com.UltraFinder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gui.UltraFinderForm;

import model.ScanResult;
import model.UltraFinderConfig;
import model.WorkerThreadInfo;

public class UltraFinder {
	public static char seperator = File.separatorChar;
	Gson gson_pretty = new GsonBuilder().setPrettyPrinting().create();

	ConcurrentHashMap<String, ArrayList<ScanResult>> foundResult = new ConcurrentHashMap<>();

	final UltraFinderConfig config;
	final UltraFinderForm gui_form;
	public final UltraFinderRepository repository;

	public CustomFileFilter filenameFilter = null;

	KeyWordHandler keyWordHandler = null;

	String spliter = "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";

	public UltraFinder(UltraFinderConfig config) {
		CustomFileFilter customFileFilter = new CustomFileFilter(config.filter);
		this.filenameFilter = customFileFilter;
		this.config = config;
		this.keyWordHandler = new KeyWordHandler(this.config.keywords, this.config.search_caseSensitive);
		this.repository = new UltraFinderRepository(this.config);

		this.gui_form = this.config.gui_mode ? new UltraFinderForm(this) : null;
		this.writeSysLog(String.format("Using config :%n" + gson_pretty.toJson(this.config)));

		// mapping threadKey with thread_id
		this.initWorkerStaus(this.repository.getThreadPool());

		this.repository.getScheduledWorker().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				updateThreadPoolStatus();

			}
		}, 100, 200, TimeUnit.MILLISECONDS);
	}

	public void start() throws InterruptedException {
		ThreadPoolExecutor threadPool = this.repository.getThreadPool();

		this.writeSysLog("Start fetching file paths...");

		FileFetcher fileFetcher = new FileFetcher(this);
		fileFetcher.Start();

		
		if (config.content_search) {
			this.updateTotalFileCount();
			this.writeSysLog("Fetching completed. Total need to scan " + repository.waitToScanFiles.size() + " files.");
			

			// System.out.println(waitToScanFiles.size());

			this.writeSysLog("Scan start...");

			ArrayList<Future<?>> scanJobs = new ArrayList<>();
			while (repository.waitToScanFiles.size() > 0) {

				if (repository.waitToScanFiles.peek() != null) {

					File to_scan_file = repository.waitToScanFiles.poll();
					FileContentScanner fileContentScanner = new FileContentScanner(to_scan_file, this);

					Future<?> scanJob = threadPool.submit(fileContentScanner);
					scanJobs.add(scanJob);
				}

			}

			while (threadPool.getActiveCount() > 0) {
				int doneJobs = scanJobs.stream().mapToInt(item -> item.isDone() ? 1 : 0).sum();

				this.gui_form.updateSearchProgress(doneJobs);
				Thread.sleep(50);
			}
			this.gui_form.updateSearchProgress(scanJobs.size());

			this.writeSysLog("Scan ended.");

			summaryResult();
		} else {
			threadPool.shutdown();
			this.writeSysLog("Scan ended.");
		}

	}

	public void updateTotalFileCount() {

		if (this.gui_form != null) {

			this.gui_form.updateTotalProgressCount(this.repository.waitToScanFiles.size());
		}

	}

	public void updateSearchResult(String resultkey) {

		if (this.foundResult.containsKey(resultkey)) {

			this.gui_form.updateResultTable(this.foundResult.get(resultkey).size(), resultkey);
		}

	}

	public void initWorkerStaus(ThreadPoolExecutor threadPoolExecutor) {

		Integer thread_num = threadPoolExecutor.getCorePoolSize();

		for (int i = 0; i < thread_num; i++) {
			Future<String> thead_future = threadPoolExecutor.submit(new Callable<String>() {

				@Override
				public String call() throws Exception {
					return Thread.currentThread().getName();
				}
			});

			try {
				String thread_id = thead_future.get(10, TimeUnit.SECONDS);

				WorkerThreadInfo workerThreadInfo = new WorkerThreadInfo(thread_id);
				this.repository.threadIndicators.put(thread_id, workerThreadInfo);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			}
		}

		this.gui_form.initWorkerThreadId(this.repository.threadIndicators);

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

		if (this.gui_form != null) {
			this.gui_form.appendLog(logText);
		}
		System.out.println(logText);

	}

	public void updateFileFetchProgress() {

		int dirCount = this.repository.totalChecked_directories.get();
		int fileCount = this.repository.totalChecked_files.get();
		int waitToScanCount = this.repository.waitToScanFiles.size();
		// System.out.println(waitToScanCount);
		this.gui_form.updateFileCount(dirCount, fileCount, waitToScanCount);
	}

	public void close() {
		System.exit(0);
	}

	public void shutdownGracefully() {

		this.gui_form.triggerClose();

	}

	public UltraFinderForm getGui() {
		return this.gui_form;
	}

	public String getProgress() {
		// TODO Auto-generated method stub
		return String.format("%s", this.repository.waitToScanFiles.size());
	}

	public Queue<File> getWaitToScanFiles() {

		return this.repository.waitToScanFiles;
	}

	public UltraFinderRepository getRepository() {
		return this.repository;
	}

	public UltraFinderConfig getConfig() {

		return this.config;
	}

	public void updateThreadPoolStatus() {
		this.gui_form.updateThreadPanel(this.repository.threadIndicators);

	}

}
