package com.gadgets.UltraFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.concurrent.Callable;

import Model.ScanResult;

public class FileContentScanner implements Callable<HashSet<ScanResult>> {

	File myScannFile = null;

	public FileContentScanner(File myScannfile) {
		this.myScannFile = myScannfile;
	}

	@Override
	public HashSet<ScanResult> call() throws Exception {

		HashSet<ScanResult> results = new HashSet<>();

		if (myScannFile.exists() && myScannFile.isFile() && myScannFile.canRead()) {

			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.myScannFile));) {
				String line = null;
				Integer rowCnt = 0;
				while ((line = bufferedReader.readLine()) != null) {

					ScanResult scanResult = new ScanResult(rowCnt, line);
					// System.out.println(line);
					rowCnt++;
				}

				System.out.println(this.myScannFile.getName() + " || cnt: " + rowCnt);
			}

		}

		return results;
	}

}
