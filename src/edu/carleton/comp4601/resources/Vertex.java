package edu.carleton.comp4601.resources;

public class Vertex implements java.io.Serializable {
 /**
	 * 
	 */
	private static final long serialVersionUID = -2947926947629624064L;
String url;

public Vertex(String url) {
	super();
	this.url = url;
}

public String getUrl() {
	return url;
}

public void setUrl(String url) {
	this.url = url;
}

public static long getSerialversionuid() {
	return serialVersionUID;
}

@Override
public String toString() {
	return "Vertex [url=" + url + "]";
} 

 
 
}
