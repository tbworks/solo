package org.b3log.solo.memsearcher;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;


public class Searcher {

	/**
	 * Max query page size when loading data from database.
	 */
	public static int PAGE_SIZE = 30;
	
	/**
	 * Max size of result showed on the result list page.
	 */
	public static int MAX_RESULT_SIZE = 30;
	
	private static final Searcher searcher  = new Searcher();
	
	private MemStorage memdb = MemStorage.getInstance();
	
	private Searcher(){}
	
	public static Searcher getInstance(){
		return searcher;
	}
	
	public List<JSONObject> search(String condition)
	{
		List<JSONObject> result = new ArrayList<JSONObject>();
		
		List<JSONObject> result_find_by_keywords = searchByKeywords(condition, MAX_RESULT_SIZE);
		
		int left_size = MAX_RESULT_SIZE - ( result_find_by_keywords == null ? 0 : result_find_by_keywords.size() );
		
		List<JSONObject> result_find_by_content = searchByContent(condition, left_size);
		
		result.addAll(result_find_by_keywords);
		result.addAll(result_find_by_content); 
		return result; 
	}

	private List<JSONObject> searchByKeywords(String condition, int max_size)
	{
		memdb.
		for()
		return null;
	}
	
	private List<JSONObject> searchByContent(String condition, int max_size)
	{
		return null;
	}
}
