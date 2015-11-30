package org.b3log.solo.memsearcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.solo.model.Article;
import org.b3log.solo.repository.ArticleRepository;
import org.b3log.solo.service.ArchiveDateQueryService;
import org.b3log.solo.service.TagQueryService;
import org.json.JSONObject;
import org.b3log.latke.repository.Filter;

import com.google.appengine.repackaged.org.apache.http.annotation.NotThreadSafe;

/**
 * Memory structure used to store all data for search. 
 * @author Tommy.Tang 
 */
@NotThreadSafe
public class MemStorage {

	private int lastID;

    private static final Logger LOGGER = Logger.getLogger(MemStorage.class.getName());
	
	private List<Resource> resList = new ArrayList<Resource>();

	private static final MemStorage meStorage  = new MemStorage();
	
	private MemStorage(){}
	
	public static MemStorage getInstance(){
		return meStorage;
	}
	 
	@Inject
    private ArticleRepository articleRepository;
	
	
	
	public static enum BuildType{
		WHOLE,
		INCREMENT; 
	} 
	
	/**
	 * Build the memory data used for search.
	 */
	public boolean build(BuildType buildType){ 
		
		if( buildType == BuildType.WHOLE ) 
			return buildAll();
		else if(buildType == BuildType.INCREMENT)
			return buildByIncrement();
		return false;
		
	}
	
	
	private boolean buildAll()
	{ 
		try{
			List<Resource> tempResList = new ArrayList<Resource>(); 
			int count =  (int) articleRepository.count();
			for(int i = 0 ; i < count; i += SearchCommon.PAGE_SIZE)
			{
				List<JSONObject> objs = articleRepository.get(i, SearchCommon.PAGE_SIZE);
				if(!build(tempResList, objs)) return false; 
			}
			return true;
		}
		catch(Exception e)
		{
			LOGGER.error(String.format("Build search data by all failed. Detailed message: %s", e.getMessage()));
			return false;
		}
	}
	
	private boolean buildByIncrement()
	{   
	    try{
	    	List<JSONObject> objs = articleRepository.getLatest(lastID);
	 	    return build(objs);
		}
		catch(Exception e)
		{
			LOGGER.error(String.format("Build search data by increment failed. Detailed message: %s", e.getMessage()));
			return false;
		}
	}
	
	private boolean build(List<JSONObject> articles)
	{
		return build(this.resList, articles);
	}
	private boolean build(List<Resource> resList, List<JSONObject> articles)
	{
		if(articles == null)
			return true;
		
		for(JSONObject item: articles)
		{
			 Resource<Integer> resource = cast2Resource(item);
			 resList.add(resource);
		} 
		
		return false;
	}
	
	private Resource<Integer> cast2Resource(JSONObject article)
	{
		try{
			Resource<Integer> resource = new Resource<Integer>();
			resource.setCandidate((Integer)article.get("oId"));
			resource.addContent(article.get("articleTitle").toString(), article.get("articleAbstract").toString());
			// create and sort the content char array.
			char [] tempArr = resource.getContent().toCharArray();
			Arrays.sort(tempArr);
			resource.setCharPool(tempArr);
			String [] tags = article.get("articleTags").toString().split(","); 
			resource.addWords(tags);
			return resource;
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage());
			return null;
		} 
	}
	
	
	 
}
