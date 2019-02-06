package com.gadgets.UltraFinder;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyWordHandler {

	HashSet<String> keywordPatterns;

	public KeyWordHandler(HashSet<String> keywordPatterns) {
		this.keywordPatterns = keywordPatterns;

	}

	public boolean checkKeyWordInLine(String line) {
		for (String pattern_str : keywordPatterns) {
			Pattern keywordPatter = Pattern.compile(pattern_str);

			Matcher matcher = keywordPatter.matcher(line);
			if (matcher.matches()) {
				return true;
			}

		}
		return false;

	}
}
