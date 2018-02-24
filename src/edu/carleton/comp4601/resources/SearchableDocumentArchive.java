package edu.carleton.comp4601.resources;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.Documents;


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
		
		
		@Path("query/{query}")
		@GET
		@Produces(MediaType.TEXT_HTML)
		public String search(@PathParam("query") String query) {
			String content = "none";
			try {
				LuceneControl lc = new LuceneControl();
				ArrayList<CrawledPage> docs = lc.query(query);
				content = docs.toString();		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "<html> " + "<title>" + "crawl is done" + "</title>" + "<body><h1>" + content
					+ "</body></h1>" + "</html> ";
		}
		
		
		@Path("view")
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
