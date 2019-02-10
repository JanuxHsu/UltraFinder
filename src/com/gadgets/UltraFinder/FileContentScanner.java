package com.gadgets.UltraFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import Model.ScanResult;
import UltraFinderGUI.UltraFinderForm;

public class FileContentScanner implements Runnable {

	File myScannFile = null;
	ConcurrentHashMap<String, ArrayList<ScanResult>> resultPool = null;
	KeyWordHandler keyWordHandler = null;
	UltraFinder ultraFinder;

	public FileContentScanner(File myScannfile, KeyWordHandler keyWordHandler,
			ConcurrentHashMap<String, ArrayList<ScanResult>> foundResult) {
		this.myScannFile = myScannfile;
		this.resultPool = foundResult;
		this.keyWordHandler = keyWordHandler;
	}

	public FileContentScanner(File myScannfile, UltraFinder ultraFinder) {
		this.myScannFile = myScannfile;
		this.resultPool = ultraFinder.foundResult;
		this.keyWordHandler = ultraFinder.keyWordHandler;
		this.ultraFinder = ultraFinder;

	}

	@Override
	public void run() {

		this.ultraFinder.updateWorkerStatus(Thread.currentThread().getName(),
				UltraFinderForm.ThreadAction.ThreadWorkStart, myScannFile.getAbsolutePath());

		if (myScannFile.exists() && myScannFile.isFile() && myScannFile.canRead()) {
			// BufferedReader bufferedReader;
			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.myScannFile))) {
				String line = null;
				Integer rowCnt = 0;
				while ((line = bufferedReader.readLine()) != null) {
					if (this.keyWordHandler.checkKeyWordInLine(line)) {
						ScanResult scanResult = new ScanResult(myScannFile.getName(), myScannFile.getAbsolutePath(),
								rowCnt, line);
						// System.out.println(line);
						if (this.resultPool.containsKey(myScannFile.getAbsolutePath())) {

							ArrayList<ScanResult> scanLines = this.resultPool.get(myScannFile.getAbsolutePath());
							scanLines.add(scanResult);
						} else {
							ArrayList<ScanResult> scanLines = new ArrayList<>();
							scanLines.add(scanResult);

							this.resultPool.put(myScannFile.getAbsolutePath(), scanLines);

						}

					}

					rowCnt++;

					this.demoDelay(1);
				}

				if (this.ultraFinder != null) {
					this.ultraFinder.updateSearchResult(this.myScannFile.getAbsolutePath());
				}

			} catch (IOException e) {

				e.printStackTrace();
			}

		}
		this.ultraFinder.updateWorkerStatus(Thread.currentThread().getName(),
				UltraFinderForm.ThreadAction.ThreadWorkEnd, "");

		this.demoDelay(200);

	}

	private void demoDelay(Integer delay) {

		if (this.ultraFinder.config.demo_mode) {
			try {
				TimeUnit.MILLISECONDS.sleep(delay);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

	}

}
