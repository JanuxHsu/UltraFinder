package com.UltraFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import model.ScanResult;
import model.WorkerRunnable;

public class FileContentScanner extends WorkerRunnable {

	File myScannFile = null;
	ConcurrentHashMap<String, ArrayList<ScanResult>> resultPool = null;
	KeyWordHandler keyWordHandler = null;
	UltraFinder ultraFinder;

	public FileContentScanner(File myScannfile, UltraFinder ultraFinder) {
		super(ultraFinder);
		this.myScannFile = myScannfile;
		this.resultPool = ultraFinder.foundResult;
		this.keyWordHandler = ultraFinder.keyWordHandler;
		this.ultraFinder = super.ultraFinder;

	}

	@Override
	public void runJob() {

		if (myScannFile.exists() && myScannFile.isFile() && myScannFile.canRead()) {
			super.updateWokerInfoText(myScannFile.getPath());

			if (this.keyWordHandler.keywordPatterns.size() == 0) {
				return;
			}

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
					super.demoDelay(1);

				}

				if (this.ultraFinder != null) {
					this.ultraFinder.updateSearchResult(this.myScannFile.getAbsolutePath());
				}

			} catch (IOException e) {

				e.printStackTrace();
			}

		}
		super.updateWokerInfoText("");

	}

}
