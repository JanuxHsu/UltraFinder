package com.gadgets.UltraFinder;

import java.io.File;

public class FileFinder implements Runnable {

	UltraFinder caller = null;
	File root = null;

	Integer totalFileCount = 0;
	Integer totalFolderCount = 0;

	public FileFinder(UltraFinder ultraFinder, File startingPath) {
		this.caller = ultraFinder;
		this.root = startingPath;
	}

	protected void fileFetcher(File currentPath) {

		if (!currentPath.isHidden()) {
			if (currentPath.isDirectory()) {
				this.totalFolderCount++;
				for (File subFile : currentPath.listFiles()) {

					fileFetcher(subFile);

				}

			} else if (currentPath.isFile() && !currentPath.isDirectory() && currentPath.exists()) {

				if (caller.filenameFilter.filterFileName(currentPath)) {
					caller.waitToScanFiles.add(currentPath);
				}
				this.totalFileCount++;
			}

		}
		this.caller.updateCurrentSearchingStatus(this.totalFolderCount, this.totalFileCount);

	}

	@Override
	public void run() {
		fileFetcher(root);
	}

}
