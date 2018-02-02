/*
 *
 * Copyright (c) 2004-2008 Arizona State University.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ARIZONA STATE UNIVERSITY ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL ARIZONA STATE UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.asu.emit.qyan.alg.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ilp.given.InputConstants;
import edu.asu.emit.qyan.alg.model.abstracts.BaseGraph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

/**
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision: 783 $
 * @latest $Date: 2009-06-19 12:19:27 -0700 (Fri, 19 Jun 2009) $
 */
public class Graph implements BaseGraph
{
	public final static double DISCONNECTED = Double.MAX_VALUE;
	
	// index of fan-outs of one vertex
	protected Map<Integer, Set<BaseVertex>> _fanout_vertices_index =
		new HashMap<Integer, Set<BaseVertex>>();
	
	// index for fan-ins of one vertex
	protected Map<Integer, Set<BaseVertex>> _fanin_vertices_index =
		new HashMap<Integer, Set<BaseVertex>>();
	
	// index for edge weights in the graph
	protected Map<Pair<Integer, Integer>, Double> _vertex_pair_weight_index = 
		new HashMap<Pair<Integer,Integer>, Double>();
	
	// index for edge lengths in the graph
	protected Map<Pair<Integer, Integer>, Double> _vertex_pair_length_index = 
		new HashMap<Pair<Integer,Integer>, Double>();
	
	// index for vertices in the graph
	public Map<Integer, BaseVertex> _id_vertex_index = 
		new HashMap<Integer, BaseVertex>();
	
	// list of vertices in the graph 
	public List<BaseVertex> _vertex_list = new Vector<BaseVertex>();
	
	// the number of vertices in the graph
	public int _vertex_num = 0;
	
	// the number of arcs in the graph
	public int _edge_num = 0;
	
	/**
	 * Constructor 1 
	 * @param data_file_name
	 */
	public Graph(final String data_file_name)
	{
		import_from_file(data_file_name);
	}
	public Graph(final InputStream data_file_stream)
	{
		import_from_file(data_file_stream);
	}
	
	/**
	 * Constructor 2
	 * 
	 * @param graph
	 */
	public Graph(final Graph graph_)
	{
		_vertex_num = graph_._vertex_num;
		_edge_num = graph_._edge_num;
		_vertex_list.addAll(graph_._vertex_list);
		_id_vertex_index.putAll(graph_._id_vertex_index);
		_fanin_vertices_index.putAll(graph_._fanin_vertices_index);
		_fanout_vertices_index.putAll(graph_._fanout_vertices_index);
		_vertex_pair_weight_index.putAll(graph_._vertex_pair_weight_index);
		_vertex_pair_length_index.putAll(graph_._vertex_pair_length_index);
	}
	
	/**
	 * Default constructor 
	 */
	public Graph(){};
	
	/**
	 * Clear members of the graph.
	 */
	public void clear()
	{
		Vertex.reset();
		_vertex_num = 0;
		_edge_num = 0; 
		_vertex_list.clear();
		_id_vertex_index.clear();
		_fanin_vertices_index.clear();
		_fanout_vertices_index.clear();
		_vertex_pair_weight_index.clear();
	}
	
