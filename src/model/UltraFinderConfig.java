package model;

import java.util.Set;

public class UltraFinderConfig {

	public Set<String> root_paths;
	public Set<String> filter;
	public Set<String> keywords;

	public Integer thread_num;

	public boolean gui_mode = false;
	public boolean detail_mode = false;

	public boolean search_caseSensitive = false;
	public boolean content_search = false;
	public boolean show_file_size = false;

	public boolean demo_mode = false;

	public UltraFinderConfig() {
		// TODO Auto-generated constructor stub
	}

}
