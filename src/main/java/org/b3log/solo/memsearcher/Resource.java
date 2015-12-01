package org.b3log.solo.memsearcher;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.repackaged.org.apache.http.annotation.NotThreadSafe;

/**
 * Resource contains the object the user interested in.<br>
 * It mainly has three parts:<br>
 * (1) Candidate object: it usually is a ID.<br>
 * (2) Content conditions: title, content's brief. <br>
 * (3) Word conditions: key words.<br>
 * <b>Note:</b> The content conditions will be matched by char,
 * and word conditions will be matched by word. If you wish some
 * input words be matched continuously, make sure that you added them 
 * into the word list by using <b>  addWords(String...) </b>.
 * @author Tommy.Tang
 */
@NotThreadSafe
public class Resource<T> {

	private T candidate;
	
	/**
	 * Bried Content
	 */
	private String content;
	
	/**
	 * Key words
	 */
	private final List<String> words = new ArrayList<String>();

	/**
	 * use for binary search.
	 */
	private char [] charPool;
	
	public T getCandidate() {
		return candidate;
	}

	public void setCandidate(T candidate) {
		this.candidate = candidate;
	}

	public String getContent() {
		return content;
	}

	public void addContent(final String ... contents) {
		this.content = addStrings(contents);
	}

	public List<String> getWords() {
		return words;
	}

	public void addWords(final String ... words) {
		for(String item: words)
			this.words.add(item);
	}
	 
	
	public char[] charPool() {
		return charPool;
	}

	public void setCharPool(final char[] charPool) {
		this.charPool = charPool;
	}

	private static String addStrings(final String ... strs)
	{ 
		StringBuilder sb = new StringBuilder();
		for(String item : strs)
			sb.append(item);
		return sb.toString();
	}
	
 
}
