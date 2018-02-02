package ilp.preprocess;
import ilp.fileop.ReadFile;
import ilp.given.InputConstants;
import ilp.model.FunctionToInstance;
import ilp.model.NodePair;
import ilp.model.ServiceChain;
import ilp.model.TrafficPairChain;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




import edu.asu.emit.qyan.alg.control.YenTopKShortestPathsAlg;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class PreProcObj {

	public static Graph makeGraphObject() throws Exception{		
		//build graph object from given network file
	    Class<?> cls = Class.forName("ilp.simulation.Sim");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
	    //print out the class name
	    System.out.println("Class Name : " + cLoader.getClass());
	    //finds the resource with the given name
	    InputStream networkFileStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH + InputConstants.NETWORK_FILE_NAME);
	    //return the Graph Object
	    return new Graph(networkFileStream);
	}
	
	public static void printGraph(Graph g){
		//print out the nodes
		System.out.println("Vertex List:");
		for(BaseVertex vrt : g.get_vertex_list()){
			System.out.println(vrt.get_id() + "," + vrt.get_type());
		}
		//print out the links
		System.out.println("Edge List:");
		for(BaseVertex source: g.get_vertex_list()){
			for(BaseVertex sink: g.get_adjacent_vertices(source)){
				System.out.println(source.get_id() + "," + sink.get_id() + "," + g.get_edge_weight(source, sink) + "," + g.get_edge_length(source, sink));
			}
		}		
	}
	
	public static void makeAllVrtSwitches(Graph g){
		for(BaseVertex vrt: g._vertex_list){
			vrt.set_type("sw");
		}
	}
	
	public static Map<Integer, ServiceChain> setOfServiceChains() throws Exception{
		//build graph object from given network file
	    Class<?> cls = Class.forName("ilp.simulation.Sim");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
		// read the Set of Service Chains
 	    InputStream chainSetStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH + InputConstants.CHAIN_SET);
 	    return ReadFile.readChainSet(chainSetStream);		
	}
	
	public static void printServiceChains(Map<Integer, ServiceChain> ChainSet){
		for(Map.Entry<Integer,ServiceChain> entry : ChainSet.entrySet()){
			 System.out.print(entry.getKey() + "\t");
			 for(FunctionToInstance temp : entry.getValue().getChainSeq()){ 
				 System.out.print(temp.functionID + "," + temp.instanceID + "\t"); 
			 }			
			 System.out.println(); 
		 }
	}
	
	//populate model service chains
	public static void populateModelChains(Map<Integer,TrafficPairChain> modelChains, 
			ArrayList<TrafficPairChain> allTrafficChains,Map<Integer,ServiceChain> ChainSet, double controlPlaneFraction){
		int chainIndex = 1000;
		//update the objects
		for(TrafficPairChain prChain : allTrafficChains){
			//update chainId
			prChain.chainIndex = chainIndex;
			chainIndex = chainIndex + 1;
			//if NAS procedure used
			if(prChain.controlScId != 0){
				//upload
				if(InputConstants.ulScIds.contains(prChain.controlScId)){
					//add source //only one instance
					prChain.funcSeq.add(new FunctionToInstance(prChain.v1.get_id(),1,controlPlaneFraction));
					//add control plane chain
					for(FunctionToInstance funcInst : ChainSet.get(prChain.controlScId).chainSeq){
						prChain.funcSeq.add(new FunctionToInstance(funcInst.functionID,funcInst.instanceID,controlPlaneFraction));
					}
				}else{//download
					//add destination //only one instance
					prChain.funcSeq.add(new FunctionToInstance(prChain.v2.get_id(),1,controlPlaneFraction));
					//add control plane chain
					for(FunctionToInstance funcInst : ChainSet.get(prChain.controlScId).chainSeq){
						prChain.funcSeq.add(new FunctionToInstance(funcInst.functionID,funcInst.instanceID,controlPlaneFraction));
					}
					//add destination //only one instance
					//destination to source routing does not add any weight
					prChain.funcSeq.add(new FunctionToInstance(prChain.v2.get_id(),1,0.0));
				}
			}			
			//add source //only one instance
			prChain.funcSeq.add(new FunctionToInstance(prChain.v1.get_id(),1));
			//add data plane chain
			for(FunctionToInstance funcInst : ChainSet.get(prChain.dataScId).chainSeq){
				prChain.funcSeq.add(new FunctionToInstance(funcInst));
			}
			//add destination
			prChain.funcSeq.add(new FunctionToInstance(prChain.v2.get_id(),1));			
		}
		//populate the map
		for(TrafficPairChain prChain : allTrafficChains){
			modelChains.put(prChain.chainIndex, prChain);
		}
	}
	
	//populate model service chains
	public static void populateModelChains(Map<Integer,TrafficPairChain> modelChains, 
			ArrayList<TrafficPairChain> allTrafficChains,Map<Integer,ServiceChain> ChainSet){
		int chainIndex = 1000;
		//update the objects
		for(TrafficPairChain prChain : allTrafficChains){
			//update chainId
			prChain.chainIndex = chainIndex;
			chainIndex = chainIndex + 1;
			//if NAS procedure used
			if(prChain.controlScId != 0){
				//upload
				if(InputConstants.ulScIds.contains(prChain.controlScId)){
					//add source //only one instance
					prChain.funcSeq.add(new FunctionToInstance(prChain.v1.get_id(),1,InputConstants.CONTROL_PLANE_FRACTION));
					//add control plane chain
					for(FunctionToInstance funcInst : ChainSet.get(prChain.controlScId).chainSeq){
						prChain.funcSeq.add(new FunctionToInstance(funcInst.functionID,funcInst.instanceID,InputConstants.CONTROL_PLANE_FRACTION));
					}
				}else{//download
					//add destination //only one instance
					prChain.funcSeq.add(new FunctionToInstance(prChain.v2.get_id(),1,InputConstants.CONTROL_PLANE_FRACTION));
					//add control plane chain
					for(FunctionToInstance funcInst : ChainSet.get(prChain.controlScId).chainSeq){
						prChain.funcSeq.add(new FunctionToInstance(funcInst.functionID,funcInst.instanceID,InputConstants.CONTROL_PLANE_FRACTION));
					}
					//add destination //only one instance
					//destination to source routing does not add any weight
					prChain.funcSeq.add(new FunctionToInstance(prChain.v2.get_id(),1,0.0));
				}
			}			
			//add source //only one instance
			prChain.funcSeq.add(new FunctionToInstance(prChain.v1.get_id(),1));
			//add data plane chain
			for(FunctionToInstance funcInst : ChainSet.get(prChain.dataScId).chainSeq){
				prChain.funcSeq.add(new FunctionToInstance(funcInst));
			}
			//add destination
			prChain.funcSeq.add(new FunctionToInstance(prChain.v2.get_id(),1));			
		}
		//populate the map
		for(TrafficPairChain prChain : allTrafficChains){
			modelChains.put(prChain.chainIndex, prChain);
		}
	}
	
	//print out the populated chains
	public static void printModelChains(Map<Integer,TrafficPairChain> modelChains){
		for(Map.Entry<Integer,TrafficPairChain> entry : modelChains.entrySet()){
			System.out.print(entry.getKey() + "\t");
			for(FunctionToInstance funcInst : entry.getValue().funcSeq){
				System.out.print("(" + funcInst.functionID + "," + funcInst.instanceID + ")\t");
			}
			System.out.println();
		}
	}
	
	//find routes for SD pairs
	public static HashMap<NodePair, List<Path>> findRoutesForSDpairs(Graph g) {
    	// k shortest paths
 		int top_k = InputConstants.k_paths;
 		// k shortest path objects
 		YenTopKShortestPathsAlg kpaths = new YenTopKShortestPathsAlg(g);
 		// Store paths for each s-d pair
 		HashMap<NodePair, List<Path>> sdpaths = new HashMap<NodePair, List<Path>>();
 		for(BaseVertex source_vert : g._vertex_list){
 			for(BaseVertex target_vert : g._vertex_list){
 				if (source_vert != target_vert) {
 					List<Path> path_temp = new ArrayList<Path>(kpaths.get_shortest_paths(source_vert, target_vert,top_k));
 					// create the sd-pair for that pair of nodes
 					NodePair sd_temp = new NodePair(source_vert,target_vert);
 					// add to list of paths depending on s-d pair
 					sdpaths.put(sd_temp, path_temp);
 				}
 			}
 		}
 		return sdpaths;
	}
	
	//read the list of SD pairs
	public static ArrayList<TrafficPairChain> setOfSDpairs( HashMap<NodePair, List<Path>> sdpaths) throws Exception{
		//build graph object from given network file
	    Class<?> cls = Class.forName("ilp.simulation.Sim");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
		InputStream sdPairStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH + InputConstants.SD_PAIRS);
		return ReadFile.readSDPairs(sdPairStream, sdpaths);
	}
	
	//print out all the traffic chains
	public static void printTrafficChains(List<TrafficPairChain> pairList){
		Double totalTraffic = 0.0;
		System.out.println("***** Traffic Node Pairs and SC ID's *****");		
		System.out.println("SD pair ; Control ScId; Data ScId; AppId; Traffic");
		for(TrafficPairChain tn : pairList){	
				totalTraffic += tn.flowTraffic;
				System.out.println("(" + tn.v1 + "," + tn.v2 + ") ; " + tn.controlScId + " ; " + tn.dataScId + " ; " + tn.appId + " ; " + tn.flowTraffic);				
		}
		System.out.println("Total number of TrafficPairChains = " + pairList.size() + " ; Traffic = " + totalTraffic + "Gbps");
	}
	
	//make all nodes NFV capable
	public static void allNFV(Graph g){
		for(BaseVertex vrt : g._vertex_list) {			
			vrt.set_type("nfv");
			System.out.println("Vertex " + vrt.get_id() + " set to type nfv!");
		}
	}
		
	//make list of NFV nodes
	public static void makeNFVList(Graph g, ArrayList<BaseVertex> nfv_nodes){
		System.out.println("Making list of NFV nodes!");
		for(BaseVertex tmp_vrt : g._vertex_list) {			
			if(tmp_vrt.get_type().equalsIgnoreCase("nfv")){
				nfv_nodes.add(tmp_vrt);
			}
		}		
	}
	
	//make list of NFV node
	public static void setToNFV(Graph g, List<Integer> vertexList){
		System.out.println("Making list of NFV nodes!");
		for(int vrtId : vertexList){
			for(BaseVertex vrt : g._vertex_list){
				if(vrtId == vrt.get_id()){
					vrt.set_type("nfv");
					System.out.println("Vertex " + vrt.get_id() + " set to type nfv!");
				}
			}
		}
	}
	
}
