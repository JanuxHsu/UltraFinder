package com.tsmc.UltraFinder;

import java.io.File;
import java.util.HashSet;

import org.apache.commons.io.FilenameUtils;

public class CustomFileFilter {

	HashSet<String> targetExtensions = new HashSet<>();

	public CustomFileFilter(HashSet<String> targetExtensions) {
		this.targetExtensions = targetExtensions;
	}

	public boolean filterExtension(File tgtFile) {
		String tgtFileExt = FilenameUtils.getExtension(tgtFile.getName()).toLowerCase();
		//System.out.println(tgtFileExt);
		if (targetExtensions.contains(tgtFileExt)) {
			return true;
		} else {
			return false;
		}

	}

}
