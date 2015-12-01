package org.b3log.solo.memsearcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.util.CollectionUtils;
import org.json.JSONObject;


/**
 * Main class for search algorithm. Nothing special.<br>
 * Main method: <b>{@link Searcher#search(String)}</b>
 * @author Tommy.Tang
 * @since ??
 */
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
	
	public class TempResult implements Comparable<TempResult>{
		private Long result;
		private Integer weight;
		public Long getResult() {
			return result;
		}
		public void setResult(Long result) {
			this.result = result;
		}
		public Integer getWeight() {
			return weight;
		}
		public void setWeight(Integer weight) {
			this.weight = weight;
		}
		public TempResult(Long result, int weight) {
			super();
			this.result = result;
			this.weight = weight;
		} 
		
		@Override
		public int compareTo(TempResult o) {
			// TODO Auto-generated method stub
			return this.weight.compareTo(o.weight);
		} 
	}
	
	/**
	 * <b>Note</b> This search algorithm will prior match those articles
	 * with required tags, then match other information like title, brief
	 * by char.
	 * @param condition
	 * @return Articles' IDs list
	 */
	public List<Long> search(String condition)
	{ 
		List<Long> result = new ArrayList<Long>();
		
		if(condition == null || condition.isEmpty()) return result;
		
		Set<Long> result_find_by_keywords = searchByKeywords(condition, MAX_RESULT_SIZE);
		
		int left_size = MAX_RESULT_SIZE - ( result_find_by_keywords == null ? 0 : result_find_by_keywords.size() );
		
		List<Long> result_find_by_content = searchByContent(condition, left_size, result_find_by_keywords);
		
		result.addAll(result_find_by_keywords);
		result.addAll(result_find_by_content); 
		return result; 
	}

	private Set<Long> searchByKeywords(String condition, int max_size)
	{
		Set<Long> result = new HashSet<Long>();
		if( memdb.getResList() == null ) return result; 
		int left_count = max_size;
		for( Resource<Long> item : memdb.getResList() )
		{
			if(matchWithKeywords(item.getWords(), condition))
				result.add(item.getCandidate()); 
		}
		return result;
	}
	 
	private List<Long> searchByContent(String condition, int max_size, Set<Long> lastResult)
	{
		List<TempResult> result = new ArrayList<TempResult>(); 
		if( memdb.getResList() == null ) return getTrueResult(result); 
		int left_count = max_size;
		for( Resource<Long> item : memdb.getResList() )
		{
			int matched_count = countMatchedCountWithContent(item.charPool(), condition);
			if( matched_count > 0 && !lastResult.contains(item.getCandidate()) && left_count -- > 0)
				result.add( new TempResult(item.getCandidate(), matched_count));
		}
		Collections.sort(result); 
		return getTrueResult(result);
	}
	 
	private boolean matchWithKeywords(List<String> words, String condition)
	{
		if(words == null ) return false;  
		for(String word : words)
		{
			if(word.contains(condition))
				return true; 
		} 
		return false;
	}
	
	private int countMatchedCountWithContent(char [] charPool, String condition){
		if( charPool == null || charPool.length ==0 ) return 0;  
		int matched_size = 0;
		for(int i = 0; i< condition.length(); i++)
		{ 
			if(SearchCommon.findCharInArray(condition.charAt(i), charPool) >= 0)
			{
				matched_size ++;
			} 
		}
		return matched_size;
	}
	
	private List<Long> getTrueResult(List<TempResult> list)
	{
		List<Long> result = new ArrayList<Long>();
		for(TempResult item : list)
		{
			result.add(item.getResult());
		}
		return result;
	}
}
