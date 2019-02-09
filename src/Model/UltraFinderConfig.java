package Model;

import java.util.Set;

public class UltraFinderConfig {

	public String root_path;

	public Integer thread_num;
	public Set<String> filter;
	public Set<String> keywords;

	public boolean gui_mode = false;
	public boolean detail_mode = false;

	public boolean search_caseSensitive = false;

	public UltraFinderConfig() {
		// TODO Auto-generated constructor stub
	}

}
