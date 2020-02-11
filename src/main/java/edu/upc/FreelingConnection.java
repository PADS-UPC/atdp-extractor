package edu.upc;

import edu.upc.nlp4bpm_commons.Cache;
import edu.upc.nlp4bpm_commons.FreelingAPI;
import edu.upc.utils.FilesUrl;

public class FreelingConnection {

	private String cacheUrl = FilesUrl.FREELING_CACHE_FILE.toString();

	public String getJsonString(String text) {
		Cache.initialize(cacheUrl);
		// FreelingAPI.setMode("local");
		String jsonOut = FreelingAPI.analyzeCached(text, "en", "json", "semgraph");
		Cache.saveCache();
		return jsonOut;
	}

}
