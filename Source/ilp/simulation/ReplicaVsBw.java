package ilp.simulation;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilp.model.IlpModel;
import ilp.model.NodePair;
import ilp.model.ServiceChain;
import ilp.model.TrafficPairChain;
import ilp.postprocess.DisplaySolution;
import ilp.postprocess.WriteResults;
import ilp.preprocess.PreProcObj;
import ilp.preprocess.TrafficGenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class ReplicaVsBw {
	
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
		  	
		    //Initialize Application ID gateways
		    HashMap<Integer,ArrayList<Integer>> apnGateways = locateAppGws();
		    //Initialize number of aggregated traffic flows for each NAS message
		    HashMap<Integer,Integer> flowsPerNasEvent = populatesNasFlows();
		    
		    //place the NFV nodes
			PreProcObj.allNFV(g);
			//create the list of NFV-capable nodes
			ArrayList<BaseVertex> nfvNodes = new ArrayList<BaseVertex>();
			PreProcObj.makeNFVList(g, nfvNodes);
			//create the list of Non NFV-capable nodes
			ArrayList<BaseVertex> nonNfvNodes = new ArrayList<BaseVertex>(g._vertex_list);
			nonNfvNodes.removeAll(nfvNodes);
			
			
			
		    //number of NFV nodes to be used
			int nfvNodesUsed = 6; //at most 6 gateways in simulation topology
		    //bandwidth per link
		    double bwPerLink = 60; //in Gbps
		    //CPU cores per node
		    double cpuCoresPerNode = 2400;
		    //control Plane Fraction
		    double controlPlaneFraction = 0.05;
		    //maximum iteration count
		    int maxItCount = 10;		
		    
		  
		    
			
		  
		    //number of iterations
			for(int itCnt=0; itCnt<maxItCount; itCnt++){			
			    //list of total model service chains 
			    ArrayList<TrafficPairChain> allTrafficChains = TrafficGenerator.generateTrafficPairChains(g, flowsPerNasEvent, apnGateways);
				PreProcObj.printTrafficChains(allTrafficChains);
				//create the Map with chain Id etc.
				Map<Integer,TrafficPairChain> modelChains = new HashMap<Integer,TrafficPairChain>();
				//populate these model chains
				PreProcObj.populateModelChains(modelChains,allTrafficChains,ChainSet,controlPlaneFraction);
				PreProcObj.printModelChains(modelChains);	
				
			    
			    //file for replica vs Bandwidth
			    String file1 = "ReplicaVsBw_I" + itCnt + ".txt";
			    BufferedWriter bw1 = new BufferedWriter(new FileWriter(file1));
			    String file2 = "IlpExecTime_I" + itCnt + ".txt";
			    BufferedWriter bw2 = new BufferedWriter(new FileWriter(file2));
			    String file3 = "LoadPerNode_I" + itCnt + ".txt";
			    BufferedWriter bw3 = new BufferedWriter(new FileWriter(file3));
			    String file4 = "maxLoadedLink_I" + itCnt + ".txt";
			    BufferedWriter bw4 = new BufferedWriter(new FileWriter(file4));
				
				//record iteration count
				bw1.write(Integer.toString(itCnt));
				bw2.write(Integer.toString(itCnt));
				bw3.write(Integer.toString(itCnt));
				bw4.write(Integer.toString(itCnt));
				for(int repInc=0; repInc<=nfvNodesUsed; repInc++){	
					//function replica count
					Map<Integer, Integer> functionReplicaCount = new HashMap<Integer, Integer>(){{
					    put(100,1); 
					    put(101,1);
					    put(102,1);
					    put(103,1);
					    put(104,1);
				    }};
					//update HashMap with new replica count
					for(int key : functionReplicaCount.keySet()){
						int value = functionReplicaCount.get(key) + repInc;
						functionReplicaCount.put(key, value);
					}
					//make the ilp object
					IlpModel ilpProblem = new IlpModel(g,nfvNodes,nonNfvNodes,modelChains,nfvNodesUsed,functionReplicaCount,bwPerLink,cpuCoresPerNode);
					System.out.println("######### ILP Model Generated! #########");
					//if there is a single replica in replica count
					if(functionReplicaCount.values().contains(1)){
						//set the relative gap to 1%
						ilpProblem.ilpModel.setParam(IloCplex.DoubleParam.EpGap,0.01);
						//set the time limit to 20 mins (1200 seconds)
						ilpProblem.ilpModel.setParam(IloCplex.DoubleParam.TiLim,1200);
					}
					//export the model as an ILP
					//ilpProblem.ilpModel.exportModel("test.lp");
					try{
						//ILP start time
				        long ilpStartTime = new Date().getTime();
						//solve the ILP
						ilpProblem.ilpModel.solve();
						 //ILP end time
				        long ilpEndTime = new Date().getTime();
				        //ILP solve time
				        long ilpSolveTime = ilpEndTime - ilpStartTime;	
				        System.out.println("ILP Execution Time = " + ilpSolveTime + "ms");
				        //write result to file
				        bw1.write("\t" + ilpProblem.ilpModel.getObjValue());
				        bw2.write("\t" + ilpSolveTime);
				        WriteResults.calculateTrafficLoadPerNode(bw3, ilpProblem.ilpModel, ilpProblem.usedVarXc, modelChains);
				        WriteResults.calculateMaxLoadedLink(bw4, ilpProblem.ilpModel, ilpProblem.usedVarY, modelChains, itCnt, repInc+1);
						//report results
						DisplaySolution.showAllNonZeroVariables(ilpProblem.ilpModel, ilpProblem.usedVarH, ilpProblem.usedVarX, ilpProblem.usedVarXc, ilpProblem.usedVarY, modelChains);
					}catch(IloException exp){
						System.err.println(exp);
						bw1.write("\tInfeasible" );
				        bw2.write("\tInfeasible" );
					}
					//deallocate the model once it is solved
					ilpProblem.ilpModel.end();					
				}
				//close file writers
				bw1.close();
				bw2.close();
				bw3.close();
				bw4.close();
			}		
			
		}
}
