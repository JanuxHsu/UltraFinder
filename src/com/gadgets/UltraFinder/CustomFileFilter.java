package com.gadgets.UltraFinder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

public class CustomFileFilter {

	Set<String> targetFileName = new HashSet<>();

	public CustomFileFilter(Set<String> targetExtensions) {
		this.targetFileName = targetExtensions;
	}

//	public boolean filterExtension(File tgtFile) {
//		String tgtFileExt = FilenameUtils.getExtension(tgtFile.getName()).toLowerCase();
//		//System.out.println(tgtFileExt);
//		if (targetExtensions.contains(tgtFileExt)) {
//			return true;
//		} else {
//			return false;
//		}
//
//	}

	public boolean filterFileName(File subFile) {
		String tgtFileExt = subFile.getName().toLowerCase();
		// System.out.println(tgtFileExt);

		for (String fileName : this.targetFileName) {
			if (tgtFileExt.endsWith(fileName)) {
				return true;
			}

		}
		return false;

	}

}
