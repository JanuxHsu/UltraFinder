package com.gadgets.UltraFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import Model.ScanResult;

public class FileContentScanner implements Runnable {

	File myScannFile = null;
	ConcurrentHashMap<String, ArrayList<ScanResult>> resultPool = null;
	KeyWordHandler keyWordHandler = null;

	public FileContentScanner(File myScannfile, KeyWordHandler keyWordHandler,
			ConcurrentHashMap<String, ArrayList<ScanResult>> foundResult) {
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
