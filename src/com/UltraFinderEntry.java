package com;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.UltraFinder.UltraFinder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import model.UltraFinderConfig;
import model.UltraFinderConfig.UltraFinderMode;

public class UltraFinderEntry {
	public static void main(String[] args) throws IOException {
		char seperator = UltraFinder.seperator;

		File configFile = new File("config.json");

		Gson gson = new Gson();
		JsonReader jsonReader = new JsonReader(new FileReader(configFile));
		JsonObject configJSON = gson.fromJson(jsonReader, JsonObject.class);

		UltraFinderConfig config = gson.fromJson(configJSON, UltraFinderConfig.class);

		config.ultraFinderMode = UltraFinderMode.valueOf(configJSON.get("ultraFinderMode").getAsString().toUpperCase());

		// clean up config filter case
		config.filter = config.filter.stream().map(item -> item.toLowerCase()).collect(Collectors.toSet());

		// insert Desktop path for dev
		Set<String> fixed_rootPaths = new HashSet<>();
		for (String root_path : config.root_paths) {

			String fixed_rootPath = root_path.equals("Desktop")
					? System.getProperty("user.home") + seperator + "Desktop"
					: root_path;
			fixed_rootPaths.add(fixed_rootPath);

		}

		config.root_paths = fixed_rootPaths;

		config.search_caseSensitive = configJSON.get("search_options").getAsJsonObject().get("case_sensitive")
				.getAsBoolean();

		config.content_search = configJSON.get("search_options").getAsJsonObject().get("content_search").getAsBoolean();
		config.top_size_count = configJSON.get("search_options").getAsJsonObject().get("top_size_count").getAsInt();
		config.min_check_size = configJSON.get("search_options").getAsJsonObject().get("min_check_size").getAsLong();
		UltraFinder ultraFinder = new UltraFinder(config);
		try {
			ultraFinder.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
