package ilp.simulation;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilp.given.InputConstants;
import ilp.model.IlpModel;
import ilp.model.ServiceChain;
import ilp.model.TrafficPairChain;
import ilp.postprocess.DisplaySolution;
import ilp.preprocess.PreProcObj;
import ilp.preprocess.TrafficGenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class PerRepBwPerCt {
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
		
		//populate function replicas count
		public static ArrayList<ArrayList<Integer>> replicaCountGeneration(int rC){
			ArrayList<ArrayList<Integer>> replicaCounts = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> replicaCount = new ArrayList<Integer>(Arrays.asList(rC,rC,rC,rC,rC));
			replicaCounts.add(replicaCount);
			replicaCount = new ArrayList<Integer>(Arrays.asList(1,rC,rC,rC,rC));
			replicaCounts.add(replicaCount);
			replicaCount = new ArrayList<Integer>(Arrays.asList(rC,1,rC,rC,rC));
			replicaCounts.add(replicaCount);
			replicaCount = new ArrayList<Integer>(Arrays.asList(rC,rC,1,rC,rC));
			replicaCounts.add(replicaCount);
			replicaCount = new ArrayList<Integer>(Arrays.asList(rC,rC,rC,1,rC));
			replicaCounts.add(replicaCount);
			replicaCount = new ArrayList<Integer>(Arrays.asList(rC,rC,rC,rC,1));
			replicaCounts.add(replicaCount);
			replicaCount = new ArrayList<Integer>(Arrays.asList(rC,rC,rC,1,1));
			replicaCounts.add(replicaCount);
			replicaCount = new ArrayList<Integer>(Arrays.asList(1,rC,rC,1,1));
			replicaCounts.add(replicaCount);
			return replicaCounts;
		}
		
		//initialize function replicas
		public static HashMap<Integer, Integer> populateReplicas(ArrayList<Integer> replicaCount){
			HashMap<Integer, Integer> repPerFunc = new HashMap<Integer, Integer>();
			for(int fCnt=0; fCnt<replicaCount.size(); fCnt++){
				repPerFunc.put(InputConstants.FUNCTION_LIST.get(fCnt), replicaCount.get(fCnt));
			}
			return repPerFunc;
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
			
		    
		    //bandwidth per link
		    double bwPerLink = 60; //in Gbps
		    //CPU cores per node
		    double cpuCoresPerNode = 2400;
		    //control Plane Fraction
		    ArrayList<Double> cntrlPlaneFracs = new ArrayList<Double>();
		    cntrlPlaneFracs.add(0.05);
		    cntrlPlaneFracs.add(0.1);
		    cntrlPlaneFracs.add(0.15);
		    cntrlPlaneFracs.add(0.2);
		    cntrlPlaneFracs.add(0.25);
		    cntrlPlaneFracs.add(0.5);
		    //Link Bw's
		    HashMap<Double, Double> perCpfLinkBw = new HashMap<Double,Double>();		  
		    //CPU cores per Node
		    HashMap<Double, Double> perCpfCpuVals = new HashMap<Double,Double>();
		    //populate the HashMaps
		    for(Double cpf : cntrlPlaneFracs){
		    	perCpfLinkBw.put(cpf, bwPerLink*(cpf/0.05));
		    	perCpfCpuVals.put(cpf, cpuCoresPerNode*(cpf/0.05));
		    }
		    
		    //CPU Cores 
		    //maximum iteration count
		    int maxItCount = 4;  
		    //number of NFV nodes to be used
			int nfvNodesUsed = 3; 
		       
			
		  
		    //number of iterations
			for(int itCnt=1; itCnt<maxItCount; itCnt++){			
			    //list of total model service chains 
			    ArrayList<TrafficPairChain> allTrafficChains = TrafficGenerator.generateTrafficPairChains(g, flowsPerNasEvent, apnGateways);
				PreProcObj.printTrafficChains(allTrafficChains);
				//create the Map with chain Id etc.
				Map<Integer,TrafficPairChain> modelChains = new HashMap<Integer,TrafficPairChain>();
					
											
				for(int repInc=2; repInc<=nfvNodesUsed; repInc++){
					for(Double controlPlaneFraction : cntrlPlaneFracs){
						//file for replica vs Bandwidth
					    String file1 = "ReplicaPerVnfvsBw_I" + itCnt + "_R" + repInc + "_CF" + controlPlaneFraction + ".txt";
					    BufferedWriter bw1 = new BufferedWriter(new FileWriter(file1)); 
					    //record iteration count
						bw1.write(Integer.toString(itCnt));	
						
						//populate these model chains
						PreProcObj.populateModelChains(modelChains,allTrafficChains,ChainSet,controlPlaneFraction);
						PreProcObj.printModelChains(modelChains);
						
						//list of replica counts
						ArrayList<ArrayList<Integer>> replicaCounts = replicaCountGeneration(repInc);
						bw1.write("\t");
						for(ArrayList<Integer> replicaCount : replicaCounts){
							//function replica count
							Map<Integer, Integer> functionReplicaCount = populateReplicas(replicaCount);
							//make the ILP object
							IlpModel ilpProblem = new IlpModel(g,nfvNodes,nonNfvNodes,modelChains,nfvNodesUsed,functionReplicaCount,perCpfLinkBw.get(controlPlaneFraction),perCpfCpuVals.get(controlPlaneFraction));
							System.out.println("######### ILP Model Generated! #########");							
							//set the relative gap to 1%
							ilpProblem.ilpModel.setParam(IloCplex.DoubleParam.EpGap,0.01);
							//set the time limit to 20 mins (1200 seconds)
//							ilpProblem.ilpModel.setParam(IloCplex.DoubleParam.TiLim,1200);
							
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
								//report results
								DisplaySolution.showAllNonZeroVariables(ilpProblem.ilpModel, ilpProblem.usedVarH, ilpProblem.usedVarX, ilpProblem.usedVarXc, ilpProblem.usedVarY, modelChains);	
							}catch(IloException exp){
								//write result to file
						        bw1.write("\tInfeasible");
							}
							//deallocate the model once it is solved
							ilpProblem.ilpModel.end();
						}
					    //close file writers
						bw1.close();
					}
				}				
			}	
			
			
		}
}
