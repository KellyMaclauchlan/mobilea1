package edu.carleton.comp4601.resources;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import edu.carleton.comp4601.utility.ServiceRegistrar;
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
			ServiceRegistrar.register(name, "http://sikaman.dyndns.org:8080/COMP4601-Directory/rest/directory", SDAConstants.COMP4601SDA, "Kelly and Brittny Search");
			//register
			//put in call to db to get accounts and put into the list 
			Documents.getInstance();
		}
		
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String sayHtml() {
			return "<html> " + "<title>" + name + "</title>" + "<body><h1>" + name
					+ "</body></h1>" + "</html> ";
		}
		
		@Path("register")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String register() {
			ServiceRegistrar.register(name, "http://sikaman.dyndns.org:8080/COMP4601-Directory/rest/directory", SDAConstants.COMP4601SDA, "Kelly and Brittny Search");
			
			return "<html> " + "<title>" + name + "</title>" + "<body><h1>" + name
					+ "</h1>Registered</body>" + "</html> ";
		}
		
		@Path("unregister")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String unregister() {
			ServiceRegistrar.unregister(name);
			return "<html> " + "<title>" + name + "</title>" + "<body><h1>" + name
					+ "</h1>Unregistered</body>" + "</html> ";
		}
		
		@Path("list")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String list() {
			String serverList = ServiceRegistrar.list();
			return "<html> " + "<title>" + name + "</title>" + "<body><h1>" + name
					+ "</h1><h3>List</h3>"+serverList+"</body>" + "</html> ";
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
			
			MongoClient mongoClient;
			try {
				mongoClient = new MongoClient("localhost", 27017);
				DB database = mongoClient.getDB("aone");
				DBCollection pageCollection = database.getCollection("pages");
				
				BasicDBObject document = new BasicDBObject();
				document.put("score", score);
				document.put("name", name);
				document.put("url", url);
				document.put("text", text);
				document.put("links", links);
				document.put("tags", tags);

				pageCollection.insert(document);
				
			} catch (Exception e){
				e.printStackTrace();
			}
			
			return "new Document entered";
		}
		
		
		//DOCUMENT REQUESTS
		@Path("documents")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String documents() {
			String content = "<ul>";
			for (Document doc : Documents.getInstance().getDocs().values()){
				content += "<li>" + doc.getName() + "</li>";
			}
			content += "</ul>";
			return "<html> " + "<title>" + "Doc List" + "</title>" + "<body><h1>" + name
					+ "</h1><h3>List of all documents</h3>"+content+"</body>" + "</html> ";
		}
		
		@Path("documents")
		@GET
		@Produces(MediaType.APPLICATION_XML)
		public String documentsXML() {
			String content = "<documents>";
			for (Document doc : Documents.getInstance().getDocs().values()){
				content += "<document>" + doc.getName() + "</document>";
			}
			content += "</documents>";
			return content;
		}
		
		
		@Path("view/{id}")
		@GET
		@Produces(MediaType.APPLICATION_XML)
		public String viewByID(@PathParam("id") int id) {
			Document doc = Documents.getInstance().find(id);
			String tags = "";
			if(!doc.getTags().isEmpty()){
				tags += "<h4>Tags</h4><ul>";
				for(String tag : doc.getTags()){
					tags += "<li>" + tag + "</li>";
				}
				tags += "</ul>";
			}
			return "<html> " + "<title>" + "View Document" + "</title>" + "<body><h1>" + name
					+ "</h1><h3>"+doc.getName()+"</h3><h5><i>"+doc.getUrl()+"</i></h5><p>"+doc.getText()+"</p>"+tags+"</body>" + "</html> ";
		}
		
		
		@Path("view/{id}")
		@GET
		@Produces(MediaType.TEXT_XML)
		public String viewByIDXML(@PathParam("id") int id) {
			Document doc = Documents.getInstance().find(id);
			String tags = "";
			if(!doc.getTags().isEmpty()){
				tags += "<tags>";
				for(String tag : doc.getTags()){
					tags += "<tag>" + tag + "</tag>";
				}
				tags += "</tags>";
			}
			return "<document><name>"+doc.getName()+"</name><url>"+doc.getUrl()+"</url><text>"+doc.getText()+"</text>"+tags+"</document> ";
		}
		
		@Path("delete/{tags}")
		@DELETE
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		@Produces(MediaType.TEXT_HTML)
		public String deleteByTag(@PathParam("tags") String tags) {
			System.out.println("HERE 1");
			String names="";
			ArrayList<Document> docs=Documents.getInstance().searchForDocs((ArrayList<String>)Arrays.asList(tags.split(" ")), false);
			 		for(Document doc :docs){
							//Documents.getInstance().close(d.getId());
			 			
			//Document doc = Documents.getInstance().find(id);
			if(doc != null){
				names+=doc.getName()+", ";
				System.out.println("HERE 2");
				MongoClient mongoClient;
				try {
					mongoClient = new MongoClient("localhost", 27017);
					DB database = mongoClient.getDB("aone");
					DBCollection pageCollection = database.getCollection("pages");
					BasicDBObject query = new BasicDBObject();
					query.put("url", doc.getUrl());
					System.out.println("HERE 3");
					pageCollection.remove(query);
						
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("HERE 5");
				Documents.getInstance().close(doc.getId());
				System.out.println("HERE 6");
				
			}
			 		}
			 		return "<html> " + "<title>" + "crawl is done" + "</title>" + "<body><h1>" + name
							+ "</h1><h3>Deleted doc with tags: "+tags+" named: "+names+"</h3></body>" + "</html> ";
			
//			return "<html> " + "<title>" + "crawl is done" + "</title>" + "<body><h1>" + name
//					+ "</h1><h3>No matching docs</h3></body>" + "</html> ";
		}
		@Path("{id}")
		@DELETE
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		@Produces(MediaType.TEXT_HTML)
		public String deleteByID(@PathParam("id") int id) {
			System.out.println("HERE 1");
			Document doc = Documents.getInstance().find(id);
			if(doc != null){
				System.out.println("HERE 2");
				MongoClient mongoClient;
				try {
					mongoClient = new MongoClient("localhost", 27017);
					DB database = mongoClient.getDB("aone");
					DBCollection pageCollection = database.getCollection("pages");
					BasicDBObject query = new BasicDBObject();
					query.put("url", doc.getUrl());
					System.out.println("HERE 3");
					pageCollection.remove(query);
						
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("HERE 5");
				Documents.getInstance().close(id);
				System.out.println("HERE 6");
				return "<html> " + "<title>" + "crawl is done" + "</title>" + "<body><h1>" + name
						+ "</h1><h3>Deleted doc with id: "+id+" named: "+doc.getName()+"</h3></body>" + "</html> ";
			}
			
			return "<html> " + "<title>" + "crawl is done" + "</title>" + "<body><h1>" + name
					+ "</h1><h3>No matching docs</h3></body>" + "</html> ";
		}
		
		@Path("update")
		@POST
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		@Produces(MediaType.TEXT_HTML)
		public String update(@FormParam("id") int id,@FormParam("score")
		float score,@FormParam("name") String name,@FormParam("url") String url,
		@FormParam("text") String text,@FormParam("tags") List<String> tags,
		@FormParam("links") List<String> links){
			//create a new document with all of the fields and add to collection
		  if(Documents.getInstance().find(id) != null){
			String url2 = Documents.getInstance().find(id).getUrl();
			Documents.getInstance().close(id);
			
			Document doc = new Document();
			doc.setId(id);
			doc.setScore(score);
			doc.setName(name);
			doc.setUrl(url);
			doc.setText(text);
			doc.setLinks((ArrayList<String>) links);
			doc.setTags((ArrayList<String>) tags);
			
			Documents.getInstance().open(doc);
			
			MongoClient mongoClient;
			try {
				mongoClient = new MongoClient("localhost", 27017);
				DB database = mongoClient.getDB("aone");
				DBCollection pageCollection = database.getCollection("pages");
				
				
				BasicDBObject query = new BasicDBObject();
				query.put("url", url2);
				pageCollection.remove(query);
				
				
				BasicDBObject document = new BasicDBObject();
				document.put("score", score);
				document.put("name", name);
				document.put("url", url);
				document.put("text", text);
				document.put("links", links);
				document.put("tags", tags);

				pageCollection.insert(document);
				
			} catch (Exception e){
				e.printStackTrace();
			}
			return "updated Document";
		  }
			
		  return "Document not found";
		}
		
		//TODO: once distributed search works remove this ugly code and delete properly
		@Path("deletequery")
		@DELETE
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		@Produces(MediaType.TEXT_HTML)
		public String deleteByQuery(@FormParam("query") String query) {
			SearchControl lc = new SearchControl();
			ArrayList<CrawledPage> docs = lc.query(query);
			if(docs.isEmpty()){
				return "no documents matching query";
			}
			Collection<Document> values = Documents.getInstance().getDocs().values();
			
			ArrayList<String> deleted = new ArrayList<String>();

			MongoClient mongoClient;
			try {
				mongoClient = new MongoClient("localhost", 27017);
				DB database = mongoClient.getDB("aone");
				DBCollection pageCollection = database.getCollection("pages");
				for(CrawledPage page: docs) {
					for(Document doc : values){
						if(page.getUrl() == doc.getUrl()){
							if(Documents.getInstance().find(doc.getId()) != null){
								deleted.add(doc.getName());
								BasicDBObject obj = new BasicDBObject();
								obj.put("url", doc.getUrl());
								System.out.println("HERE 3");
								pageCollection.remove(obj);
								Documents.getInstance().close(doc.getId());
							}
						}
					}
				}
				
			} catch (Exception e) {
			e.printStackTrace();
			}
			
			String content = "<ul>";
			if(deleted.isEmpty()){
				content = "none";
			} else {
				for(String name : deleted){
					content += "<li>"+name+"</li>";
				}
			}
			content += "</ul>";
			return "<html> " + "<title>" + "crawl is done" + "</title>" + "<body><h1>" + name
					+ "</h1>"+content+"</body>" + "</html> ";
		}

		//CRAWL AND SEARCH REQUESTS
		
		@Path("crawl")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String crawl() {
			String content = "";
			try {
				Controller.startCrawler();
				for (Document doc : Documents.getInstance().getDocs().values()){
					content += "<h3>" + doc.getName() + "</h3>";
					if(!doc.getTags().isEmpty()){
						content += "<h4>Tags</h4><ul>";
						for(String tag : doc.getTags()){
							content += "<li>" + tag + "</li>";
						}
						content += "</ul>";
					}
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "<html> " + "<title>" + "crawl is done" + "</title>" + "<body><h1>" + name
					+ "</h1>"+content+"</body>" + "</html> ";
		}
		
		
		//TODO: no results rn - find reason why
		@Path("search/{terms}")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String profSearch(@PathParam("terms") String terms) {
			String content = "<h1>"+name+"</h1><h2>Search Results</h2>";
			try {
				String[] termarr = terms.split("\\+");
				System.out.println(termarr);
				ArrayList<Document> docs = Documents.getInstance().searchForDocs(new ArrayList<String>(Arrays.asList(termarr)), true);
				if(docs.isEmpty()){
					content = "No documents";
				}
				for(Document page: docs) {
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
			return "<html> " + "<title>" + "local search" + "</title>" + "<body><h1>" + content
					+ "</body></h1>" + "</html> ";
		}

		//TODO: no results rn - find reason why
		@Path("simplequery/{query}")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String profQuery(@PathParam("query") String query) {
			String content = "<h1>"+name+"</h1><h2>Search Results</h2>";
			SearchResult sr	=	SearchServiceManager.getInstance().query(query);
			ArrayList<Document> docs = new ArrayList<Document>();//Documents.getInstance().query(query);	
			try {
				sr.await(SDAConstants.TIMEOUT,	TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}		
			docs.addAll(sr.getDocs());	
			
			try {
				if(docs.isEmpty()){
					content = "No documents";
				}
				for(Document page: docs) {
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
			return "<html> " + "<title>" + "local search" + "</title>" + "<body><h1>" + content
					+ "</body></h1>" + "</html> ";
		}
		
		//TODO: make above work then delete or change url for this request
		//Multiple: http://localhost:8080/COMP4601-SDA/rest/sda/query/+coconut%20banana
		@Path("query/{query}")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String query(@PathParam("query") String query) {
			String content = "<h1>"+name+"</h1><h2>Search Results</h2>";
			try {
				SearchControl lc = new SearchControl();
				ArrayList<CrawledPage> docs = lc.query(query);	
				if(docs.isEmpty()){
					content = "No documents";
				}
				for(CrawledPage page: docs) {
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
			return "<html> " + "<title>" + "local search" + "</title>" + "<body><h1>" + content
					+ "</body></h1>" + "</html> ";
		}
		
		
		
		@Path("search/{terms}")
		@GET
		@Produces(MediaType.APPLICATION_XML)
		public String profSearchXML(@PathParam("terms") String terms) {
			String content = "";
			try {
				String[] termarr = terms.split("\\+");
				System.out.println(termarr);
				ArrayList<Document> docs = Documents.getInstance().searchForDocs(new ArrayList<String>(Arrays.asList(termarr)), true);
				
				for(Document page: docs) {
					content += "<result><url>"+page.getUrl()+"</url><text>"+page.getText()+"</text>";
					if(!page.getLinks().isEmpty()){
						content += "<links>";
						for(String url: page.getLinks()) {
							content += "<link>"+url+"</link>";
						}
						content += "</links>";
					}
					
					content += "</result>";
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				content = "error";
				e.printStackTrace();
			}
			return "<results>" + content + "</results>";
		}

		//TODO: no results rn - find reason why
		@Path("simplequery/{query}")
		@GET
		@Produces(MediaType.APPLICATION_XML)
		public String profQueryXML(@PathParam("query") String query) {
			String content = "";
			SearchResult sr	=	SearchServiceManager.getInstance().query(query);
			ArrayList<Document> docs = new ArrayList<Document>();//Documents.getInstance().query(query);	
			try {
				sr.await(SDAConstants.TIMEOUT,	TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}		
			docs.addAll(sr.getDocs());	
			
			try {
				for(Document page: docs) {
					content += "<result><url>"+page.getUrl()+"</url><text>"+page.getText()+"</text>";
					if(!page.getLinks().isEmpty()){
						content += "<links>";
						for(String url: page.getLinks()) {
							content += "<link>"+url+"</link>";
						}
						content += "</links>";
					}
					
					content += "</result>";
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				content = "error";
				e.printStackTrace();
			}
			return "<results>" + content + "</results>";
		}
		
		//TODO: make above work then delete or change url for this request
		//Multiple: http://localhost:8080/COMP4601-SDA/rest/sda/query/+coconut%20banana
		@Path("query/{query}")
		@GET
		@Produces(MediaType.APPLICATION_XML)
		public String queryXML(@PathParam("query") String query) {
			String content = "";
			try {
				SearchControl lc = new SearchControl();
				ArrayList<CrawledPage> docs = lc.query(query);	
				for(CrawledPage page: docs) {
					content += "<result><url>"+page.getUrl()+"</h3><p>"+page.getText()+"</p>";
					if(!page.getLinks().isEmpty()){
						content += "<links>";
						for(String url: page.getLinks()) {
							content += "<link>"+url+"</link>";
						}
						content += "</links>";
					}
					
					content += "</result>";
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				content = "error";
				e.printStackTrace();
			}
			return "<results>" + content + "</results>";
		}
		
		
		//TODO: return html table
		@Path("pagerank")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String search() {
			String content = "<h1>"+name+"</h1><h2>Page Rank Results</h2><table style=\"border: 1px solid grey;border-collapse: collapse;\"><tr><th style=\"border: 1px solid grey;\">Page</th><th style=\"border: 1px solid grey;\">Page Rank Score</th></tr>";
			SearchControl lc = new SearchControl();
			ArrayList<CrawledPage> pages = lc.pageRankedPages();
			if(pages.isEmpty()){
				content = "No documents";
			}
			for(CrawledPage page: pages) {
			     //res.add((String) el);
				content += "<tr><td style=\"border: 1px solid grey;\">"+page.getUrl()+"</td><td style=\"border: 1px solid grey;\">"+page.getPageRank()+"</td></tr>";
				
			}
			
			return "<html> " + "<title>" + "pagerank" + "</title>" + "<body><h1>" + content
					+ "</table></body></h1>" + "</html> ";
		}
		
		@Path("boost")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String boost(){
			SearchControl sc = new SearchControl();
			sc.indexPageRank();	
			return "<html> " + "<title>" + "Boosted" + "</title>" + "<body>Boosted re-indexing complete.</body>" + "</html> ";
		}
		
		@Path("noboost")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String noboost(){
			SearchControl.index();
			return "<html> " + "<title>" + "re-indexed" + "</title>" + "<body>Re-indexing complete.</body>" + "</html> ";
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
			return "<html> " + "<title>" + "last graph" + "</title>" + "<body>"+content+"</body>" + "</html> ";
		}
		
}
