package edu.carleton.comp4601.resources;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Multigraph; 
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;


import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {
	Graph graph;
	MongoClient mongoClient;
	int idCount;
	
	public MyCrawler() {
		super();
		graph = new Graph();
		idCount = 0;
		
		try {
			mongoClient = new MongoClient("localhost", 27017);
			DB database = mongoClient.getDB("aone");
			DBCollection collectionGraph = database.getCollection("graphs");
//			DBObject theObj = collectionGraph.findOne();
//
//			    Graph g=(Graph) Marshaller.deserializeObject((byte[]) theObj.get("graph"));
//			    System.out.println(g.toString());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif"
                                                           + "|png|mp3|mp4|zip|gz))$");

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
     @Override
     public boolean shouldVisit(Page referringPage, WebURL url) {
         String href = url.getURL().toLowerCase();
         System.out.println(href);
         return !FILTERS.matcher(href).matches()
                && href.startsWith("https://sikaman.dyndns.org");
     }

     /**
      * This function is called when a page is fetched and ready
      * to be processed by your program.
      */
     @Override
     public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);
        try {
          URL url2 = new URL(url);
          InputStream input = url2.openStream();
          LinkContentHandler linkHandler = new LinkContentHandler();
          ContentHandler textHandler = new BodyContentHandler();
          ToHTMLContentHandler toHTMLHandler = new ToHTMLContentHandler();
          TeeContentHandler teeHandler = new TeeContentHandler(linkHandler, textHandler, toHTMLHandler);
          Metadata metadata = new Metadata();
          ParseContext parseContext = new ParseContext();
          HtmlParser parser = new HtmlParser();

		  parser.parse(input, teeHandler, metadata, parseContext);
		  
		  
	      System.out.println("title:\n" + metadata.get("title"));
	      System.out.println("links:\n" + linkHandler.getLinks());
	      System.out.println("text:\n" + textHandler.toString());
	      System.out.println("html:\n" + toHTMLHandler.toString());
	      
	      String text = textHandler.toString();
          String html = toHTMLHandler.toString();
          String title = metadata.get("title");
          //Set<WebURL> links = htmlParseData.getOutgoingUrls();
          Set<WebURL> links = new HashSet<WebURL>();
          ArrayList<String> linksArray = new ArrayList<String>();
          for(Link link : linkHandler.getLinks()){
         	 WebURL wurl = new WebURL();
			 wurl.setURL(link.getUri());
         	 links.add(wurl);
         	 linksArray.add(link.getUri());
          }
	      //metadata.get(property)
	      System.out.println("MD: " + metadata.names().toString());

	      
	      
	      Document doc = Jsoup.parse(toHTMLHandler.toString());

             
          HashSet<String> linksfound=new HashSet<String>();
          HashSet<String> headers=new HashSet<String>();
          HashSet<String> body=new HashSet<String>();
          HashSet<String> images=new HashSet<String>();
          ArrayList<String> tags = new ArrayList<String>();

          if(title != null && title.length() > 0){
        	  tags.add(title); 
          }
          
          //if(metadata.get("description").length() > 0){
        //	  tags.add(metadata.get("description")); 
          //}
          
          Elements linksFound = doc.select("a[href]");
          for( Element e : linksFound){
             linksfound.add(e.attr("href")+" "+e.text());
          }
          Elements im = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
          for( Element e : im){
             images.add("src:"+e.attr("src")+" height: "+e.attr("height")+" width: "+e.attr("width")+" alt: "+e.attr("alt"));
             tags.add(e.attr("alt"));
          }
          Elements para = doc.select("p");
          for( Element e : para){
             body.add(e.text());
             tags.add(e.text());
          }
          Elements heads = doc.select("h0, h1, h2, h3, h4");
          for( Element e : heads){
             headers.add(e.text());
             tags.add(e.text());
          }
          
          /*Elements metaTags = doc.select("meta");
          for( Element e : metaTags){
             tags.add(e.text());
          }*/
          
             
          //do mongo db entries here 
          CrawledPage cp= new CrawledPage(url,text.length(),html.length(),links.size(),links,text,html);
          cp.linksFound=linksfound;
          cp.images=images;
          cp.headers=headers;
          cp.body=body;
          cp.docId=doc.id();
          cp.score = 0;
             

             
     	  DB database = mongoClient.getDB("aone");
     	  DBCollection collection = database.getCollection("pages");
     	  collection.setObjectClass(CrawledPage.class);
     			

     	  BasicDBObject newDocument = new BasicDBObject();
     	  newDocument.put("url", cp.getUrl());
     	  newDocument.put("textLength", cp.getTextLength());
     	  newDocument.put("htmlLength", cp.getHtmlLength());
     	  newDocument.put("outGoingLinks", cp.getOutGoingLinks());
     	  newDocument.put("links", cp.getLinks());
     	  newDocument.put("text", cp.getText());
     	  newDocument.put("html", cp.getHtml());
     	  newDocument.put("linksfound", cp.getLinksFound());
     	  newDocument.put("body", cp.getBody());
     	  newDocument.put("headers", cp.getHeaders());
     	  newDocument.put("images", cp.getImages());
     	  newDocument.put("id",idCount);
     	  newDocument.put("score",cp.getScore());
     	  newDocument.put("name",title);
     				
     	  collection.insert(newDocument);

     	  DBCollection collectionGraph = database.getCollection("graphs");
     	  BasicDBObject newDoc = new BasicDBObject();
     	  byte[] g = Marshaller.serializeObject(graph);
     	  newDoc.put("graph", g);
     	  newDoc.put("url",cp.getUrl());
     	  collectionGraph.insert(newDoc);
     				

      	  graph.GraphIt(url, links);
     	  DocumentHandler dh = new DocumentHandler();
     	  dh.saveDocument(idCount, 0, title, url, textHandler.toString(), linksArray, tags);
     		      
     	  System.out.println("GRAPH: " + graph.toString());
     	  idCount++;	
     	} catch (Exception e2) {
     	  // TODO Auto-generated catch block
     	  e2.printStackTrace();
     	}
    }
}
