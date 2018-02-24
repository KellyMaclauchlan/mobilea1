package edu.carleton.comp4601.resources;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
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
	
	public static void index(){
		Analyzer analyzer =	new	StandardAnalyzer();	
		/*
		 * For	each	resource	(i.e.,	a	MongoDB	document)	
			– Create	a	Lucene	document	
			– Use	each	field	Mongo	document	create	a	field	in	
			  the	Lucene	document	deciding	whether	to	allow	it	
			  to	be	searchable	or	not.		
			– Save	the	Lucent	document.	*/
		/*
		 */
		//File docDir	= new File(CRAWL_DIR);	
		FSDirectory dir;
		IndexWriter	writer;
		IndexWriterConfig iwc;
		try {
			dir = FSDirectory.open(new File("C:/Users/IBM_ADMIN/dev/mobilea1/lucenedir").toPath());
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
		
		/*
		 * Your Lucene documents must contain fields for:
			URL -- the resolved location of the original document.
			DocID -- the id of the document stored within your database.
			Date -- meaning the date and time when the document was crawled.
			Content -- this is the content returned by the content handler used in the Standard 
			Analyzer. For HTML page this should contain information extracted using JSOUP; e.g., 
			content of paragraph, heading and title tags. For images, this may not contain much.
			Metadata fields -- a field should be created for each piece of meta data; e.g., for 
			a file with a MIME type of image/jpeg the field name would be type and the value 
			would be image/jpeg.
		 * */
		System.out.println(page.get("url").toString()+ "\n" + page.get("_id").toString() + "\n" + page.get("text").toString());
		luceneDoc.add(new TextField("URL", page.get("url").toString(), Field.Store.YES));
		luceneDoc.add(new TextField("DB_ID", page.get("_id").toString(), Field.Store.YES));
		
		//luceneDoc.add(new IntPoint("DOC_ID",Integer.valueOf(page.get("_id").toString())));
		//luceneDoc.add(new StoredField("DOC_ID", Integer.valueOf(page.get("_id").toString())));
		
		luceneDoc.add(new TextField("CONTENTS", page.get("text").toString(), Field.Store.YES));	//want to search body
		
		System.out.println("lcd: " + luceneDoc.toString());
		writer.addDocument(luceneDoc);
	}
	
	
	public ArrayList<CrawledPage> query(String searchString) {	
		ArrayList<CrawledPage> docs = new ArrayList<CrawledPage>();	
		try {
			Path path = new File("C:/Users/IBM_ADMIN/dev/mobilea1/lucenedir").toPath();
			Directory index = FSDirectory.open(path);
			IndexReader	reader = DirectoryReader.open(index);	
			IndexSearcher searcher = new IndexSearcher(reader);	
			Analyzer analyzer = new StandardAnalyzer();
		    System.out.println("query: " + searchString);
		    Query q = new QueryParser("CONTENTS", analyzer).parse(searchString+"*");
		 
			TopDocs	results	= searcher.search(q, 5); //make 200 later
			System.out.println("RES: " + results.totalHits);
			ScoreDoc[] hits	= results.scoreDocs;
			for	(ScoreDoc hit: hits) {	
				Document indexDoc;
					indexDoc = searcher.doc(hit.doc);
					String id = indexDoc.get("DB_ID");	
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
			newDocument.put("score", score);

			BasicDBObject searchQuery = new BasicDBObject().append("_id", id);

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
}
	
	/* in class ex
	 * private	void indexAFile(File	file,	FileInputStream fis)	throws	IOExcep0on	{	
		doc	=	new	Document();	
		Field pathField	=	new	StringField(PATH,	file.getPath(),	Field.Store.YES);	
		doc.add(pathField);	
		try	{	
			 	int docId	=	Integer.valueOf(file.getName().replaceFirst("[.][^.]+$",	""));	
			 	doc.add(new	IntField(DOC_ID,	docId,	Field.Store.YES));	
		}	catch	(NumberFormatExcep0on	e)	{	
		}	
		doc.add(new	StoredField(MODIFIED,	file.lastModified()));	
		doc.add(new	TextField(CONTENTS,	new	BufferedReader(	
					new	InputStreamReader(fis,	"UTF-8"))));	
		writer.addDocument(doc);	
		}*/	
	
	/*
	 * 	
	*/
	


