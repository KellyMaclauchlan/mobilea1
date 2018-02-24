package edu.carleton.comp4601.resources;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.mongodb.BasicDBObject;

import edu.uci.ics.crawler4j.url.WebURL;

public class CrawledPage extends BasicDBObject{
	String url; 
	int textLength;
	int htmlLength;
	int outGoingLinks;
	float score;
	Set<String> links;
	Set<String> linksFound;
	Set<String> images;
	Set<String> headers;
	Set<String>body;
	String text;
	String html;
	String docId;
	String _id;
	
	public CrawledPage() {
		url="";
		textLength=0;
		htmlLength=0;
		outGoingLinks=0;
		score=0;
				
	}
	
	public CrawledPage(String url, int textLength, int htmlLength, int outGoingLinks, Set<WebURL> links, String text,
			String html) {
		this.url = url;
		this.textLength = textLength;
		this.htmlLength = htmlLength;
		this.outGoingLinks = outGoingLinks;
		HashSet<String> urls =  new HashSet<String>();
		for(WebURL s : links){
			urls.add(s.getURL());
		}
		this.links = urls;
		this.text = text;
		this.html = html;
	}

	public CrawledPage(String url, int textLength, int htmlLength, int outGoingLinks) {

		this.url = url;
		this.textLength = textLength;
		this.htmlLength = htmlLength;
		this.outGoingLinks = outGoingLinks;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getTextLength() {
		return textLength;
	}
	public void setTextLength(int textLength) {
		this.textLength = textLength;
	}
	public int getHtmlLength() {
		return htmlLength;
	}
	public void setHtmlLength(int htmlLength) {
		this.htmlLength = htmlLength;
	}
	public int getOutGoingLinks() {
		return outGoingLinks;
	}
	public void setOutGoingLinks(int outGoingLinks) {
		this.outGoingLinks = outGoingLinks;
	}

	

	public Set<String> getLinks() {
		return links;
	}

	public void setLinks(Set<String> links) {
		this.links = links;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public Set<String> getLinksFound() {
		return linksFound;
	}

	public void setLinksFound(Set<String> linksFound) {
		this.linksFound = linksFound;
	}

	public Set<String> getImages() {
		return images;
	}

	public void setImages(Set<String> images) {
		this.images = images;
	}

	public Set<String> getHeaders() {
		return headers;
	}

	public void setHeaders(Set<String> headers) {
		this.headers = headers;
	}

	public Set<String> getBody() {
		return body;
	}

	public void setBody(Set<String> body) {
		this.body = body;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}
	
	public void setMongoId(String id) {
		this._id = id;
	}
	
	public String getMongoId() {
		return _id;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public float getScore() {
		return this.score;
	}
	

}
