package com.UltraFinder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class CustomFileFilter {

	Set<String> targetFileName = new HashSet<>();

	public CustomFileFilter(Set<String> targetExtensions) {
		this.targetFileName = targetExtensions;
	}

	public boolean filterFileName(File subFile) {
		String tgtFileExt = subFile.getName().toLowerCase();

		for (String fileName : this.targetFileName) {
			if (tgtFileExt.endsWith(fileName)) {
				return true;
			}

		}
		return false;

	}

}