	/**
	 * There is a requirement for the input graph. 
	 * The ids of vertices must be consecutive. 
	 *  
	 * @param data_file_name
	 */
	//import file from string
	public void import_from_file(final String data_file_name)
	{
		// 0. Clear the variables 
		clear();
		//the initial line number is zero
		int lineNum = 0;
		
		try
		{
			// 1. read the file and put the content in the buffer
			FileReader input = new FileReader(data_file_name);
			BufferedReader br = new BufferedReader(input);
			boolean reading_node = false;
			boolean reading_edge = false;
			String strLine;
			
		  //The comments at the beginning of the file are parsed over							   
		   do{
		    	strLine = br.readLine();
		    	lineNum ++;
		    	
		   } while (strLine != null && !strLine.contains(InputConstants.START_OF_FILE_DELIMETER));
		   if(strLine == null){
		    	System.err.println("ERROR: Incorrect file syntax at line number:"+lineNum);
		   }
				   
				   
				 //Read File Line By Line until end
				 while ((strLine = br.readLine()) != null &&
				    	!strLine.contains(InputConstants.END_OF_FILE_DELIMETER)){
				    	
				    	lineNum ++;
				    	if(strLine == null || strLine.trim().length() == 0)
				    		continue;
				    	if(strLine.contains(InputConstants.START_OF_NODES_DELIMETER)
				    			&& !strLine.contains(InputConstants.START_OF_LINKS_DELIMETER)){
				    		reading_node = true;
				    		reading_edge = false;
				    		continue;
				    	} else if(strLine.contains(InputConstants.START_OF_LINKS_DELIMETER)
				    			&& !strLine.contains(InputConstants.START_OF_NODES_DELIMETER)){
				    		reading_node = false;
				    		reading_edge = true;
				    		continue;
				    	}
				    	if(!reading_node && !reading_edge){
				    		System.err.println("ERROR: None of node delimiter or link delimiter " +
				    				"found yet through line:"+lineNum);
				    		continue;
				    	}
				    	
				    	if(reading_node){
				    		String parts[] = strLine.split(",");	
				    		Integer node_index = Integer.valueOf(parts[0]);	
			    			BaseVertex vertex = new Vertex(node_index,parts[1]);								
							_vertex_list.add(vertex);
							_id_vertex_index.put(vertex.get_id(), vertex);
							_vertex_num ++;							
				    	}
				    	
				    	if(reading_edge){
				    		  String split_edge[] = strLine.split(","); 
							  //source node
							  int source_node_index = Integer.valueOf(split_edge[0]);
							  //destination node
							  int destination_node_index = Integer.valueOf(split_edge[1]);
							  //weight of the edge							  
							  double weight = 1.00;
							  add_edge(source_node_index, destination_node_index, weight);                       
					   	}			
			}
			br.close();

		}catch (IOException e)
		{
			// If another exception is generated, print a stack trace
			e.printStackTrace();
		}
	}	
	//import file from stream
	public void import_from_file(final InputStream data_file_stream)
	{
		// 0. Clear the variables 
				clear();
				//the initial line number is zero
				int lineNum = 0;
				
				try
				{
					// 1. read the file and put the content in the buffer			
					BufferedReader br = new BufferedReader(new InputStreamReader(data_file_stream));
					boolean reading_node = false;
					boolean reading_edge = false;
					String strLine;
					
				  //The comments at the beginning of the file are parsed over							   
				   do{
				    	strLine = br.readLine();
				    	lineNum ++;
				    	
				   } while (strLine != null && !strLine.contains(InputConstants.START_OF_FILE_DELIMETER));
				   if(strLine == null){
				    	System.err.println("ERROR: Incorrect file syntax at line number:"+lineNum);
				   }
						   
						   
						 //Read File Line By Line until end
						 while ((strLine = br.readLine()) != null &&
						    	!strLine.contains(InputConstants.END_OF_FILE_DELIMETER)){
						    	
						    	lineNum ++;
						    	if(strLine == null || strLine.trim().length() == 0)
						    		continue;
						    	if(strLine.contains(InputConstants.START_OF_NODES_DELIMETER)
						    			&& !strLine.contains(InputConstants.START_OF_LINKS_DELIMETER)){
						    		reading_node = true;
						    		reading_edge = false;
						    		continue;
						    	} else if(strLine.contains(InputConstants.START_OF_LINKS_DELIMETER)
						    			&& !strLine.contains(InputConstants.START_OF_NODES_DELIMETER)){
						    		reading_node = false;
						    		reading_edge = true;
						    		continue;
						    	}
						    	if(!reading_node && !reading_edge){
						    		System.err.println("ERROR: None of node delimiter or link delimiter " +
						    				"found yet through line:"+lineNum);
						    		continue;
						    	}
						    	
						    	if(reading_node){
						    		String parts[] = strLine.split(",");	
						    		Integer node_index = Integer.valueOf(parts[0]);	
//					    			BaseVertex vertex = new Vertex(node_index,parts[1]);
						    		BaseVertex vertex = new Vertex(node_index);
									_vertex_list.add(vertex);
									_id_vertex_index.put(vertex.get_id(), vertex);
									_vertex_num ++;							
						    	}
						    	
						    	if(reading_edge){
						    		  String split_edge[] = strLine.split(","); 
									  //source node
									  int source_node_index = Integer.valueOf(split_edge[0]);
									  //destination node
									  int destination_node_index = Integer.valueOf(split_edge[1]);
									  //weight of the edge							  
									  double weight = 1.00;//	
									  //add the length of the edge if it exists
									  if( split_edge.length > 2 ){
										  double length = Integer.valueOf(split_edge[2]);
										  add_edge(source_node_index, destination_node_index, weight, length);
									  }else{
										  add_edge(source_node_index, destination_node_index, weight);	
									  }
							   	}			
					}
					br.close();

				}catch (IOException e)
				{
					// If another exception is generated, print a stack trace
					e.printStackTrace();
				}
	}

