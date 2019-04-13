package com.server;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.UltraFinder.UltraFinder;
import com.google.common.util.concurrent.MoreExecutors;

import model.UltraFinderConfig;

public class UltraFinderController {

	static UltraFinderController instance = null;

	ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 3, 5000, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>());

	static char seperator = File.separatorChar;

	ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	String spliter = "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";

	public UltraFinder finder = null;

	static String status = null;

	private UltraFinderController() {

	}

	public static UltraFinderController getInstance() {
		if (instance == null) {
			synchronized (UltraFinderController.class) {
				if (instance == null) {
					instance = new UltraFinderController();
				}
			}
		}
		return instance;
	}

	public Future<?> submitJob(UltraFinderConfig config) {

		config.filter = config.filter.stream().map(item -> item.toLowerCase()).collect(Collectors.toSet());

		// insert Desktop path for dev
		Set<String> fixed_rootPaths = new HashSet<>();
		for (String root_path : config.root_paths) {

			String fixed_rootPath = root_path.equals("Desktop")
					? System.getProperty("user.home") + seperator + "Desktop"
					: root_path;
			fixed_rootPaths.add(fixed_rootPath);

		}

		config.root_paths = fixed_rootPaths;

		UltraFinder ultraFinder = new UltraFinder(config);

		this.finder = ultraFinder;
		Future<?> job = MoreExecutors.listeningDecorator(this.threadPool).submit(new Runnable() {

			@Override
			public void run() {
				try {
					ultraFinder.start();
					ultraFinder.shutdownGracefully();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		return job;
	}

	public int getActiveCount() {

		return threadPool.getActiveCount();
	}

}
