package edu.carleton.comp4601.resources;

import java.net.UnknownHostException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;


import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.Documents;

public class Action {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	String id;

	public Action(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = id;
		
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Document getDocumentXML() {
		Document d = Documents.getInstance().find(new Integer(id));
		if (d == null) {
			throw new RuntimeException("No such Documnet: " + id);
		}
		return d;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getDocumentHTML() {
		Document d = Documents.getInstance().find(new Integer(id));
		if (d == null) {
			throw new RuntimeException("No such Documnet: " + id);
		}
		return "<html> " + "<title>" +d.getName()  + "</title>" + "<body><h1>" + d.getUrl()
				+ "</body></h1>" + "</html> ";
	}
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public Response putDocument(JAXBElement<Document> doc) {
		Document c = doc.getValue();
		return putAndGetResponse(c);
	}
	
	private Response putAndGetResponse(Document document) {
		Response res; 
		Document docFound = Documents.getInstance().find(new Integer(id));
		if(docFound !=null){
			res = Response.ok().build();
		}else {
			res = Response.created(uriInfo.getAbsolutePath()).build();
			
		}
		
		Documents.getInstance().getDocs().put(document.getId(), document);
		
//		MongoClient mongoClient;
//		try {
//			mongoClient = new MongoClient("localhost", 27017);
//			DB database = mongoClient.getDB("bank");
//			DBCollection collection = database.getCollection("accounts");
//			collection.setObjectClass(Account.class);
//			
//			// in this i update the whole collection each time anything changed 
//			//if for account with that id existing 
//			for (Do a : Accounts.getInstance().getAccounts().values()){
//				BasicDBObject newDocument = new BasicDBObject();
//				newDocument.put("id", a.getId());
//				newDocument.put("balance", a.getBalance());
//				newDocument.put("description", a.getDescription());
//				BasicDBObject query = new BasicDBObject();
//				query.put("id", a.getId());
//				DBCursor cursor = collection.find(query);
//				if(cursor.count()>=1){
//					BasicDBObject updateObject = new BasicDBObject();
//					updateObject.put("$set", newDocument);
//					 
//					collection.update(query, updateObject);
//				}else{
//					collection.insert(newDocument);
//				}
//			}
//			
//			
////			mongoClient.getDatabaseNames().forEach(System.out::println);
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return res;
	}
	
	@DELETE
	public void deleteDocument() {
		System.out.println(id);
		MongoClient mongoClient;
//		try {
//			mongoClient = new MongoClient("localhost", 27017);
//			DB database = mongoClient.getDB("bank");
//			DBCollection collection = database.getCollection("accounts");
//
//			BasicDBObject searchQuery = new BasicDBObject();
//			searchQuery.put("id", new Integer(id));
//			 
//			collection.remove(searchQuery);
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if (!Documents.getInstance().close(new Integer(id)))
			throw new RuntimeException("Document " + id + " not found");
	}
}
