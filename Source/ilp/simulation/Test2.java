package ilp.simulation;

import ilp.given.InputConstants;
import ilp.model.IlpModel;
import ilp.model.NodePair;
import ilp.model.ServiceChain;
import ilp.model.TrafficPairChain;
import ilp.postprocess.DisplaySolution;
import ilp.preprocess.PreProcObj;
import ilp.preprocess.TrafficGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Test2 {
	
	//Initialize Application Gateways
	public static HashMap<Integer, ArrayList<Integer>> locateAppGws(){
		HashMap<Integer, ArrayList<Integer>> appGws = new HashMap<Integer, ArrayList<Integer>>();
		//Internet (Non-real-time application, web, email)
		ArrayList<Integer> locations = new ArrayList<Integer>();
		locations.add(19);
		appGws.put(1,locations);
		//VoIP 
		locations = new ArrayList<Integer>();
		locations.add(2);
		appGws.put(2,locations);
		//Video (Live Streaming)
		locations = new ArrayList<Integer>();
		locations.add(3);
		appGws.put(3,locations);
		//Video (Progressive Video)
		locations = new ArrayList<Integer>();
		locations.add(1);locations.add(8);locations.add(12);
		appGws.put(4,locations);
		//Media Downloads (FTP, P2P, Progressive Download)
		locations = new ArrayList<Integer>();
		locations.add(19);
		appGws.put(5,locations);
		
		return appGws;
	}
	
	//Initialize number of traffic flows for each NAS event
	public static HashMap<Integer,Integer> populatesNasFlows(){
		HashMap<Integer,Integer> nasFlows = new HashMap<Integer,Integer>();
		//Attach Flows
		nasFlows.put(5,10);
		//X2 Handover Flows
		nasFlows.put(6,5);
		//S1 Handover Flows
		nasFlows.put(7, 10);
		//Dedicated Bearer Flows
		nasFlows.put(8,45);
		//Pure Data Plane Flows
		nasFlows.put(0, 50);
		
		return nasFlows;
	}	
	
	
	//run Integer Linear Program
	public static void runIlp() throws Exception{
		
		//number of NFV nodes to be used
		int nfvNodesUsed = 6; //at most 6 gateways in simulation topology
		//function replica count
		Map<Integer, Integer> functionReplicaCount = new HashMap<Integer, Integer>(){{
		    put(100,2); 
		    put(101,2);
		    put(102,2);
		    put(103,2);
		    put(104,2);
	    }};
	    //bandwidth per link
	    double bwPerLink = 50; //in Gbps
	    //CPU cores per node
	    double cpuCoresPerNode = 2300;
	    //control Plane Fraction
	    double controlPlaneFraction = 0.05;
	    
		
		//generate the graph object
		Graph g = PreProcObj.makeGraphObject();
		//make all nodes into switches
		PreProcObj.makeAllVrtSwitches(g);
		//print out the nodes and links in the graph
		PreProcObj.printGraph(g);
		
		 //get the set of service chains
	  	Map<Integer,ServiceChain> ChainSet = PreProcObj.setOfServiceChains();
		// print out the Set of Service Chains
	  	PreProcObj.printServiceChains(ChainSet);
		
	  	//generate the routes for the traffic pairs
	    HashMap<NodePair,List<Path>> sdpaths = PreProcObj.findRoutesForSDpairs(g);
	    //Initialize Application ID gateways
	    HashMap<Integer,ArrayList<Integer>> apnGateways = locateAppGws();
	    //Initialize number of aggregated traffic flows for each NAS message
	    HashMap<Integer,Integer> flowsPerNasEvent = populatesNasFlows();
	  
	    
	    //list of total model service chains
//	    ArrayList<TrafficPairChain> allTrafficChains = PreProcObj.setOfSDpairs(sdpaths); 
	    ArrayList<TrafficPairChain> allTrafficChains = TrafficGenerator.generateTrafficPairChains(g, flowsPerNasEvent, apnGateways);
		PreProcObj.printTrafficChains(allTrafficChains);
		//create the Map with chain Id etc.
		Map<Integer,TrafficPairChain> modelChains = new HashMap<Integer,TrafficPairChain>();
		//populate these model chains
		PreProcObj.populateModelChains(modelChains,allTrafficChains,ChainSet,controlPlaneFraction);
		PreProcObj.printModelChains(modelChains);
		
		//place the NFV nodes
		PreProcObj.allNFV(g);
		//create the list of NFV-capable nodes
		ArrayList<BaseVertex> nfvNodes = new ArrayList<BaseVertex>();
		PreProcObj.makeNFVList(g, nfvNodes);
		//create the list of Non NFV-capable nodes
		ArrayList<BaseVertex> nonNfvNodes = new ArrayList<BaseVertex>(g._vertex_list);
		nonNfvNodes.removeAll(nfvNodes);
		
		//make the ilpobject
		IlpModel ilpProblem = new IlpModel(g,nfvNodes,nonNfvNodes,modelChains,nfvNodesUsed,functionReplicaCount,bwPerLink,cpuCoresPerNode);
		System.out.println("######### ILP Model Generated! #########");
		//export the model as an ILP
		//ilpProblem.ilpModel.exportModel("test.lp");
		//ILP start time
        long ilpStartTime = new Date().getTime();
		//solve the ILP
		ilpProblem.ilpModel.solve();
		 //ILP end time
        long ilpEndTime = new Date().getTime();
        //ILP solve time
        long ilpSolveTime = ilpEndTime - ilpStartTime;	
        System.out.println("ILP Execution Time = " + ilpSolveTime + "ms");
		//report results
		DisplaySolution.showAllNonZeroVariables(ilpProblem.ilpModel, ilpProblem.usedVarH, ilpProblem.usedVarX, ilpProblem.usedVarXc, ilpProblem.usedVarY, modelChains);		
		
	}
}