	/**
	 * Note that this may not be used externally, because some other members in the class
	 * should be updated at the same time. 
	 * 
	 * @param start_vertex_id
	 * @param end_vertex_id
	 * @param weight
	 */
	public void add_edge(int start_vertex_id, int end_vertex_id, double weight)
	{
		// actually, we should make sure all vertices ids must be correct. 
		if(!_id_vertex_index.containsKey(start_vertex_id)
		|| !_id_vertex_index.containsKey(end_vertex_id) 
		|| start_vertex_id == end_vertex_id)
		{
			throw new IllegalArgumentException("The edge from "+start_vertex_id
					+" to "+end_vertex_id+" does not exist in the graph.");
		}
		
		// update the adjacent-list of the graph
		Set<BaseVertex> fanout_vertex_set = new HashSet<BaseVertex>();
		if(_fanout_vertices_index.containsKey(start_vertex_id))
		{
			fanout_vertex_set = _fanout_vertices_index.get(start_vertex_id);
		}
		fanout_vertex_set.add(_id_vertex_index.get(end_vertex_id));
		_fanout_vertices_index.put(start_vertex_id, fanout_vertex_set);
		
		//
		Set<BaseVertex> fanin_vertex_set = new HashSet<BaseVertex>();
		if(_fanin_vertices_index.containsKey(end_vertex_id))
		{
			fanin_vertex_set = _fanin_vertices_index.get(end_vertex_id);
		}
		fanin_vertex_set.add(_id_vertex_index.get(start_vertex_id));
		_fanin_vertices_index.put(end_vertex_id, fanin_vertex_set);

		// store the new edge 
		_vertex_pair_weight_index.put(
				new Pair<Integer, Integer>(start_vertex_id, end_vertex_id), 
				weight);
		
		++_edge_num;
	}
	//when length of the link is also required
	public void add_edge(int start_vertex_id, int end_vertex_id, double weight, double length)
	{
		// actually, we should make sure all vertices ids must be correct. 
		if(!_id_vertex_index.containsKey(start_vertex_id)
		|| !_id_vertex_index.containsKey(end_vertex_id) 
		|| start_vertex_id == end_vertex_id)
		{
			throw new IllegalArgumentException("The edge from "+start_vertex_id
					+" to "+end_vertex_id+" does not exist in the graph.");
		}
		
		// update the adjacent-list of the graph
		Set<BaseVertex> fanout_vertex_set = new HashSet<BaseVertex>();
		if(_fanout_vertices_index.containsKey(start_vertex_id))
		{
			fanout_vertex_set = _fanout_vertices_index.get(start_vertex_id);
		}
		fanout_vertex_set.add(_id_vertex_index.get(end_vertex_id));
		_fanout_vertices_index.put(start_vertex_id, fanout_vertex_set);
		
		//
		Set<BaseVertex> fanin_vertex_set = new HashSet<BaseVertex>();
		if(_fanin_vertices_index.containsKey(end_vertex_id))
		{
			fanin_vertex_set = _fanin_vertices_index.get(end_vertex_id);
		}
		fanin_vertex_set.add(_id_vertex_index.get(start_vertex_id));
		_fanin_vertices_index.put(end_vertex_id, fanin_vertex_set);

		// store the new edge weight
		_vertex_pair_weight_index.put(
				new Pair<Integer, Integer>(start_vertex_id, end_vertex_id), 
				weight);
		// store the new edge length
		_vertex_pair_length_index.put(
				new Pair<Integer, Integer>(start_vertex_id, end_vertex_id), 
				length);
		
		++_edge_num;
	}
	
