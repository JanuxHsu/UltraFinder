package com.gadgets.UltraFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.Callable;

import Model.ScanResult;

public class FileContentScanner implements Runnable {

	File myScannFile = null;

	public FileContentScanner(File myScannfile) {
		this.myScannFile = myScannfile;
	}

	@Override
	public void run() {

		//HashSet<ScanResult> results = new HashSet<>();
		
		//System.out.println(this.getClass().getName());

		if (myScannFile.exists() && myScannFile.isFile() && myScannFile.canRead()) {
			//BufferedReader bufferedReader;
			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.myScannFile))) {
				String line = null;
				Integer rowCnt = 0;
				while ((line = bufferedReader.readLine()) != null) {

					ScanResult scanResult = new ScanResult(rowCnt, line);
					// System.out.println(line);
					rowCnt++;
				}

				//System.out.println(this.myScannFile.getName() + " || cnt: " + rowCnt);
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
