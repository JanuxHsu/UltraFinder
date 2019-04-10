package com.server;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.gadgets.UltraFinder.UltraFinder;
import com.google.common.util.concurrent.MoreExecutors;

import model.UltraFinderConfig;

public class UltraFinderController {

	static UltraFinderController instance = null;

	ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 3, 5000, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>());

	static char seperator = File.separatorChar;

	ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	String spliter = "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";

	private UltraFinderController() {

		ThreadPoolExecutor threadPool = this.threadPool;
		executorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (threadPool.getActiveCount() > 0) {
					System.out.println(threadPool.getActiveCount());

				}

			}
		}, 10, 200, TimeUnit.MILLISECONDS);
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

		config.root_path = config.root_path.equals("Desktop") ? System.getProperty("user.home") + seperator + "Desktop"
				: config.root_path;

		UltraFinder ultraFinder = new UltraFinder(config);

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
