package org.b3log.solo.memsearcher;

public class SearchCommon {
 
	public final static int PAGE_SIZE = 30;
	
	/** binary search algorithm
	 * @param target char
	 * @param char array
	 * @return index 
	 */
	public static int findCharInArray(char a, char [] arr)
	{
		 int i=0;
		 int j= arr.length-1;
		 while(i<=j)
		 {
			 int t = (i+j)/2;
			 if(a==arr[t])
			 {
				 return t;
			 }else if(a> arr[t])
			 {
				 i = t+1;
			 }
			 else
			 {
				 j = t-1;
			 }
		 }
		 return -1;
	}
	
	
}
