package com.gadgets.UltraFinder;

import java.io.File;

public class FileFinder implements Runnable {

	UltraFinder caller = null;
	File root = null;

	public FileFinder(UltraFinder ultraFinder, File startingPath) {
		this.caller = ultraFinder;
		this.root = startingPath;
	}

	protected void fileFetcher(File currentPath) {

		for (File subFile : currentPath.listFiles()) {
			if (subFile.isFile() && subFile.exists()) {
				if (caller.filenameFilter.filterExtension(subFile)) {
					caller.waitToScanFiles.add(subFile);
					System.out.println(subFile.getAbsolutePath() + " || added!");
				}

			} else {
				fileFetcher(subFile);
			}
		}
	}

	@Override
	public void run() {
		fileFetcher(root);

	}

}
