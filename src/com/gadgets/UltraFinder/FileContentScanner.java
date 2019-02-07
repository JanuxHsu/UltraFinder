package com.gadgets.UltraFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import Model.ScanResult;

public class FileContentScanner implements Runnable {

	File myScannFile = null;
	ConcurrentLinkedQueue<ScanResult> resultPool = null;
	KeyWordHandler keyWordHandler = null;

	public FileContentScanner(File myScannfile, KeyWordHandler keyWordHandler,
			ConcurrentLinkedQueue<ScanResult> foundResult) {
		this.myScannFile = myScannfile;
		this.resultPool = foundResult;
		this.keyWordHandler = keyWordHandler;
	}

	@Override
	public void run() {

		// HashSet<ScanResult> results = new HashSet<>();

		// System.out.println(this.getClass().getName());

		if (myScannFile.exists() && myScannFile.isFile() && myScannFile.canRead()) {
			// BufferedReader bufferedReader;
			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.myScannFile))) {
				String line = null;
				Integer rowCnt = 0;
				while ((line = bufferedReader.readLine()) != null) {
					if (this.keyWordHandler.checkKeyWordInLine(line)) {
						ScanResult scanResult = new ScanResult(rowCnt, line);
						// System.out.println(line);
						this.resultPool.add(scanResult);
					}

					rowCnt++;
				}

				// System.out.println(this.myScannFile.getName() + " || cnt: " + rowCnt);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