	/**
	 * Store the graph information into a file. 
	 * 
	 * @param file_name
	 */
	public void export_to_file(final String file_name)
	{
		//1. prepare the text to export
		StringBuffer sb = new StringBuffer();
		sb.append(_vertex_num+"\n\n");
		for(Pair<Integer, Integer> cur_edge_pair : _vertex_pair_weight_index.keySet())
		{
			int starting_pt_id = cur_edge_pair.first();
			int ending_pt_id = cur_edge_pair.second();
			double weight = _vertex_pair_weight_index.get(cur_edge_pair);
			sb.append(starting_pt_id+"	"+ending_pt_id+"	"+weight+"\n");
		}
		//2. open the file and put the data into the file. 
		Writer output = null;
		try {
			// use buffering
			// FileWriter always assumes default encoding is OK!
			output = new BufferedWriter(new FileWriter(new File(file_name)));
			output.write(sb.toString());
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}catch(IOException e)
		{
			e.printStackTrace();
		}finally {
			// flush and close both "output" and its underlying FileWriter
			try
			{
				if (output != null) output.close();
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.asu.emit.qyan.alg.model.abstracts.BaseGraph#get_adjacent_vertices(edu.asu.emit.qyan.alg.model.abstracts.BaseVertex)
	 */
	public Set<BaseVertex> get_adjacent_vertices(BaseVertex vertex)
	{
		return _fanout_vertices_index.containsKey(vertex.get_id()) 
				? _fanout_vertices_index.get(vertex.get_id()) 
				: new HashSet<BaseVertex>();
	}

	/* (non-Javadoc)
	 * @see edu.asu.emit.qyan.alg.model.abstracts.BaseGraph#get_precedent_vertices(edu.asu.emit.qyan.alg.model.abstracts.BaseVertex)
	 */
	public Set<BaseVertex> get_precedent_vertices(BaseVertex vertex)
	{
		return _fanin_vertices_index.containsKey(vertex.get_id()) 
				? _fanin_vertices_index.get(vertex.get_id()) 
				: new HashSet<BaseVertex>();
	}
	
	/* (non-Javadoc)
	 * @see edu.asu.emit.qyan.alg.model.abstracts.BaseGraph#get_edge_weight(edu.asu.emit.qyan.alg.model.abstracts.BaseVertex, edu.asu.emit.qyan.alg.model.abstracts.BaseVertex)
	 */
	public double get_edge_weight(BaseVertex source, BaseVertex sink)
	{
		return _vertex_pair_weight_index.containsKey(
					new Pair<Integer, Integer>(source.get_id(), sink.get_id()))? 
							_vertex_pair_weight_index.get(
									new Pair<Integer, Integer>(source.get_id(), sink.get_id())) 
						  : DISCONNECTED;
	}
	public double get_edge_length(BaseVertex source, BaseVertex sink)
	{
		return _vertex_pair_length_index.containsKey(
					new Pair<Integer, Integer>(source.get_id(), sink.get_id()))? 
							_vertex_pair_length_index.get(
									new Pair<Integer, Integer>(source.get_id(), sink.get_id())) 
						  : DISCONNECTED;
	}

	/**
	 * Set the number of vertices in the graph
	 * @param num
	 */
	public void set_vertex_num(int num)
	{
		_vertex_num = num;
	}
	
	public void set_edge_num(int num)
	{
		_edge_num = num;
	}
	
	/**
	 * Return the vertex list in the graph.
	 */
	public List<BaseVertex> get_vertex_list()
	{
		return _vertex_list;
	}
	
	public void clear_vertex_weight(){
		for(BaseVertex vrt : this._vertex_list){
			//clear the weight for each vertex
			vrt.clear_weight();
		}
	}
	
	/**
	 * Get the vertex with the input id.
	 * 
	 * @param id
	 * @return
	 */
	public BaseVertex get_vertex(int id)
	{
		return _id_vertex_index.get(id);
	}
}
