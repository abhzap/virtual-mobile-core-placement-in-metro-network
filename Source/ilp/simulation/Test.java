package ilp.simulation;

import ilp.given.InputConstants;
import ilp.model.IlpModel;
import ilp.model.NodePair;
import ilp.model.ServiceChain;
import ilp.model.TrafficPairChain;
import ilp.postprocess.DisplaySolution;
import ilp.preprocess.PreProcObj;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Test {
	//run Integer Linear Program
		public static void runIlp() throws Exception{
			
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
		    //list of total model service chains
		    ArrayList<TrafficPairChain> allTrafficChains = PreProcObj.setOfSDpairs(sdpaths); 
			PreProcObj.printTrafficChains(allTrafficChains);
			//create the Map with chain Id etc.
			Map<Integer,TrafficPairChain> modelChains = new HashMap<Integer,TrafficPairChain>();
			//populate these model chains
			PreProcObj.populateModelChains(modelChains,allTrafficChains,ChainSet);
			PreProcObj.printModelChains(modelChains);
			
			//place the NFV nodes
//			PreProcObj.allNFV(g);
			PreProcObj.setToNFV(g, InputConstants.NFV_NODE_LIST);
			//create the list of NFV-capable nodes
			ArrayList<BaseVertex> nfvNodes = new ArrayList<BaseVertex>();
			PreProcObj.makeNFVList(g, nfvNodes);
			//create the list of Non NFV-capable nodes
			ArrayList<BaseVertex> nonNfvNodes = new ArrayList<BaseVertex>(g._vertex_list);
			nonNfvNodes.removeAll(nfvNodes);
			
			//make the ilpobject
			IlpModel ilpProblem = new IlpModel(g,nfvNodes,nonNfvNodes,modelChains);
			System.out.println("######### ILP Model Generated! #########");
			//export the model as an ILP
			ilpProblem.ilpModel.exportModel("test.lp");
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
