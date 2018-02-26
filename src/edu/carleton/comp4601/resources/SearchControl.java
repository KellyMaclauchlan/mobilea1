package edu.carleton.comp4601.resources;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.bson.types.ObjectId;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Multigraph;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import edu.uci.ics.crawler4j.url.WebURL;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;


//TODO: rename to search control 
public class SearchControl {
	
	public void indexPageRank(){
		Analyzer analyzer =	new	StandardAnalyzer();	
		FSDirectory dir;
		IndexWriter	writer;
		IndexWriterConfig iwc;
		try {
			dir = FSDirectory.open(new File("C:/Users/IBM_ADMIN/SDA/index").toPath());
			iwc	= new IndexWriterConfig(analyzer);	
			iwc.setOpenMode(OpenMode.CREATE);	
			writer = new IndexWriter(dir, iwc);	
			indexDocumentsPageRank(writer);	
			
			if (writer	!=	null)
				writer.close();	
			
		 	if	(dir	!=	null)	
				dir.close();
		 	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void indexDocumentsPageRank(IndexWriter writer){
		ArrayList<CrawledPage> pages = pageRankedPages();
		for(CrawledPage page : pages){
			try {
				indexDocPageRank(writer, page);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void indexDocPageRank(IndexWriter writer, CrawledPage page) throws IOException {
		Document luceneDoc = new Document();

		System.out.println(page.getUrl()+ "\n" + page.getDocId() + "\n" + page.getText());
		
		TextField tf = new TextField("URL", page.getUrl(), Field.Store.YES);
		tf.setBoost((float) (1+page.getPageRank()));
		luceneDoc.add(tf);
		
		TextField tf2 = new TextField("docId", page.getDocId(), Field.Store.YES);
		tf2.setBoost((float) (1+page.getPageRank()));
		luceneDoc.add(tf2);
		
		TextField tf3 = new TextField("contents", page.getText(), Field.Store.YES);
		tf3.setBoost((float) (1+page.getPageRank()));
		luceneDoc.add(tf3);	//want to search body
		
		
		System.out.println(page.getTitle());
		TextField tf4 = new TextField("title", page.getTitle(), Field.Store.YES);
		tf4.setBoost((float) (1+page.getPageRank()));
		luceneDoc.add(tf4);	//want to search body
		
		
		//i
		TextField tf5 = new TextField("i","COMP4601 Searchable Document Archive V2.1: Brittny Lapierre and Kelly Maclauchlan", Field.Store.YES);
		tf5.setBoost((float) (1+page.getPageRank()));
		luceneDoc.add(tf5);	//want to search body
		
		System.out.println("lcd: " + luceneDoc.toString());
		writer.addDocument(luceneDoc);
	}
	
	
	public static void index(){
		Analyzer analyzer =	new	StandardAnalyzer();	

		FSDirectory dir;
		IndexWriter	writer;
		IndexWriterConfig iwc;
		try {
			dir = FSDirectory.open(new File("C:/Users/IBM_ADMIN/SDA/index").toPath());
			iwc	= new IndexWriterConfig(analyzer);	
			iwc.setOpenMode(OpenMode.CREATE);	
			writer = new IndexWriter(dir, iwc);	
			indexDocuments(writer);	
			
			if (writer	!=	null)
				writer.close();	
			
		 	if	(dir	!=	null)	
				dir.close();
		 	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	

	private static void indexDocuments(IndexWriter writer){
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB database = mongoClient.getDB("aone");
			DBCollection pages = database.getCollection("pages");
			DBCursor cursor = pages.find();
			while(cursor.hasNext()){
				DBObject page = cursor.next();
				
				indexMongoDoc(writer, page);
				
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private static void indexMongoDoc(IndexWriter writer, DBObject page) throws IOException {
		Document luceneDoc = new Document();

		System.out.println(page.get("url").toString()+ "\n" + page.get("_id").toString() + "\n" + page.get("text").toString());
		TextField tf = new TextField("URL", page.get("url").toString(), Field.Store.YES);
		tf.setBoost(1);
		luceneDoc.add(tf);
		
		TextField tf2 = new TextField("docId", page.get("_id").toString(), Field.Store.YES);
		tf2.setBoost(1);
		luceneDoc.add(tf2);
		
		TextField tf3 = new TextField("contents", page.get("text").toString(), Field.Store.YES);
		tf3.setBoost(1);
		luceneDoc.add(tf3);	//want to search body
		
		System.out.println(page.get("name"));
		TextField tf4 = new TextField("title", page.get("name").toString(), Field.Store.YES);
		tf4.setBoost(1);
		luceneDoc.add(tf4);	//want to search body
		
		//i
		TextField tf5 = new TextField("i","COMP4601 Searchable Document Archive V2.1: Brittny Lapierre and Kelly Maclauchlan", Field.Store.YES);
		tf5.setBoost(1);
		luceneDoc.add(tf5);	//want to search body
		
		System.out.println("lcd: " + luceneDoc.toString());
		writer.addDocument(luceneDoc);
	}
	
	
	public ArrayList<CrawledPage> query(String searchString) {	
		ArrayList<CrawledPage> docs = new ArrayList<CrawledPage>();
		//System.out.println(searchString.matches(".:."));
		
		if(searchString.contains(":")){
			String[] terms = searchString.split(" ");
			ArrayList<String> fields = new ArrayList<String>();
			for(String pair : terms){
				if(pair.contains(":")){
					String[] pairArr = pair.split(":");
					if(pairArr.length > 0){
						fields.add(pairArr[0]);
					}
				}
			}
			System.out.println("Term specific!!");
			for(String field  : fields){
				System.out.println(field);
			}
			try {
				Path path = new File("C:/Users/IBM_ADMIN/SDA/index").toPath();
				Directory index = FSDirectory.open(path);
				IndexReader	reader = DirectoryReader.open(index);	
				IndexSearcher searcher = new IndexSearcher(reader);	
				Analyzer analyzer = new StandardAnalyzer();
			    System.out.println("query: " + searchString);
			    //Query q = new QueryParser("contents", analyzer).parse(searchString);
			    Query q = MultiFieldQueryParser.parse(searchString, fields.toArray(new String[fields.size()]), new BooleanClause.Occur[]{BooleanClause.Occur.MUST} , analyzer);
			    //q.setDefaultOperator(QueryParser.Operator.AND);
			    TopDocs	results	= searcher.search(q, 200); //make 200 later
				System.out.println("RES: " + results.totalHits);
				ScoreDoc[] hits	= results.scoreDocs;
				
				for	(ScoreDoc hit: hits) {	
					Document indexDoc;
						indexDoc = searcher.doc(hit.doc);
						String id = indexDoc.get("docId");	
						System.out.println("ID: " + id);
						if (id != null)	{	
							CrawledPage d = find(id);	
								if (d != null) {	
									System.out.println("HIT! " + id);
									d.setScore(hit.score); // Used in display to user	
									updateCrawledPageScore(id, hit.score);
									docs.add(d);	
							}	
						}	
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				Path path = new File("C:/Users/IBM_ADMIN/SDA/index").toPath();
				Directory index = FSDirectory.open(path);
				IndexReader	reader = DirectoryReader.open(index);	
				IndexSearcher searcher = new IndexSearcher(reader);	
				Analyzer analyzer = new StandardAnalyzer();
			    System.out.println("query: " + searchString);
			    Query q = new QueryParser("contents", analyzer).parse(searchString+"*");
			 
				TopDocs	results	= searcher.search(q, 200); //make 200 later
				System.out.println("RES: " + results.totalHits);
				ScoreDoc[] hits	= results.scoreDocs;
				for	(ScoreDoc hit: hits) {	
					Document indexDoc;
						indexDoc = searcher.doc(hit.doc);
						String id = indexDoc.get("docId");	
						System.out.println("ID: " + id);
						if (id != null)	{	
							CrawledPage d = find(id);	
								if (d != null) {	
									System.out.println("HIT! " + id);
									d.setScore(hit.score); // Used in display to user	
									updateCrawledPageScore(id, hit.score);
									docs.add(d);	
							}	
						}	
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		return docs;
	 }


	private CrawledPage find(String id) {
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB database = mongoClient.getDB("aone");
			DBCollection pages = database.getCollection("pages");
			
			BasicDBObject query = new BasicDBObject();
			query.put("_id", new ObjectId(id));//
			
			DBCursor cursor = pages.find(query);
			if(cursor.hasNext()){
				DBObject res = cursor.next();
				
				HashSet<WebURL> hash = new HashSet<WebURL>();

				for(Object el: (BasicDBList) res.get("links")) {
				     //res.add((String) el);
					WebURL url = new WebURL();
					url.setURL((String) el);
					hash.add(url);
				}
				
				Set<WebURL> links = hash;
				CrawledPage page = new CrawledPage((String)res.get("url"), (int) res.get("textLength"), (int) res.get("htmlLength"), (int) res.get("outGoingLinks"), links, (String) res.get("text"), (String)res.get("html"));
				
				return page;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
	
	private CrawledPage updateCrawledPageScore(String id, float score) {
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB database = mongoClient.getDB("aone");
			DBCollection pages = database.getCollection("pages");
			
			BasicDBObject newDocument = new BasicDBObject();
			newDocument.append("$set", new BasicDBObject().append("score", score));

			BasicDBObject searchQuery = new BasicDBObject().append("_id", new ObjectId(id));


			pages.update(searchQuery, newDocument);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
	
	public Multigraph<Vertex, DefaultWeightedEdge> getGraph(){
		MongoClient mongoClient;
		try {
			mongoClient = new MongoClient("localhost", 27017);
			Graph graph = new Graph();
			DB database = mongoClient.getDB("aone");
			DBCollection collectionGraph = database.getCollection("graphs");
			DBCursor cursor = collectionGraph.find().sort(new BasicDBObject("_id", -1)).limit(1);
			 if( cursor.hasNext() ){
				 BasicDBObject obj = (BasicDBObject) cursor.next();
				 //deserializeObject(byte[] data)
				 graph = (Graph) Marshaller.deserializeObject((byte[]) obj.get("graph"));
			 	 return graph.getMultiGraph();
			 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    return null;
	}
	
	public Map<Vertex, Double> pageRankScores(){
		Multigraph<Vertex, DefaultWeightedEdge> g = getGraph();
		if(g != null){
			PageRank<Vertex, DefaultWeightedEdge> pr = new PageRank<Vertex, DefaultWeightedEdge>(g);
			Map<Vertex, Double> scores = pr.getScores();
			System.out.println(scores);
			return scores;
		}
		return null;
	}
	
	public ArrayList<CrawledPage> pageRankedPages(){
		Map<Vertex, Double> pageRanks = pageRankScores();
		ArrayList<CrawledPage> pages = new ArrayList<CrawledPage>();
		
		if(pageRanks != null){
			MongoClient mongoClient;
			try {
				mongoClient = new MongoClient("localhost", 27017);
				DB database = mongoClient.getDB("aone");
				DBCollection pageCollection = database.getCollection("pages");
				for (Map.Entry<Vertex, Double> pageRank : pageRanks.entrySet()){
					
					BasicDBObject query = new BasicDBObject();
					query.put("url", pageRank.getKey().getUrl());//
					
					DBCursor cursor = pageCollection.find(query);//.limit(1)
					
					if(cursor.hasNext()){
						BasicDBObject res = (BasicDBObject) cursor.next();
						HashSet<WebURL> hash = new HashSet<WebURL>();

						for(Object el: (BasicDBList) res.get("links")) {
						     //res.add((String) el);
							WebURL url = new WebURL();
							url.setURL((String) el);
							hash.add(url);
						}
						
						Set<WebURL> links = hash;
						CrawledPage page = new CrawledPage((String)res.get("url"), (int) res.get("textLength"), (int) res.get("htmlLength"), (int) res.get("outGoingLinks"), links, (String) res.get("text"), (String)res.get("html"));
						page.setDocId(res.getObjectId("_id").toString());
						page.setPageRank(pageRank.getValue());
						System.out.println((String)res.get("name"));
						page.setTitle((String)res.get("name"));
						pages.add(page);
					}
				} 
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		return pages;
	}
}
	


