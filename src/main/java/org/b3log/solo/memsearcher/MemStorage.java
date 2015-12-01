package org.b3log.solo.memsearcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.solo.model.Article;
import org.b3log.solo.repository.ArticleRepository;
import org.b3log.solo.repository.impl.ArticleRepositoryImpl;
import org.b3log.solo.service.ArchiveDateQueryService;
import org.b3log.solo.service.TagQueryService;
import org.json.JSONObject;
import org.b3log.latke.repository.Filter;

import com.google.appengine.repackaged.org.apache.http.annotation.NotThreadSafe;

/**
 * Memory structure used to store all data for search. 
 * @author Tommy.Tang 
 * @Thread-Safe
 */
public class MemStorage {

	private int lastID;

    private static final Logger LOGGER = Logger.getLogger(MemStorage.class.getName());
	
	private List<Resource<Long>> resList = new ArrayList<Resource<Long>>();

	private static final MemStorage meStorage  = new MemStorage();
	
	private MemStorage(){}
	
	private Object lock = new Object();
	
	private volatile boolean building = false;
	
	public static MemStorage getInstance(){
		return meStorage;
	}
	 
    private ArticleRepository articleRepository; 
	
	public static enum BuildType{
		WHOLE,
		INCREMENT; 
	} 
	
	/**
	 * Task of refreshing mem-article-data used for searcher.
	 * @author Tommy.Tang
	 */
	public class MemRefreshTask extends TimerTask {
		  
		private MemStorage memStorage = getInstance(); 

		private BuildType buildType;
		 
		public MemRefreshTask(BuildType buildType) {
			super();
			this.buildType = buildType;
		}  
		
		@Override
		public void run() { 
			memStorage.build(buildType); 
		}  
	}
	
	/**
	 * Used to load data used for searcher.<br>
	 * <b>Detailed: </b>Start two cyclic tasks for update and re-construct operations respectfully. 
	 */
	public void startRefreshCyclicTask()
	{ 
		 // get articleRepository instance 
		 articleRepository = Lifecycle.getBeanManager().getReference(ArticleRepositoryImpl.class);
		 
		 Timer timer = new Timer();
		 // Interval of update operation : 2min. Start after 2 min.
		 timer.schedule(new MemRefreshTask(BuildType.INCREMENT), 2*60*1000 ,2*60*1000);
		 // Interval of construct operation : 60min. Start immediately.
		 timer.schedule(new MemRefreshTask(BuildType.WHOLE), 0, 60*60*1000); 
	}
	
	/**
	 * Build the memory data used for search.
	 */
	public boolean build(BuildType buildType){ 
		LOGGER.info("Article-index memory refresh command recevied! Command Type :" + buildType.toString());
		if(!building)
		{
			synchronized(lock)
			{
				if(!building)
				{
					building = true;
					try{
						boolean result = false;
						if( buildType == BuildType.WHOLE ) 
							result = buildAll();
						else if(buildType == BuildType.INCREMENT)
							result = buildByIncrement();  
						if(result)
							LOGGER.info("Article-index memory refresh command executed successfully! Command Type :" + buildType.toString());
						else
							LOGGER.error("Article-index memory refresh command executed failed!!! Command Type :" + buildType.toString());
						return result;
					}
					catch(Exception e)
					{
						LOGGER.error(String.format("Build command received but fail to accomplish. Detailed message: %s", e.getMessage()));
						return false;
					}
					finally{
						building = !building;
					}   
				}
			}
		}
		LOGGER.info("There is already one progress doing refresh. Please try it later.");
		return false; 
	}
	
	
	private boolean buildAll()
	{ 
		try{
			List<Resource<Long>> tempResList = new ArrayList<Resource<Long>>(); 
			int count =  (int) articleRepository.count();
			for(int i = 0 ; i < count; i += SearchCommon.PAGE_SIZE)
			{
				List<JSONObject> objs = articleRepository.get(i+1, SearchCommon.PAGE_SIZE);
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
	
	private boolean build(List<Resource<Long>> resourceList, List<JSONObject> articles)
	{
		if(articles == null)
			return true;
		
		for(JSONObject item: articles)
		{
			 Resource<Long> resource = cast2Resource(item);
			 if(resource == null)
				 return false;
			 this.resList.add(resource);
		} 
		
		return true;
	}
	
	private Resource<Long> cast2Resource(JSONObject article)
	{
		try{
			Resource<Long> resource = new Resource<Long>();
			resource.setCandidate(Long.valueOf(article.get("oId").toString()));
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

	public List<Resource<Long>> getResList() {
		return resList;
	}
	
	
	
	
	 
}
