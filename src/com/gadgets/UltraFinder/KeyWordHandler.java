package com.gadgets.UltraFinder;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyWordHandler {

	Set<String> keywordPatterns;

	boolean isCaseSensitive = false;

	public KeyWordHandler(Set<String> keywords, boolean isCaseSensitive) {
		this.keywordPatterns = keywords;
		this.isCaseSensitive = isCaseSensitive;

	}

	public boolean checkKeyWordInLine(String line) {

		String scan_line = isCaseSensitive ? line : line.toLowerCase();

		for (String pattern_str : keywordPatterns) {

			String matcher_pattern = this.isCaseSensitive ? pattern_str : pattern_str.toLowerCase();

			if (scan_line.contains(matcher_pattern)) {
				return true;
			}

//			Pattern keywordPatter = Pattern.compile(matcher_pattern);
//			Matcher matcher = keywordPatter.matcher(scan_line);
//
//			if (matcher.matches()) {
//				return true;
//			}

		}
		return false;

	}
}
