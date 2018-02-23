package edu.carleton.comp4601.resources;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Multigraph;

import edu.uci.ics.crawler4j.url.WebURL;

public class Graph implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2651595938664802991L;
	Multigraph<Vertex, DefaultWeightedEdge> multiGraph;
	ConcurrentHashMap<String,Vertex> vertices;
	
	public Graph() {
		super();
		vertices = new ConcurrentHashMap<String,Vertex>();
		multiGraph = new Multigraph<Vertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	}
	
	public synchronized void GraphIt(String url, Set<WebURL> links){
		Vertex u= new Vertex(url);
		if(vertices.containsKey(url)){
			u=vertices.get(url);
		}else{
			vertices.put(url, u);
			multiGraph.addVertex(u);
		}
		for(WebURL w : links){
			String ur=w.getURL();
			Vertex v = new Vertex(ur);
			if(vertices.containsKey(ur)){
				v=vertices.get(ur);
			}else{
				multiGraph.addVertex(v);
				vertices.put(ur, v);
			}
			if(multiGraph.containsEdge(u, v)||multiGraph.containsEdge(v, u)){
				System.out.print("edge already there");
			}else{
				if(u.url!=v.url)
					multiGraph.addEdge(u, v);
				
			}
			
		}
		System.out.println(multiGraph.toString());
		
	}
	
	@Override
	public String toString() {
		return "Graph [multiGraph=" + multiGraph.toString() + "]";
	}

	public synchronized Multigraph<Vertex, DefaultWeightedEdge> getMultiGraph() {
		return multiGraph;
	}


	
	
}
