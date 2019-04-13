package com.UltraFinder;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import model.UltraFinderConfig;
import model.WorkerThreadInfo;

public class UltraFinderRepository {
	ConcurrentLinkedQueue<String> waitToCheckDirectories = new ConcurrentLinkedQueue<>();
	public ConcurrentLinkedQueue<File> waitToScanFiles = new ConcurrentLinkedQueue<>();

	AtomicInteger totalChecked_directories = new AtomicInteger(0);
	AtomicInteger totalChecked_files = new AtomicInteger(0);

	LinkedHashMap<String, WorkerThreadInfo> threadIndicators = new LinkedHashMap<>();

	final ThreadPoolExecutor threadPoolExecutor;

	final ScheduledThreadPoolExecutor scheduledWorker = new ScheduledThreadPoolExecutor(5);

	public UltraFinderRepository(UltraFinderConfig config) {
		this.threadPoolExecutor = new ThreadPoolExecutor(config.thread_num, config.thread_num, 3, TimeUnit.HOURS,
				new LinkedBlockingQueue<Runnable>());

	}

	public ThreadPoolExecutor getThreadPool() {
		return this.threadPoolExecutor;
	}

	public LinkedHashMap<String, WorkerThreadInfo> getThreadIndicators() {
		return this.threadIndicators;
	}

	public ScheduledThreadPoolExecutor getScheduledWorker() {
		return this.scheduledWorker;
	}
}
