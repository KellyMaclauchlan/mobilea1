package edu.carleton.comp4601.resources;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.Documents;
import edu.carleton.comp4601.utility.SDAConstants;
import edu.carleton.comp4601.utility.SearchResult;
import edu.carleton.comp4601.utility.SearchServiceManager;
import edu.uci.ics.crawler4j.url.WebURL;


@Path("/sda")
public class SearchableDocumentArchive {
	// Allows to insert contextual objects into the class,
		// e.g. ServletContext, Request, Response, UriInfo
		@Context
		UriInfo uriInfo;
		@Context
		Request request;

		private String name;

		public SearchableDocumentArchive() {
			name = "COMP4601 Searchable Document Archive V2.1: Brittny Lapierre and Kelly Maclauchlan";
			//put in call to db to get accounts and put into the list 
			Documents.getInstance();
		}
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String sayHtml() {
			return "<html> " + "<title>" + name + "</title>" + "<body><h1>" + name
					+ "</body></h1>" + "</html> ";
		}
		
		@Path("crawl")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String crawl() {
			try {
				Controller.startCrawler();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "<html> " + "<title>" + "crawl is done" + "</title>" + "<body><h1>" + name
					+ "</body></h1>" + "</html> ";
		}
		
		//SearchServiceManager 
		/*@Path("search/{terms}")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String search(@PathParam("terms") String terms) {
			SearchResult res = SearchServiceManager.getInstance().search(terms);
			return "<html> " + "<title>" + "crawl is done" + "</title>" + "<body><h1>" + name
					+ "</h1>"+res.toString()+"</body>" + "</html> ";
		}*/
		
/*
 * TODO: fix error/make custom:
 * org.apache.lucene.index.IndexNotFoundException: no segments* file found in MMapDirectory@C:\Users\IBM_ADMIN\SDA\index lockFactory=org.apache.lucene.store.NativeFSLockFactory@4e39afb5: files: []
	at org.apache.lucene.index.SegmentInfos$FindSegmentsFile.run(SegmentInfos.java:685)
 * 
 * */	
		@GET	
		@Path("search/{tags}")	
		@Produces(MediaType.TEXT_HTML)	
		public	String	searchForDocs(@PathParam("tags") String	tags){	
			SearchResult sr	= SearchServiceManager.getInstance().query(tags);	
			//Perform your local search	(this	is	my	specific	code,	yours	differs!)	
			ArrayList<Document>	docs	=	Documents.getInstance().query(tags);	
			//We	will	wait	for	up	to	10	seconds	but	will	then	
			//			take	the	documents	that	we	have.	
			try	{	
			 	sr.await(SDAConstants.TIMEOUT,	TimeUnit.SECONDS);	
			}	catch	(Exception	e)	{	
			}	
			//Take	the	state	of	the	documents	
			docs.addAll(sr.getDocs());	
			//Build	the	page	(not	provided	here)	
			return	docs.toString();//documentsAsString(docs,	tags);	
		}
		
/* TODO: change to prof's search method
* @GET	
@Path("query/{tags}")	
@Produces(MediaType.APPLICATION_XML)	
	public	DocumentColleceon queryAsXML(@PathParam("tags")	String	tags)	{	
	DocumentCollec>on	dc	=	new	DocumentCollec>on();	
	//	Perform	your	local	search	(this	is	my	specific	code,	yours	differs!)
	dc.setDocuments(Documents.getInstance().query(tags));	
	//	Return	the	XML	version	of	the	DocumentColleceon
	return	dc;	
}*/

		@Path("query/{query}")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String query(@PathParam("query") String query) {
			String content = "<h1>"+name+"</h1><h2>Search Results</h2>";
			try {
				SearchControl lc = new SearchControl();
				ArrayList<CrawledPage> docs = lc.query(query);
				//content = new JSONArray(docs).toString();		
				if(docs.isEmpty()){
					content = "No documents";
				}
				for(CrawledPage page: docs) {
				     //res.add((String) el);
					content += "<h3>"+page.getUrl()+"</h3><p>"+page.getText()+"</p>";
					if(!page.getLinks().isEmpty()){
						content += "<h4>Links</h4><ul>";
						for(String url: page.getLinks()) {
							content += "<li><a href='"+url+"'>"+url+"</a></li>";
						}
						content += "</ul>";
					}
					
					content += "<br/>";
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				content = "error";
				e.printStackTrace();
			}
			return "<html> " + "<title>" + "crawl is done" + "</title>" + "<body><h1>" + content
					+ "</body></h1>" + "</html> ";
		}
		
		//pageRankScores();
		@Path("pagerank")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String search() {
			String content = "<h1>"+name+"</h1><h2>Page Rank Results</h2>";
			SearchControl lc = new SearchControl();
			ArrayList<CrawledPage> pages = lc.pageRankedPages();
			if(pages.isEmpty()){
				content = "No documents";
			}
			for(CrawledPage page: pages) {
			     //res.add((String) el);
				content += "<h3>Page: "+page.getUrl()+"</h3><p>Page Rank: "+page.getPageRank()+"</p>";
				
				content += "<br/>";
			}
			
			return "<html> " + "<title>" + "crawl is done" + "</title>" + "<body><h1>" + content
					+ "</body></h1>" + "</html> ";
		}
		
		@Path("viewgraph")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String viewGraph(){
			String content = "none";
			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				Graph graph = new Graph();
				DB database = mongoClient.getDB("aone");
				DBCollection collectionGraph = database.getCollection("graphs");
				DBCursor cursor = collectionGraph.find().sort(new BasicDBObject("_id", -1)).limit(1);
				 if( cursor.hasNext() ){
					 BasicDBObject obj = (BasicDBObject) cursor.next();
					 //deserializeObject(byte[] data)
					 graph = (Graph) Marshaller.deserializeObject((byte[]) obj.get("graph"));
				 	 content = graph.toString();
				 	 System.out.println(content);
				 }
				     
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "<html> " + "<title>" + "no" + "</title>" + "<body>"+content+"</body>" + "</html> ";
		}
		

		@POST
		@Produces(MediaType.TEXT_PLAIN)
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		public String newDocument(@FormParam("id") int id,@FormParam("score")
		float score,@FormParam("name") String name,@FormParam("url") String url,
		@FormParam("text") String text,@FormParam("tags") List<String> tags,
		@FormParam("links") List<String> links){
			//create a new document with all of the fields and add to collection
			Document doc = new Document();
			doc.setId(id);
			doc.setScore(score);
			doc.setName(name);
			doc.setUrl(url);
			doc.setText(text);
			doc.setLinks((ArrayList<String>) links);
			doc.setTags((ArrayList<String>) tags);
			
			Documents.getInstance().open(doc);

			return "new Document entered";
		}
		
		
//		@Path("delete")
//		@GET
//		@Produces(MediaType.TEXT_PLAIN)
//		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//		public String delete(@FormParam("tags") ArrayList<String> tags,
//				@Context HttpServletResponse servletResponse) throws IOException {
//			
//			ArrayList<Document> docs=Documents.getInstance().searchForDocs(tags, false);
//			for(Document d :docs){
//				Documents.getInstance().close(d.getId());
//			}
//			return "Files deleted";
//			
//		}
//		@Path("search")
//		@GET
//		@Produces(MediaType.TEXT_PLAIN)
//		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//		public String search(@FormParam("tags") ArrayList<String> tags,
//				@Context HttpServletResponse servletResponse) throws IOException {
//			
//			ArrayList<Document> docs=Documents.getInstance().searchForDocs(tags, true);
//			String docstring="";
//			for(Document d :docs){
//				docstring+=d.getName()+"\n";
//			}
//			return docstring;
//			
//		}
//		@Path("documents")
//		@GET
//		@Produces(MediaType.TEXT_PLAIN)
//		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//		public String documents(@FormParam("tags") ArrayList<String> tags,
//				@Context HttpServletResponse servletResponse) throws IOException {
//			ArrayList<Document> docs= (ArrayList<Document>) Documents.getInstance().getDocs().values();
//			
//			String docstring="";
//			for(Document d :docs){
//				docstring+=d.getName()+"\n";
//			}
//			return docstring; // this can be put in html and xml
//			
//		}
		
}
