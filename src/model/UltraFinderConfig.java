package model;

import java.util.Set;

public class UltraFinderConfig {

	public static enum UltraFinderMode {
		KEYWORD, FILESIZE
	};

	public UltraFinderMode ultraFinderMode = UltraFinderMode.KEYWORD;
	public Set<String> root_paths;
	public Set<String> filter;
	public Set<String> keywords;

	public Integer thread_num;

	public boolean gui_mode = false;
	public boolean detail_mode = false;

	public boolean search_caseSensitive = false;
	public boolean content_search = false;

	public boolean demo_mode = false;

	public int top_size_count = 100;
	public Long min_check_size = new Long(1024);

	public UltraFinderConfig() {
		// TODO Auto-generated constructor stub
	}

}
