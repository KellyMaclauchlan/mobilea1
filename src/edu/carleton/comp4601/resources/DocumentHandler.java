package edu.carleton.comp4601.resources;

import java.util.ArrayList;

import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.Documents;

public class DocumentHandler {
	int countid=0;
	public void saveDocument(int id, float score, String name, String url, String text, ArrayList<String> links, ArrayList<String> tags){
		Document doc = new Document();
		doc.setId(id);
		doc.setScore(score);
		doc.setName(name);
		doc.setUrl(url);
		doc.setText(text);
		doc.setLinks(links);
		doc.setTags(tags);
		Documents.getInstance().open(doc);
		countid++;
	}
}
