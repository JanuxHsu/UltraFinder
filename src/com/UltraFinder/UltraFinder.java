package com.UltraFinder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gui.UltraFinderForm;

import model.ScanResult;
import model.UltraFinderConfig;
import model.UltraFinderConfig.UltraFinderMode;
import model.WorkerThreadInfo;

public class UltraFinder {
	public static char seperator = File.separatorChar;
	Gson gson_pretty = new GsonBuilder().setPrettyPrinting().create();
	JsonParser jsonbParser = new JsonParser();

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
		this.gui_form = this.config.gui_mode ? new UltraFinderForm(this) : null;

		this.repository = new UltraFinderRepository(this.config);

		this.writeSysLog(
				String.format("Using config :%n%s", StringEscapeUtils.unescapeJava(gson_pretty.toJson(this.config))));

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

		switch (this.config.ultraFinderMode) {
		case KEYWORD:
			this.updateTotalFileCount();
			this.writeSysLog("Fetching completed. Total need to scan " + repository.waitToScanFiles.size() + " files.");

			// System.out.println(waitToScanFiles.size());

			this.writeSysLog("Scan start...");

			long original_jobCount = this.repository.getThreadPool().getCompletedTaskCount();

			ArrayList<Future<?>> scanJobs = new ArrayList<>();

			ScheduledFuture<?> checker = repository.getScheduledWorker().scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					gui_form.updateSearchProgress(
							new Long(repository.getThreadPool().getCompletedTaskCount() - original_jobCount)
									.intValue());
				}
			}, 0, 500, TimeUnit.MILLISECONDS);

			while (repository.waitToScanFiles.size() > 0) {

				if (repository.waitToScanFiles.peek() != null) {

					File to_scan_file = repository.waitToScanFiles.poll();
					FileContentScanner fileContentScanner = new FileContentScanner(to_scan_file, this);

					Future<?> scanJob = threadPool.submit(fileContentScanner);
					scanJobs.add(scanJob);
				}

			}

			while (threadPool.getActiveCount() > 0) {

				// this.gui_form.updateSearchProgress(scanJobs.size());
				Thread.sleep(50);
			}

			checker.cancel(true);
			int doneJobs = scanJobs.stream().mapToInt(item -> item.isDone() ? 1 : 0).sum();
			this.gui_form.updateSearchProgress(doneJobs);

			this.writeSysLog("Scan ended.");

			summaryKeyWordResult();
			break;

		case FILESIZE:
			threadPool.shutdown();

			this.writeSysLog("Scan ended.");

			summaryFileSizeResult();
			break;

		default:
			this.writeSysLog("Scan ended.");
			this.writeSysLog("unknown mode type.");
			break;
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

	public void summaryKeyWordResult() {

		this.writeSysLog("Total " + foundResult.size() + " files match the keyword.");

		String resultTxtPath = System.getProperty("user.dir") + UltraFinder.seperator + "Result.txt";
		File resultTxtFile = new File(resultTxtPath);
		String message = "";
		try {

			message = String.format("Total scanned %s directories, %s files, Total %s files match the keyword.%n",
					this.repository.totalChecked_directories, this.repository.totalChecked_files,
					foundResult.keySet().size());
			this.writeSysLog(message);

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

	public void summaryFileSizeResult() {

		this.writeSysLog(String.format("Total scanned %s directories, %s files, listing top %s largest files.",
				this.repository.totalChecked_directories, this.repository.totalChecked_files,
				this.config.top_size_count));
		String resultTxtPath = System.getProperty("user.dir") + UltraFinder.seperator + "Result.txt";
		File resultTxtFile = new File(resultTxtPath);

		try {

			JsonArray resArray = new JsonArray();

			Map<Long, File> infoMap = this.repository.fileSizeMap;

			List<Long> list = new ArrayList<>(infoMap.keySet());

			list.sort(Collections.reverseOrder());
			Set<Long> result = new LinkedHashSet<>(list);

			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			for (Long key : result) {

				JsonObject resObj = new JsonObject();

				File file = this.repository.fileSizeMap.get(key);

				resObj.addProperty("name", file.getName());
				resObj.addProperty("raw_size", file.length());
				resObj.addProperty("size", FileUtils.byteCountToDisplaySize(key));
				resObj.addProperty("path", file.getAbsolutePath());
				resObj.addProperty("modified_date", sDateFormat.format(new Date(file.lastModified())));

				resArray.add(resObj);

			}

			FileUtils.writeStringToFile(resultTxtFile, gson_pretty.toJson(resArray), "UTF-8", false);

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

	public void updateFileFetchProgress(UltraFinderMode mode) {

		int dirCount = this.repository.totalChecked_directories.get();
		int fileCount = this.repository.totalChecked_files.get();
		int waitToScanCount = 0;
		int queuedJobCount = this.repository.getThreadPool().getQueue().size();
		switch (mode) {
		case KEYWORD:
			waitToScanCount = this.repository.waitToScanFiles.size();
			break;

		case FILESIZE:
			waitToScanCount = this.repository.fileSizeMap.size();

			this.updateFileSizeTable();
			break;

		default:
			break;
		}

		// System.out.println(waitToScanCount);
		this.gui_form.updateFileCount(dirCount, fileCount, waitToScanCount, queuedJobCount);
	}

	private void updateFileSizeTable() {
		if (this.getGui() != null) {
			try {
				List<Object[]> rowList = new ArrayList<>();

				Map<Long, File> infoMap = this.repository.fileSizeMap;

				List<Long> list = new ArrayList<>(infoMap.keySet());

				list.sort(Collections.reverseOrder());
				Set<Long> result = new LinkedHashSet<>(list);

				int index = 1;

				for (Long key : result) {
					if (infoMap.get(key) != null) {
						// System.out.println(FileUtils.byteCountToDisplaySize(key));
						ArrayList<Object> row = new ArrayList<>();
						row.add(index);
						row.add(FileUtils.byteCountToDisplaySize(key));
						row.add(infoMap.get(key).getPath());

						rowList.add(row.toArray());
						index++;
					}

				}

				this.gui_form.refreshTable(rowList);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

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

	public Map<String, Object> getProgress() {
		HashMap<String, Object> map = new HashMap<>();

		map.put("dirs", this.repository.totalChecked_directories);
		map.put("files", this.repository.totalChecked_files);
		map.put("jobs", this.repository.getThreadPool().getQueue().size());

		if (this.config.ultraFinderMode.equals(UltraFinderMode.FILESIZE)) {
			map.put("found", this.repository.fileSizeMap.size());

		} else {
			map.put("found", this.repository.waitToScanFiles.size());

		}

		return map;
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
