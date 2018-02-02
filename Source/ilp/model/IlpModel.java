package ilp.model;

import ilog.concert.IloColumn;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilp.given.InputConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class IlpModel {
	
	//define CPLEX object
	public IloCplex ilpModel;
	//objective
	public IloObjective bwUsed;
	//VNF placement constraint
	public Map<Cstr4,IloRange> vnfPlacementConstraint;
	//Flow constraint placement
	public Map<Cstr5,IloRange> flowConstraint;
	//no placement flow constraint
	public Map<Cstr6,IloRange> noPlacFlowConstraint;
	//vnf replica constraint 1
    public Map<VarX,IloRange> vnfReplicaConstraint1;
  //vnf replica constraint 2
    public Map<VarX,IloRange> vnfReplicaConstraint2;
    //vnf replica constraint 3
    public Map<Integer, IloRange> vnfReplicaConstraint3;    
    //VNF Location 1
    public Map<BaseVertex,IloRange> vnfLocationConstraint1;
    //VNF Location 2
    public Map<BaseVertex,IloRange> vnfLocationConstraint2;
    //VNF Location 3
    public IloRange vnfLocationConstraint3;
	//core capacity constraint
    public Map<BaseVertex,IloRange> coreCapacityConstraint;
	//flow capacity constraint
    public Map<NodePair,IloRange> linkCapacityConstraint;
	//latency constraint
    public Map<Integer,IloRange> latencyConstaint;
	
	//Handles for variable x_vf
    public Map<VarX, IloNumVar> usedVarX;
    //Handles for variable x_{vi}^{cj}
    public Map<VarXc, IloNumVar> usedVarXc;	
    //Handles for variables h_v
    public Map<VarH, IloNumVar> usedVarH;
	//Handles for var_y_l_sigma_sd
	public Map<VarY, IloNumVar> usedVarY;
	
	//create the new improved ILP object
	public IlpModel(Graph g, ArrayList<BaseVertex> nfvNodes, ArrayList<BaseVertex> nonNfvNodes,  
			Map<Integer,TrafficPairChain> modelChains, int nfvNodesUsed, Map<Integer, Integer> functionReplicaCount, double bwPerLink, double cpuCorePerNode) throws Exception{
		
		//Columns for variables
		Map<VarX,IloColumn> varXvf;
		Map<VarXc,IloColumn> varXcjvi;
		Map<VarH,IloColumn> varHv;
		Map<VarY,IloColumn> varYcil;
		
		//model for the ILP object
		this.ilpModel = new IloCplex();
		//objective(1) for ILP object //minimization
		this.bwUsed = this.ilpModel.addMinimize();
		
		//ADD THE RANGED CONSTRAINTS
		int cstrNum=0;//initializing constraint number counter
		//VNF placement constraint
		this.vnfPlacementConstraint = new HashMap<Cstr4,IloRange>();
		for(int chainIndex : modelChains.keySet()){
			for(int funcIndex=0; funcIndex<modelChains.get(chainIndex).funcSeq.size(); funcIndex++){
				int funcID = modelChains.get(chainIndex).funcSeq.get(funcIndex).functionID;
				int funcInst = modelChains.get(chainIndex).funcSeq.get(funcIndex).instanceID;
				int srcVrtID = modelChains.get(chainIndex).v1.get_id();
				int destVrtID = modelChains.get(chainIndex).v2.get_id();
				//add constraint if f_i is not f^c_s or f^c_d
				if( (funcID!=srcVrtID) && (funcID!=destVrtID)){
					String constraint = "cstr4" + "_" + cstrNum;
					IloRange rng = this.ilpModel.addRange(1.0, 1.0, constraint);
					cstrNum++;
					//keep track of the constraint				
					this.vnfPlacementConstraint.put(new Cstr4(chainIndex,funcIndex,funcInst), rng);
				}
			}
		}
		System.out.println("Constraint (4) generated!");
		
		cstrNum=0;
		//Flow Constraint
		this.flowConstraint = new HashMap<Cstr5,IloRange>();
		for(int chainIndex : modelChains.keySet()){
			BaseVertex srcVrt = modelChains.get(chainIndex).v1;
			BaseVertex destVrt = modelChains.get(chainIndex).v2;
			for(int funcIndex=0; funcIndex<modelChains.get(chainIndex).funcSeq.size()-1; funcIndex++){
				int funcInst = modelChains.get(chainIndex).funcSeq.get(funcIndex).instanceID;
				for(BaseVertex nfvNode : nfvNodes){
					String constraint = "cstr5" + "_" + cstrNum;
					IloRange rng = this.ilpModel.addRange(0.0, 0.0, constraint);
					cstrNum++;
					//keep track of the constraint
					this.flowConstraint.put(new Cstr5(chainIndex,nfvNode,funcIndex,funcInst), rng);
				}
				//check if source vertex is not NFV
				if(nonNfvNodes.contains(srcVrt)){
					String constraint = "cstr5" + "_" + cstrNum;
					IloRange rng = this.ilpModel.addRange(0.0, 0.0, constraint);
					cstrNum++;
					//keep track of the constraint
					this.flowConstraint.put(new Cstr5(chainIndex,srcVrt,funcIndex,funcInst), rng);
				}
				//check if destination vertex is not NFV
				if(nonNfvNodes.contains(destVrt)){
					String constraint = "cstr5" + "_" + cstrNum;
					IloRange rng = this.ilpModel.addRange(0.0, 0.0, constraint);
					cstrNum++;
					//keep track of the constraint
					this.flowConstraint.put(new Cstr5(chainIndex,destVrt,funcIndex,funcInst), rng);
				}
			}
		}
		System.out.println("Constraint (5) generated!");
		
		cstrNum=0;
		//No Placement Flow Constraint
		this.noPlacFlowConstraint = new HashMap<Cstr6,IloRange>();
		for(int chainIndex : modelChains.keySet()){
			int srcVrtID = modelChains.get(chainIndex).v1.get_id();
			int destVrtID = modelChains.get(chainIndex).v2.get_id();
			for(int funcIndex=0; funcIndex<modelChains.get(chainIndex).funcSeq.size()-1; funcIndex++){
				for(BaseVertex nonNfvNode :  nonNfvNodes){
					//add constraint if not source or destination
					if( (nonNfvNode.get_id()!=srcVrtID) && (nonNfvNode.get_id()!=destVrtID) ){
						String constraint = "cstr6" + "_" + cstrNum;
						IloRange rng = this.ilpModel.addRange(0.0, 0.0, constraint);
						cstrNum++;
						//keep track of the constraint
						this.noPlacFlowConstraint.put(new Cstr6(chainIndex,nonNfvNode,funcIndex), rng);
					}
				}
			}
		}
		System.out.println("Constraint (6) generated!");
		
		cstrNum=0;
		//VNF Replica constraint - (7)/1
		this.vnfReplicaConstraint1 = new HashMap<VarX,IloRange>();
		for(int funcID : InputConstants.FUNCTION_LIST){
			for(BaseVertex nfvNode : nfvNodes){
				String constraint = "cstr71" + "_" + cstrNum;
				IloRange rng = this.ilpModel.addRange(-Double.MAX_VALUE, 0.0, constraint);
				cstrNum++;
				//keep track of constraint
				this.vnfReplicaConstraint1.put(new VarX(nfvNode,funcID), rng);
			}
		}
		System.out.println("Constraint (7)/1 generated!");
		
		cstrNum=0;
		//VNF Replica constraint - (7)/2
		this.vnfReplicaConstraint2 = new HashMap<VarX,IloRange>();
		for(int funcID : InputConstants.FUNCTION_LIST){
			for(BaseVertex nfvNode : nfvNodes){
				String constraint = "cstr72" + "_" + cstrNum;
				IloRange rng = this.ilpModel.addRange(0.0, Double.MAX_VALUE, constraint);
				cstrNum++;
				//keep track of constraint
				this.vnfReplicaConstraint2.put(new VarX(nfvNode,funcID), rng);
			}
		}
		System.out.println("Constraint (7)/2 generated!");
		
		cstrNum=0;
		//VNF Replica Constraint - (8)
		this.vnfReplicaConstraint3 = new HashMap<Integer,IloRange>();
		for(int funcID : InputConstants.FUNCTION_LIST){
			String constraint = "cstr8" + "_" + cstrNum;
			IloRange rng = this.ilpModel.addRange(-Double.MAX_VALUE, 
				functionReplicaCount.get(funcID), constraint);
			cstrNum++;
			//keep track of constraint
			this.vnfReplicaConstraint3.put(funcID, rng);
		}
		System.out.println("Constraint (8) generated!");
		
		cstrNum=0;
		//VNF Location Constraint - (9)/1
		this.vnfLocationConstraint1 = new HashMap<BaseVertex,IloRange>();
		for(BaseVertex nfvNode : nfvNodes){
			String constraint = "cstr91" + "_" + cstrNum;
			IloRange rng = this.ilpModel.addRange(-Double.MAX_VALUE,0.0,constraint);
			cstrNum++;
			//keep track of constraint
			this.vnfLocationConstraint1.put(nfvNode, rng);
		}
		System.out.println("Constraint (9)/1 generated!");
		
		cstrNum=0;
		//VNF Location Constraint - (9)/2
		this.vnfLocationConstraint2 = new HashMap<BaseVertex,IloRange>();
		for(BaseVertex nfvNode : nfvNodes){
			String constraint = "cstr92" + "_" + cstrNum;
			IloRange rng = this.ilpModel.addRange(0.0,Double.MAX_VALUE,constraint);
			cstrNum++;
			//keep track of constraint
			this.vnfLocationConstraint2.put(nfvNode, rng);
		}
		System.out.println("Constraint (9)/2 generated!");
		
		cstrNum=0;
		//Location Constraint - (10)
		this.vnfLocationConstraint3 = this.ilpModel.addRange(-Double.MAX_VALUE, nfvNodesUsed,"cstr10");
		System.out.println("Constraint 10 generated");
		
		cstrNum=0;
		//Core capacity constraint - (11)
		this.coreCapacityConstraint = new HashMap<BaseVertex,IloRange>();
		for(BaseVertex nfvNode : nfvNodes){
			String constraint = "cstr11" + "_" + cstrNum;
			IloRange rng = this.ilpModel.addRange(0.0, cpuCorePerNode,constraint);
			cstrNum++;
			//keep track of constraint
			this.coreCapacityConstraint.put(nfvNode,rng);
		}
		System.out.println("Constraint 11 generated!");
		
		cstrNum=0;
		//Link capacity constraint - (12)
		this.linkCapacityConstraint = new HashMap<NodePair,IloRange>();
		for(BaseVertex srcVrt : g.get_vertex_list()){
			for(BaseVertex tarVrt : g.get_adjacent_vertices(srcVrt)){
				String constraint = "cstr12" + "_" + cstrNum;
				IloRange rng = this.ilpModel.addRange(0.0, bwPerLink, constraint);
				cstrNum++;
				//keep track of constraint
				this.linkCapacityConstraint.put(new NodePair(srcVrt,tarVrt), rng);
			}
		}
		System.out.println("Constraint 12 generated!");
		
		cstrNum=0;
		//Latency constraint - (13)
		this.latencyConstaint =  new HashMap<Integer,IloRange>();
		for(int chainIndex : modelChains.keySet()){
			TrafficPairChain tnPrSc = modelChains.get(chainIndex);
			int sVrtId = tnPrSc.v1.get_id();
			int dVrtId = tnPrSc.v2.get_id();
			double latencyThreshold = 0;
			int controlScId = modelChains.get(chainIndex).controlScId;
			int appId = modelChains.get(chainIndex).appId;
			if(controlScId == 0){
				latencyThreshold = InputConstants.APP_LATENCY.get(appId);
			}else{
				//GBR if Voice or Video(Live Streaming)
				if( (appId==2) || (appId==3) ){
					latencyThreshold = InputConstants.APP_LATENCY.get(appId) + InputConstants.CONTROL_PLANE_LATENCY_GBR;
				}else{				
					latencyThreshold = InputConstants.APP_LATENCY.get(appId) + InputConstants.CONTROL_PLANE_LATENCY_NON_GBR;
				}
			}
			//calculate processing latency
			for(int funcIndex=0; funcIndex < tnPrSc.funcSeq.size(); funcIndex++){
				int fId = tnPrSc.funcSeq.get(funcIndex).functionID;
				if( (fId!=sVrtId) && (fId!=dVrtId) ){
					double betaFactor = tnPrSc.funcSeq.get(funcIndex-1).betaTrafPerc;
					latencyThreshold -= betaFactor*tnPrSc.flowTraffic*InputConstants.PROCESSING_LATENCY;
				}
			}
			String constraint = "cstr13" + "_" + cstrNum;
			IloRange rng = this.ilpModel.addRange(0.0, latencyThreshold, constraint);
			cstrNum++;
			//keep track of constraint
			this.latencyConstaint.put(chainIndex, rng);
		}
		System.out.println("Constraint 13 generated!");
		
		
		
		
		
		//ADD THE VARIABLES
		//Columns for variable x_{vi}^{cj}
		varXcjvi = new HashMap<VarXc,IloColumn>();
		//Handles for variable x_{vi}^{cj}
		this.usedVarXc = new HashMap<VarXc,IloNumVar>();
		//VNF Placement Constraint - (4)
		for(Map.Entry<Cstr4, IloRange> entryCstr : this.vnfPlacementConstraint.entrySet()){
			for(BaseVertex nfvNode : nfvNodes){
				//create key to keep track of column
				VarXc tempKey = new VarXc(entryCstr.getKey().chainIndex,nfvNode,entryCstr.getKey().funcIndex,entryCstr.getKey().funcInst);
				//add the column to the constraint
				IloColumn col = this.ilpModel.column(entryCstr.getValue(), 1.0);
				//keep track of column
				varXcjvi.put(tempKey, col);
			}
		}
		//Flow Constraint - (5)
		for(Map.Entry<Cstr5, IloRange> entryCstr : this.flowConstraint.entrySet()){
			//create key to keep track of column
			VarXc tempKey1 = new VarXc(entryCstr.getKey().chainIndex,entryCstr.getKey().v,entryCstr.getKey().funcIndex,entryCstr.getKey().funcInst);
			//get the column corresponding to key
			IloColumn col1 = varXcjvi.get(tempKey1);
			//create key to keep track of column
			int nxtFuncIndex = entryCstr.getKey().funcIndex+1;
			int nxtFuncInstance = modelChains.get(entryCstr.getKey().chainIndex).funcSeq.get(nxtFuncIndex).instanceID;
			VarXc tempKey2 = new VarXc(entryCstr.getKey().chainIndex,entryCstr.getKey().v,nxtFuncIndex,nxtFuncInstance);
			//get the column corresponding to key
			IloColumn col2 = varXcjvi.get(tempKey2);
			if(col1 != null){
				//add the column to the constraint
				col1 = col1.and(this.ilpModel.column(entryCstr.getValue(), -1.0));
				//update column hashmap
				varXcjvi.put(tempKey1, col1);
			}else{
				//add the column to the constraint
				col1 = this.ilpModel.column(entryCstr.getValue(), -1.0);
				//keep track of column
				varXcjvi.put(tempKey1, col1);
			}
			if(col2 != null){
				//add the column to the constraint
				col2 = col2.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
				//update column hashmap
				varXcjvi.put(tempKey2, col2);
			}else{
				//add the column to the constraint
				col2 = this.ilpModel.column(entryCstr.getValue(), 1.0);
				//keep track of column
				varXcjvi.put(tempKey2, col2);
			}
		}
		//VNF Replica Constraint - (7)/1
		for(Map.Entry<VarX,IloRange> entryCstr : this.vnfReplicaConstraint1.entrySet()){
			int fId = entryCstr.getKey().f_id;
			BaseVertex nfvNode = entryCstr.getKey().v;
			//iterate through the model chains
			for(Map.Entry<Integer, TrafficPairChain> entry : modelChains.entrySet()){
				for(int funcIndex=0; funcIndex<entry.getValue().funcSeq.size(); funcIndex++){
					if(fId == entry.getValue().funcSeq.get(funcIndex).functionID){
						//create key to keep track of column
						VarXc tempKey = new VarXc(entry.getKey(),nfvNode,funcIndex,entry.getValue().funcSeq.get(funcIndex).instanceID);
//						String xName = "X_SC" + tempKey.scID + "_Nod" + tempKey.v.get_id() + "_VnfSQ" + tempKey.funcIndex + "_VnfInst" + tempKey.funcInst;
//						System.out.println(xName);
						//get the column corresponding to key
						IloColumn col = varXcjvi.get(tempKey);
						//add the column to the constraint
						col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
						//update column hashmap
						varXcjvi.put(tempKey, col);
					}
				}
			}
		}
		//VNF Replica Constraint - (7)/2
		for(Map.Entry<VarX,IloRange> entryCstr : this.vnfReplicaConstraint2.entrySet()){
			int fId = entryCstr.getKey().f_id;
			BaseVertex nfvNode = entryCstr.getKey().v;
			//iterate through the model chains
			for(Map.Entry<Integer, TrafficPairChain> entry : modelChains.entrySet()){
				for(int funcIndex=0; funcIndex<entry.getValue().funcSeq.size(); funcIndex++){
					if(fId == entry.getValue().funcSeq.get(funcIndex).functionID){
						//create key to keep track of column
						VarXc tempKey = new VarXc(entry.getKey(),nfvNode,funcIndex,entry.getValue().funcSeq.get(funcIndex).instanceID);
						//get the column corresponding to key
						IloColumn col = varXcjvi.get(tempKey);
						//add the column to the constraint
						col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
						//update column hashmap
						varXcjvi.put(tempKey, col);
					}
				}
			}
		}
		//CPU Core Constraint - (11)
		for(Map.Entry<BaseVertex, IloRange> entryCstr : this.coreCapacityConstraint.entrySet()){
			for(TrafficPairChain tnPrSc : modelChains.values()){
				int sVrtId = tnPrSc.v1.get_id();
				int dVrtId = tnPrSc.v2.get_id();
				for(int funcIndex=0; funcIndex < tnPrSc.funcSeq.size(); funcIndex++){
					//check whether source or destination function
					int fId = tnPrSc.funcSeq.get(funcIndex).functionID;
					if( (fId!=sVrtId) && (fId!=dVrtId)){
						//beta factor
						double betaFactor = tnPrSc.funcSeq.get(funcIndex-1).betaTrafPerc;
						//create key to keep track of column
						VarXc tempKey = new VarXc(tnPrSc.chainIndex,entryCstr.getKey(),funcIndex,tnPrSc.funcSeq.get(funcIndex).instanceID);
						//get the column corresponding to key
						IloColumn col = varXcjvi.get(tempKey);
						//add the column to the constraint
						col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0*tnPrSc.flowTraffic*betaFactor*InputConstants.CORE_CONSUMPTION_PER_NODE));
						//update column HashMap
						varXcjvi.put(tempKey, col);
					}
				}
			}			
		}
		//Add x_{vi}^{cj} variables to the model
		//Include the range for varXcjvi variables
		for(Map.Entry<VarXc, IloColumn> entryVar : varXcjvi.entrySet()){
			BaseVertex hostNode = entryVar.getKey().v;
			int fId = modelChains.get(entryVar.getKey().scID).funcSeq.get(entryVar.getKey().funcIndex).functionID;
			String xName = "X_SC" + entryVar.getKey().scID + "_Nod" + entryVar.getKey().v.get_id() + "_VnfSQ" + entryVar.getKey().funcIndex + "_VnfInst" + entryVar.getKey().funcInst;
		  	//source function on source node; destination function on destination node
			if(hostNode.get_id() == fId){
				this.usedVarXc.put(entryVar.getKey(),this.ilpModel.intVar(entryVar.getValue(), 1, 1, xName));
		  	}else if(nonNfvNodes.contains(hostNode)){//if EPC function on Non-NFV Node
		  		this.usedVarXc.put(entryVar.getKey(),this.ilpModel.intVar(entryVar.getValue(), 0, 0, xName));
		  	}else{			
		  		this.usedVarXc.put(entryVar.getKey(),this.ilpModel.intVar(entryVar.getValue(), 0, 1, xName));
		  	}
		}
		//deallocate X variables
		varXcjvi.clear();
		System.out.println("######## Variable Xc has been added to ILP model! ##########");
		
		
		
		
		
		
		
		
		
		
		
		//Columns for variables x_{vf}
		varXvf = new HashMap<VarX,IloColumn>();
		//Handles for variables x_{vf}
		this.usedVarX = new HashMap<VarX,IloNumVar>();
		//VNF Replica Constraint - (7)/1
		for(Map.Entry<VarX,IloRange> entryCstr : this.vnfReplicaConstraint1.entrySet()){		
			//add the column to the constraint
			IloColumn col = this.ilpModel.column(entryCstr.getValue(), -1.0*InputConstants.Big_M);
			//keep track of column
			varXvf.put(entryCstr.getKey(), col);			
		}
		//VNF Replica Constraint - (7)/2
		for(Map.Entry<VarX,IloRange> entryCstr : this.vnfReplicaConstraint2.entrySet()){			
			//get the column corresponding to key
			IloColumn col = varXvf.get(entryCstr.getKey());
			//add the column to the constraint
			col = col.and(this.ilpModel.column(entryCstr.getValue(), -1.0));
			//update column HashMap
			varXvf.put(entryCstr.getKey(), col);			
		}
		//VNF Replica Constraint - (8)
		for(Map.Entry<Integer,IloRange> entryCstr : this.vnfReplicaConstraint3.entrySet()){
			for(BaseVertex nfvNode : nfvNodes){
				//create key to find corresponding column
				VarX tempKey = new VarX(nfvNode,entryCstr.getKey());
				//get the column
				IloColumn col = varXvf.get(tempKey);
				//add column to constraint
				col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
				//update column HashMap
				varXvf.put(tempKey, col);
			}
		}
		//VNF Location Constraint - (9)/1
		for(Map.Entry<BaseVertex, IloRange> entryCstr : this.vnfLocationConstraint1.entrySet()){
			for(int fID : InputConstants.FUNCTION_LIST){
				//create key to find corresponding column
				VarX tempKey = new VarX(entryCstr.getKey(),fID);
				//get the column
				IloColumn col = varXvf.get(tempKey);
				//add column to constraint
				col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
				//update column HashMap
				varXvf.put(tempKey, col);				
			}
		}
		//VNF Location Constraint - (9)/2
		for(Map.Entry<BaseVertex, IloRange> entryCstr : this.vnfLocationConstraint2.entrySet()){
			for(int fID : InputConstants.FUNCTION_LIST){
				//create key to find corresponding column
				VarX tempKey = new VarX(entryCstr.getKey(),fID);
				//get the column
				IloColumn col = varXvf.get(tempKey);
				//add column to constraint
				col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
				//update column HashMap
				varXvf.put(tempKey, col);
			}
		}
		//Add x_{vf} variables to the model
		//Include the range for varXvf variables
		for(Map.Entry<VarX, IloColumn> entryVar : varXvf.entrySet()){
			 String xName = "X_Nod" + entryVar.getKey().v.get_id() + "_Vnf" + entryVar.getKey().f_id;
		  	 this.usedVarX.put(entryVar.getKey(),this.ilpModel.intVar(entryVar.getValue(), 0, 1, xName));
		}
		//deallocate X variables
		varXvf.clear();
		System.out.println("######## Variable X has been added to ILP model! ##########");
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		//Columns for variables y_{il}^{c}
		varYcil = new HashMap<VarY,IloColumn>();
		//Handles for variable y_{il}^{c}
		this.usedVarY = new HashMap<VarY,IloNumVar>();
		//Flow Constraint - (5)
		for(Map.Entry<Cstr5, IloRange> entryCstr : this.flowConstraint.entrySet()){			
			//outgoing links
			for(BaseVertex tVrt : g.get_adjacent_vertices(entryCstr.getKey().v)){
				//create key for column
				VarY tempKey = new VarY(entryCstr.getKey().chainIndex, entryCstr.getKey().funcIndex,entryCstr.getKey().v,tVrt);
				//get the column
				IloColumn col = varYcil.get(tempKey);
				if(col!=null){
					//add column to constraint
					col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
					//update HashMap
					varYcil.put(tempKey, col);
				}else{
					//generate column
					col = this.ilpModel.column(entryCstr.getValue(), 1.0);
					//add to HasMap
					varYcil.put(tempKey, col);
				}
			}
			//incoming links
			for(BaseVertex sVrt : g.get_precedent_vertices(entryCstr.getKey().v)){
				//create key for column
				VarY tempKey = new VarY(entryCstr.getKey().chainIndex, entryCstr.getKey().funcIndex,sVrt,entryCstr.getKey().v);
				//get the column
				IloColumn col = varYcil.get(tempKey);
				if(col!=null){
					//add column to constraint
					col = col.and(this.ilpModel.column(entryCstr.getValue(), -1.0));
					//update HashMap
					varYcil.put(tempKey, col);
				}else{
					//generate column
					col = this.ilpModel.column(entryCstr.getValue(), -1.0);
					//add to HasMap
					varYcil.put(tempKey, col);
				}
			}						
		}
		//No Placement Flow Constraint - (6)
		for(Map.Entry<Cstr6, IloRange> entryCstr : this.noPlacFlowConstraint.entrySet()){			
			//outgoing links
			for(BaseVertex tVrt : g.get_adjacent_vertices(entryCstr.getKey().v)){
				//create key for column
				VarY tempKey = new VarY(entryCstr.getKey().chainIndex, entryCstr.getKey().funcIndex,entryCstr.getKey().v,tVrt);
				//get the column
				IloColumn col = varYcil.get(tempKey);
				if(col!=null){
					//add column to constraint
					col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
					//update HashMap
					varYcil.put(tempKey, col);
				}else{
					//generate column
					col = this.ilpModel.column(entryCstr.getValue(), 1.0);
					//add to HasMap
					varYcil.put(tempKey, col);
				}
			}
			//incoming links
			for(BaseVertex sVrt : g.get_precedent_vertices(entryCstr.getKey().v)){
				//create key for column
				VarY tempKey = new VarY(entryCstr.getKey().chainIndex, entryCstr.getKey().funcIndex,sVrt,entryCstr.getKey().v);
				//get the column
				IloColumn col = varYcil.get(tempKey);
				if(col!=null){
					//add column to constraint
					col = col.and(this.ilpModel.column(entryCstr.getValue(), -1.0));
					//update HashMap
					varYcil.put(tempKey, col);
				}else{
					//generate column
					col = this.ilpModel.column(entryCstr.getValue(), -1.0);
					//add to HasMap
					varYcil.put(tempKey, col);
				}
			}			
		}
		//Flow Capacity Constraint - (12)
		for(Map.Entry<NodePair, IloRange> entryCstr : this.linkCapacityConstraint.entrySet()){
			for(Map.Entry<Integer, TrafficPairChain> entryChain : modelChains.entrySet()){
				for(int funcIndex=0; funcIndex<entryChain.getValue().funcSeq.size()-1; funcIndex++){
					//beta factor
					double betaFactor = entryChain.getValue().funcSeq.get(funcIndex).betaTrafPerc;
					//create key for column
					VarY tempKey = new VarY(entryChain.getKey(), funcIndex, entryCstr.getKey().v1, entryCstr.getKey().v2);
					//get the column
					IloColumn col = varYcil.get(tempKey);
					//add column to constraint
					col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0*entryChain.getValue().flowTraffic*betaFactor));
					//update HashMap
					varYcil.put(tempKey, col);
				}
			}
		}
		//Latency Constraint - (13)
		for(Map.Entry<Integer, IloRange> entryCstr : this.latencyConstaint.entrySet()){
			for(BaseVertex vrt : g._vertex_list){
				for(BaseVertex tVrt : g.get_adjacent_vertices(vrt)){
					for(int funcIndex=0; funcIndex<modelChains.get(entryCstr.getKey()).funcSeq.size()-1; funcIndex++){
						//create key for column
						VarY tempKey = new VarY(entryCstr.getKey(), funcIndex, vrt, tVrt);
						//get the column
						IloColumn col = varYcil.get(tempKey);
						//add column to constraint
						col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0*(g.get_edge_length(vrt, tVrt)/InputConstants.SPEED_OF_LIGHT)));
						//update HashMap
						varYcil.put(tempKey, col);
					}
				}
			}
		}
		//ADD OBJECTIVE TO THE PROBLEM
		for(Map.Entry<Integer, TrafficPairChain> entryChain : modelChains.entrySet()){
			for(BaseVertex vrt : g._vertex_list){
				for(BaseVertex tVrt : g.get_adjacent_vertices(vrt)){
					for(int funcIndex=0; funcIndex < entryChain.getValue().funcSeq.size()-1; funcIndex++){
						//beta factor
						double betaFactor = entryChain.getValue().funcSeq.get(funcIndex).betaTrafPerc;
						//create key for column
						VarY tempKey = new VarY(entryChain.getKey(), funcIndex, vrt, tVrt);
						//get the column
						IloColumn col = varYcil.get(tempKey);
						//add column to constraint
						col = col.and(this.ilpModel.column(this.bwUsed, 1.0*entryChain.getValue().flowTraffic*betaFactor));
						//update HashMap
						varYcil.put(tempKey, col);
					}					
				}				
			}			
		}		
		//Add y_{il}^{c} variables to the model
		//Include the range for varYcil variables
		for(Map.Entry<VarY, IloColumn> entryVar : varYcil.entrySet()){
			String yName = "Y_SC"+ entryVar.getKey().scId + "_Ind" + entryVar.getKey().funcIndex + "_Ls" +  entryVar.getKey().sVrt.get_id() + "_Ld" + entryVar.getKey().tVrt.get_id();
			this.usedVarY.put(entryVar.getKey(), this.ilpModel.intVar(entryVar.getValue(),0,1,yName));
		}
		//deallocate Y variables
		varYcil.clear();
		System.out.println("######## Variable y has been added to ILP model! ##########");
		
		
		
		
		
		
		//Columns for variables h_v
		varHv = new HashMap<VarH,IloColumn>();
		//Handles for variable h_{v}
		this.usedVarH = new HashMap<VarH,IloNumVar>();
		//VNF Location Constraint - (9)/1
		for(Map.Entry<BaseVertex, IloRange> entryCstr : this.vnfLocationConstraint1.entrySet()){
			//create key for the column
			VarH tempKey =  new VarH(entryCstr.getKey());
			//create column for the variable
			IloColumn col = this.ilpModel.column(entryCstr.getValue(), -1.0*InputConstants.Big_M);
			//keep track of the column
			varHv.put(tempKey, col);
		}
		//VNF Location Constraint - (9)/2
		for(Map.Entry<BaseVertex, IloRange> entryCstr : this.vnfLocationConstraint2.entrySet()){
			//create key for the column
			VarH tempKey =  new VarH(entryCstr.getKey());
			//get the col for the variable
			IloColumn col = varHv.get(tempKey);
			//add column to the constraint
			col = col.and(this.ilpModel.column(entryCstr.getValue(), -1.0));
			//keep track of the column
			varHv.put(tempKey, col);
		}
		//Location Constraint - (10)
		for(BaseVertex nfvNode : nfvNodes){
			//create key for the column
			VarH tempKey =  new VarH(nfvNode);
			//get the col for the variable
			IloColumn col = varHv.get(tempKey);
			//add column to the constraint
			col = col.and(this.ilpModel.column(this.vnfLocationConstraint3, 1.0));
			//keep track of the column
			varHv.put(tempKey, col);
		}
		//Add h_v variables to the model
		//Include the range for varHv variables
		for(Map.Entry<VarH, IloColumn> entryVar : varHv.entrySet()){
			 String hName = "H_Nod" + entryVar.getKey().node.get_id();
		  	 this.usedVarH.put(entryVar.getKey(),this.ilpModel.intVar(entryVar.getValue(), 0, 1, hName));
		}
		//deallocate H variables
		varHv.clear();
		System.out.println("######## Variable h has been added to ILP model! ##########");
	}
	
	//create the ILP object
	public IlpModel(Graph g, ArrayList<BaseVertex> nfvNodes, ArrayList<BaseVertex> nonNfvNodes,  
			Map<Integer,TrafficPairChain> modelChains) throws Exception{
		
		//Columns for variables
		Map<VarX,IloColumn> varXvf;
		Map<VarXc,IloColumn> varXcjvi;
		Map<VarH,IloColumn> varHv;
		Map<VarY,IloColumn> varYcil;
		
		//model for the ILP object
		this.ilpModel = new IloCplex();
		//objective(1) for ILP object //minimization
		this.bwUsed = this.ilpModel.addMinimize();
		
		//ADD THE RANGED CONSTRAINTS
		int cstrNum=0;//initializing constraint number counter
		//VNF placement constraint
		this.vnfPlacementConstraint = new HashMap<Cstr4,IloRange>();
		for(int chainIndex : modelChains.keySet()){
			for(int funcIndex=0; funcIndex<modelChains.get(chainIndex).funcSeq.size(); funcIndex++){
				int funcID = modelChains.get(chainIndex).funcSeq.get(funcIndex).functionID;
				int funcInst = modelChains.get(chainIndex).funcSeq.get(funcIndex).instanceID;
				int srcVrtID = modelChains.get(chainIndex).v1.get_id();
				int destVrtID = modelChains.get(chainIndex).v2.get_id();
				//add constraint if f_i is not f^c_s or f^c_d
				if( (funcID!=srcVrtID) && (funcID!=destVrtID)){
					String constraint = "cstr4" + "_" + cstrNum;
					IloRange rng = this.ilpModel.addRange(1.0, 1.0, constraint);
					cstrNum++;
					//keep track of the constraint				
					this.vnfPlacementConstraint.put(new Cstr4(chainIndex,funcIndex,funcInst), rng);
				}
			}
		}
		System.out.println("Constraint (4) generated!");
		
		cstrNum=0;
		//Flow Constraint
		this.flowConstraint = new HashMap<Cstr5,IloRange>();
		for(int chainIndex : modelChains.keySet()){
			BaseVertex srcVrt = modelChains.get(chainIndex).v1;
			BaseVertex destVrt = modelChains.get(chainIndex).v2;
			for(int funcIndex=0; funcIndex<modelChains.get(chainIndex).funcSeq.size()-1; funcIndex++){
				int funcInst = modelChains.get(chainIndex).funcSeq.get(funcIndex).instanceID;
				for(BaseVertex nfvNode : nfvNodes){
					String constraint = "cstr5" + "_" + cstrNum;
					IloRange rng = this.ilpModel.addRange(0.0, 0.0, constraint);
					cstrNum++;
					//keep track of the constraint
					this.flowConstraint.put(new Cstr5(chainIndex,nfvNode,funcIndex,funcInst), rng);
				}
				//check if source vertex is not NFV
				if(nonNfvNodes.contains(srcVrt)){
					String constraint = "cstr5" + "_" + cstrNum;
					IloRange rng = this.ilpModel.addRange(0.0, 0.0, constraint);
					cstrNum++;
					//keep track of the constraint
					this.flowConstraint.put(new Cstr5(chainIndex,srcVrt,funcIndex,funcInst), rng);
				}
				//check if destination vertex is not NFV
				if(nonNfvNodes.contains(destVrt)){
					String constraint = "cstr5" + "_" + cstrNum;
					IloRange rng = this.ilpModel.addRange(0.0, 0.0, constraint);
					cstrNum++;
					//keep track of the constraint
					this.flowConstraint.put(new Cstr5(chainIndex,destVrt,funcIndex,funcInst), rng);
				}
			}
		}
		System.out.println("Constraint (5) generated!");
		
		cstrNum=0;
		//No Placement Flow Constraint
		this.noPlacFlowConstraint = new HashMap<Cstr6,IloRange>();
		for(int chainIndex : modelChains.keySet()){
			int srcVrtID = modelChains.get(chainIndex).v1.get_id();
			int destVrtID = modelChains.get(chainIndex).v2.get_id();
			for(int funcIndex=0; funcIndex<modelChains.get(chainIndex).funcSeq.size()-1; funcIndex++){
				for(BaseVertex nonNfvNode :  nonNfvNodes){
					//add constraint if not source or destination
					if( (nonNfvNode.get_id()!=srcVrtID) && (nonNfvNode.get_id()!=destVrtID) ){
						String constraint = "cstr6" + "_" + cstrNum;
						IloRange rng = this.ilpModel.addRange(0.0, 0.0, constraint);
						cstrNum++;
						//keep track of the constraint
						this.noPlacFlowConstraint.put(new Cstr6(chainIndex,nonNfvNode,funcIndex), rng);
					}
				}
			}
		}
		System.out.println("Constraint (6) generated!");
		
		cstrNum=0;
		//VNF Replica constraint - (7)/1
		this.vnfReplicaConstraint1 = new HashMap<VarX,IloRange>();
		for(int funcID : InputConstants.FUNCTION_LIST){
			for(BaseVertex nfvNode : nfvNodes){
				String constraint = "cstr71" + "_" + cstrNum;
				IloRange rng = this.ilpModel.addRange(-Double.MAX_VALUE, 0.0, constraint);
				cstrNum++;
				//keep track of constraint
				this.vnfReplicaConstraint1.put(new VarX(nfvNode,funcID), rng);
			}
		}
		System.out.println("Constraint (7)/1 generated!");
		
		cstrNum=0;
		//VNF Replica constraint - (7)/2
		this.vnfReplicaConstraint2 = new HashMap<VarX,IloRange>();
		for(int funcID : InputConstants.FUNCTION_LIST){
			for(BaseVertex nfvNode : nfvNodes){
				String constraint = "cstr72" + "_" + cstrNum;
				IloRange rng = this.ilpModel.addRange(0.0, Double.MAX_VALUE, constraint);
				cstrNum++;
				//keep track of constraint
				this.vnfReplicaConstraint2.put(new VarX(nfvNode,funcID), rng);
			}
		}
		System.out.println("Constraint (7)/2 generated!");
		
		cstrNum=0;
		//VNF Replica Constraint - (8)
		this.vnfReplicaConstraint3 = new HashMap<Integer,IloRange>();
		for(int funcID : InputConstants.FUNCTION_LIST){
			String constraint = "cstr8" + "_" + cstrNum;
			IloRange rng = this.ilpModel.addRange(-Double.MAX_VALUE, 
					InputConstants.FUNCTION_REPLICA_COUNT.get(funcID), constraint);
			cstrNum++;
			//keep track of constraint
			this.vnfReplicaConstraint3.put(funcID, rng);
		}
		System.out.println("Constraint (8) generated!");
		
		cstrNum=0;
		//VNF Location Constraint - (9)/1
		this.vnfLocationConstraint1 = new HashMap<BaseVertex,IloRange>();
		for(BaseVertex nfvNode : nfvNodes){
			String constraint = "cstr91" + "_" + cstrNum;
			IloRange rng = this.ilpModel.addRange(-Double.MAX_VALUE,0.0,constraint);
			cstrNum++;
			//keep track of constraint
			this.vnfLocationConstraint1.put(nfvNode, rng);
		}
		System.out.println("Constraint (9)/1 generated!");
		
		cstrNum=0;
		//VNF Location Constraint - (9)/2
		this.vnfLocationConstraint2 = new HashMap<BaseVertex,IloRange>();
		for(BaseVertex nfvNode : nfvNodes){
			String constraint = "cstr92" + "_" + cstrNum;
			IloRange rng = this.ilpModel.addRange(0.0,Double.MAX_VALUE,constraint);
			cstrNum++;
			//keep track of constraint
			this.vnfLocationConstraint2.put(nfvNode, rng);
		}
		System.out.println("Constraint (9)/2 generated!");
		
		cstrNum=0;
		//Location Constraint - (10)
		this.vnfLocationConstraint3 = this.ilpModel.addRange(-Double.MAX_VALUE, InputConstants.NUMBER_OF_NODES_USED,"cstr10");
		System.out.println("Constraint 10 generated");
		
		cstrNum=0;
		//Core capacity constraint - (11)
		this.coreCapacityConstraint = new HashMap<BaseVertex,IloRange>();
		for(BaseVertex nfvNode : nfvNodes){
			String constraint = "cstr11" + "_" + cstrNum;
			IloRange rng = this.ilpModel.addRange(0.0, InputConstants.CPU_CORES_AT_NODE,constraint);
			cstrNum++;
			//keep track of constraint
			this.coreCapacityConstraint.put(nfvNode,rng);
		}
		System.out.println("Constraint 11 generated!");
		
		cstrNum=0;
		//Link capacity constraint - (12)
		this.linkCapacityConstraint = new HashMap<NodePair,IloRange>();
		for(BaseVertex srcVrt : g.get_vertex_list()){
			for(BaseVertex tarVrt : g.get_adjacent_vertices(srcVrt)){
				String constraint = "cstr12" + "_" + cstrNum;
				IloRange rng = this.ilpModel.addRange(0.0, InputConstants.BANDWIDTH_PER_LAMBDA, constraint);
				cstrNum++;
				//keep track of constraint
				this.linkCapacityConstraint.put(new NodePair(srcVrt,tarVrt), rng);
			}
		}
		System.out.println("Constraint 12 generated!");
		
		cstrNum=0;
		//Latency constraint - (13)
		this.latencyConstaint =  new HashMap<Integer,IloRange>();
		for(int chainIndex : modelChains.keySet()){
			TrafficPairChain tnPrSc = modelChains.get(chainIndex);
			int sVrtId = tnPrSc.v1.get_id();
			int dVrtId = tnPrSc.v2.get_id();
			double latencyThreshold = 0;
			int controlScId = modelChains.get(chainIndex).controlScId;
			int appId = modelChains.get(chainIndex).appId;
			if(controlScId == 0){
				latencyThreshold = InputConstants.APP_LATENCY.get(appId);
			}else{
				//GBR if Voice or Video(Live Streaming)
				if( (appId==2) || (appId==3) ){
					latencyThreshold = InputConstants.APP_LATENCY.get(appId) + InputConstants.CONTROL_PLANE_LATENCY_GBR;
				}else{				
					latencyThreshold = InputConstants.APP_LATENCY.get(appId) + InputConstants.CONTROL_PLANE_LATENCY_NON_GBR;
				}
			}
			//calculate processing latency
			for(int funcIndex=0; funcIndex < tnPrSc.funcSeq.size(); funcIndex++){
				int fId = tnPrSc.funcSeq.get(funcIndex).functionID;
				if( (fId!=sVrtId) && (fId!=dVrtId) ){
					double betaFactor = tnPrSc.funcSeq.get(funcIndex-1).betaTrafPerc;
					latencyThreshold -= betaFactor*tnPrSc.flowTraffic*InputConstants.PROCESSING_LATENCY;
				}
			}
			String constraint = "cstr13" + "_" + cstrNum;
			IloRange rng = this.ilpModel.addRange(0.0, latencyThreshold, constraint);
			cstrNum++;
			//keep track of constraint
			this.latencyConstaint.put(chainIndex, rng);
		}
		System.out.println("Constraint 13 generated!");
		
		
		
		
		
		//ADD THE VARIABLES
		//Columns for variable x_{vi}^{cj}
		varXcjvi = new HashMap<VarXc,IloColumn>();
		//Handles for variable x_{vi}^{cj}
		this.usedVarXc = new HashMap<VarXc,IloNumVar>();
		//VNF Placement Constraint - (4)
		for(Map.Entry<Cstr4, IloRange> entryCstr : this.vnfPlacementConstraint.entrySet()){
			for(BaseVertex nfvNode : nfvNodes){
				//create key to keep track of column
				VarXc tempKey = new VarXc(entryCstr.getKey().chainIndex,nfvNode,entryCstr.getKey().funcIndex,entryCstr.getKey().funcInst);
				//add the column to the constraint
				IloColumn col = this.ilpModel.column(entryCstr.getValue(), 1.0);
				//keep track of column
				varXcjvi.put(tempKey, col);
			}
		}
		//Flow Constraint - (5)
		for(Map.Entry<Cstr5, IloRange> entryCstr : this.flowConstraint.entrySet()){
			//create key to keep track of column
			VarXc tempKey1 = new VarXc(entryCstr.getKey().chainIndex,entryCstr.getKey().v,entryCstr.getKey().funcIndex,entryCstr.getKey().funcInst);
			//get the column corresponding to key
			IloColumn col1 = varXcjvi.get(tempKey1);
			//create key to keep track of column
			int nxtFuncIndex = entryCstr.getKey().funcIndex+1;
			int nxtFuncInstance = modelChains.get(entryCstr.getKey().chainIndex).funcSeq.get(nxtFuncIndex).instanceID;
			VarXc tempKey2 = new VarXc(entryCstr.getKey().chainIndex,entryCstr.getKey().v,nxtFuncIndex,nxtFuncInstance);
			//get the column corresponding to key
			IloColumn col2 = varXcjvi.get(tempKey2);
			if(col1 != null){
				//add the column to the constraint
				col1 = col1.and(this.ilpModel.column(entryCstr.getValue(), -1.0));
				//update column hashmap
				varXcjvi.put(tempKey1, col1);
			}else{
				//add the column to the constraint
				col1 = this.ilpModel.column(entryCstr.getValue(), -1.0);
				//keep track of column
				varXcjvi.put(tempKey1, col1);
			}
			if(col2 != null){
				//add the column to the constraint
				col2 = col2.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
				//update column hashmap
				varXcjvi.put(tempKey2, col2);
			}else{
				//add the column to the constraint
				col2 = this.ilpModel.column(entryCstr.getValue(), 1.0);
				//keep track of column
				varXcjvi.put(tempKey2, col2);
			}
		}
		//VNF Replica Constraint - (7)/1
		for(Map.Entry<VarX,IloRange> entryCstr : this.vnfReplicaConstraint1.entrySet()){
			int fId = entryCstr.getKey().f_id;
			BaseVertex nfvNode = entryCstr.getKey().v;
			//iterate through the model chains
			for(Map.Entry<Integer, TrafficPairChain> entry : modelChains.entrySet()){
				for(int funcIndex=0; funcIndex<entry.getValue().funcSeq.size(); funcIndex++){
					if(fId == entry.getValue().funcSeq.get(funcIndex).functionID){
						//create key to keep track of column
						VarXc tempKey = new VarXc(entry.getKey(),nfvNode,funcIndex,entry.getValue().funcSeq.get(funcIndex).instanceID);
//						String xName = "X_SC" + tempKey.scID + "_Nod" + tempKey.v.get_id() + "_VnfSQ" + tempKey.funcIndex + "_VnfInst" + tempKey.funcInst;
//						System.out.println(xName);
						//get the column corresponding to key
						IloColumn col = varXcjvi.get(tempKey);
						//add the column to the constraint
						col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
						//update column hashmap
						varXcjvi.put(tempKey, col);
					}
				}
			}
		}
		//VNF Replica Constraint - (7)/2
		for(Map.Entry<VarX,IloRange> entryCstr : this.vnfReplicaConstraint2.entrySet()){
			int fId = entryCstr.getKey().f_id;
			BaseVertex nfvNode = entryCstr.getKey().v;
			//iterate through the model chains
			for(Map.Entry<Integer, TrafficPairChain> entry : modelChains.entrySet()){
				for(int funcIndex=0; funcIndex<entry.getValue().funcSeq.size(); funcIndex++){
					if(fId == entry.getValue().funcSeq.get(funcIndex).functionID){
						//create key to keep track of column
						VarXc tempKey = new VarXc(entry.getKey(),nfvNode,funcIndex,entry.getValue().funcSeq.get(funcIndex).instanceID);
						//get the column corresponding to key
						IloColumn col = varXcjvi.get(tempKey);
						//add the column to the constraint
						col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
						//update column hashmap
						varXcjvi.put(tempKey, col);
					}
				}
			}
		}
		//CPU Core Constraint - (11)
		for(Map.Entry<BaseVertex, IloRange> entryCstr : this.coreCapacityConstraint.entrySet()){
			for(TrafficPairChain tnPrSc : modelChains.values()){
				int sVrtId = tnPrSc.v1.get_id();
				int dVrtId = tnPrSc.v2.get_id();
				for(int funcIndex=0; funcIndex < tnPrSc.funcSeq.size(); funcIndex++){
					//check whether source or destination function
					int fId = tnPrSc.funcSeq.get(funcIndex).functionID;
					if( (fId!=sVrtId) && (fId!=dVrtId)){
						//beta factor
						double betaFactor = tnPrSc.funcSeq.get(funcIndex-1).betaTrafPerc;
						//create key to keep track of column
						VarXc tempKey = new VarXc(tnPrSc.chainIndex,entryCstr.getKey(),funcIndex,tnPrSc.funcSeq.get(funcIndex).instanceID);
						//get the column corresponding to key
						IloColumn col = varXcjvi.get(tempKey);
						//add the column to the constraint
						col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0*tnPrSc.flowTraffic*betaFactor*InputConstants.CORE_CONSUMPTION_PER_NODE));
						//update column HashMap
						varXcjvi.put(tempKey, col);
					}
				}
			}			
		}
		//Add x_{vi}^{cj} variables to the model
		//Include the range for varXcjvi variables
		for(Map.Entry<VarXc, IloColumn> entryVar : varXcjvi.entrySet()){
			BaseVertex hostNode = entryVar.getKey().v;
			int fId = modelChains.get(entryVar.getKey().scID).funcSeq.get(entryVar.getKey().funcIndex).functionID;
			String xName = "X_SC" + entryVar.getKey().scID + "_Nod" + entryVar.getKey().v.get_id() + "_VnfSQ" + entryVar.getKey().funcIndex + "_VnfInst" + entryVar.getKey().funcInst;
		  	//source function on source node; destination function on destination node
			if(hostNode.get_id() == fId){
				this.usedVarXc.put(entryVar.getKey(),this.ilpModel.intVar(entryVar.getValue(), 1, 1, xName));
		  	}else if(nonNfvNodes.contains(hostNode)){//if EPC function on Non-NFV Node
		  		this.usedVarXc.put(entryVar.getKey(),this.ilpModel.intVar(entryVar.getValue(), 0, 0, xName));
		  	}else{			
		  		this.usedVarXc.put(entryVar.getKey(),this.ilpModel.intVar(entryVar.getValue(), 0, 1, xName));
		  	}
		}
		//deallocate X variables
		varXcjvi.clear();
		System.out.println("######## Variable Xc has been added to ILP model! ##########");
		
		
		
		
		
		
		
		
		
		
		
		//Columns for variables x_{vf}
		varXvf = new HashMap<VarX,IloColumn>();
		//Handles for variables x_{vf}
		this.usedVarX = new HashMap<VarX,IloNumVar>();
		//VNF Replica Constraint - (7)/1
		for(Map.Entry<VarX,IloRange> entryCstr : this.vnfReplicaConstraint1.entrySet()){		
			//add the column to the constraint
			IloColumn col = this.ilpModel.column(entryCstr.getValue(), -1.0*InputConstants.Big_M);
			//keep track of column
			varXvf.put(entryCstr.getKey(), col);			
		}
		//VNF Replica Constraint - (7)/2
		for(Map.Entry<VarX,IloRange> entryCstr : this.vnfReplicaConstraint2.entrySet()){			
			//get the column corresponding to key
			IloColumn col = varXvf.get(entryCstr.getKey());
			//add the column to the constraint
			col = col.and(this.ilpModel.column(entryCstr.getValue(), -1.0));
			//update column HashMap
			varXvf.put(entryCstr.getKey(), col);			
		}
		//VNF Replica Constraint - (8)
		for(Map.Entry<Integer,IloRange> entryCstr : this.vnfReplicaConstraint3.entrySet()){
			for(BaseVertex nfvNode : nfvNodes){
				//create key to find corresponding column
				VarX tempKey = new VarX(nfvNode,entryCstr.getKey());
				//get the column
				IloColumn col = varXvf.get(tempKey);
				//add column to constraint
				col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
				//update column HashMap
				varXvf.put(tempKey, col);
			}
		}
		//VNF Location Constraint - (9)/1
		for(Map.Entry<BaseVertex, IloRange> entryCstr : this.vnfLocationConstraint1.entrySet()){
			for(int fID : InputConstants.FUNCTION_LIST){
				//create key to find corresponding column
				VarX tempKey = new VarX(entryCstr.getKey(),fID);
				//get the column
				IloColumn col = varXvf.get(tempKey);
				//add column to constraint
				col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
				//update column HashMap
				varXvf.put(tempKey, col);				
			}
		}
		//VNF Location Constraint - (9)/2
		for(Map.Entry<BaseVertex, IloRange> entryCstr : this.vnfLocationConstraint2.entrySet()){
			for(int fID : InputConstants.FUNCTION_LIST){
				//create key to find corresponding column
				VarX tempKey = new VarX(entryCstr.getKey(),fID);
				//get the column
				IloColumn col = varXvf.get(tempKey);
				//add column to constraint
				col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
				//update column HashMap
				varXvf.put(tempKey, col);
			}
		}
		//Add x_{vf} variables to the model
		//Include the range for varXvf variables
		for(Map.Entry<VarX, IloColumn> entryVar : varXvf.entrySet()){
			 String xName = "X_Nod" + entryVar.getKey().v.get_id() + "_Vnf" + entryVar.getKey().f_id;
		  	 this.usedVarX.put(entryVar.getKey(),this.ilpModel.intVar(entryVar.getValue(), 0, 1, xName));
		}
		//deallocate X variables
		varXvf.clear();
		System.out.println("######## Variable X has been added to ILP model! ##########");
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		//Columns for variables y_{il}^{c}
		varYcil = new HashMap<VarY,IloColumn>();
		//Handles for variable y_{il}^{c}
		this.usedVarY = new HashMap<VarY,IloNumVar>();
		//Flow Constraint - (5)
		for(Map.Entry<Cstr5, IloRange> entryCstr : this.flowConstraint.entrySet()){			
			//outgoing links
			for(BaseVertex tVrt : g.get_adjacent_vertices(entryCstr.getKey().v)){
				//create key for column
				VarY tempKey = new VarY(entryCstr.getKey().chainIndex, entryCstr.getKey().funcIndex,entryCstr.getKey().v,tVrt);
				//get the column
				IloColumn col = varYcil.get(tempKey);
				if(col!=null){
					//add column to constraint
					col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
					//update HashMap
					varYcil.put(tempKey, col);
				}else{
					//generate column
					col = this.ilpModel.column(entryCstr.getValue(), 1.0);
					//add to HasMap
					varYcil.put(tempKey, col);
				}
			}
			//incoming links
			for(BaseVertex sVrt : g.get_precedent_vertices(entryCstr.getKey().v)){
				//create key for column
				VarY tempKey = new VarY(entryCstr.getKey().chainIndex, entryCstr.getKey().funcIndex,sVrt,entryCstr.getKey().v);
				//get the column
				IloColumn col = varYcil.get(tempKey);
				if(col!=null){
					//add column to constraint
					col = col.and(this.ilpModel.column(entryCstr.getValue(), -1.0));
					//update HashMap
					varYcil.put(tempKey, col);
				}else{
					//generate column
					col = this.ilpModel.column(entryCstr.getValue(), -1.0);
					//add to HasMap
					varYcil.put(tempKey, col);
				}
			}						
		}
		//No Placement Flow Constraint - (6)
		for(Map.Entry<Cstr6, IloRange> entryCstr : this.noPlacFlowConstraint.entrySet()){			
			//outgoing links
			for(BaseVertex tVrt : g.get_adjacent_vertices(entryCstr.getKey().v)){
				//create key for column
				VarY tempKey = new VarY(entryCstr.getKey().chainIndex, entryCstr.getKey().funcIndex,entryCstr.getKey().v,tVrt);
				//get the column
				IloColumn col = varYcil.get(tempKey);
				if(col!=null){
					//add column to constraint
					col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0));
					//update HashMap
					varYcil.put(tempKey, col);
				}else{
					//generate column
					col = this.ilpModel.column(entryCstr.getValue(), 1.0);
					//add to HasMap
					varYcil.put(tempKey, col);
				}
			}
			//incoming links
			for(BaseVertex sVrt : g.get_precedent_vertices(entryCstr.getKey().v)){
				//create key for column
				VarY tempKey = new VarY(entryCstr.getKey().chainIndex, entryCstr.getKey().funcIndex,sVrt,entryCstr.getKey().v);
				//get the column
				IloColumn col = varYcil.get(tempKey);
				if(col!=null){
					//add column to constraint
					col = col.and(this.ilpModel.column(entryCstr.getValue(), -1.0));
					//update HashMap
					varYcil.put(tempKey, col);
				}else{
					//generate column
					col = this.ilpModel.column(entryCstr.getValue(), -1.0);
					//add to HasMap
					varYcil.put(tempKey, col);
				}
			}			
		}
		//Flow Capacity Constraint - (12)
		for(Map.Entry<NodePair, IloRange> entryCstr : this.linkCapacityConstraint.entrySet()){
			for(Map.Entry<Integer, TrafficPairChain> entryChain : modelChains.entrySet()){
				for(int funcIndex=0; funcIndex<entryChain.getValue().funcSeq.size()-1; funcIndex++){
					//beta factor
					double betaFactor = entryChain.getValue().funcSeq.get(funcIndex).betaTrafPerc;
					//create key for column
					VarY tempKey = new VarY(entryChain.getKey(), funcIndex, entryCstr.getKey().v1, entryCstr.getKey().v2);
					//get the column
					IloColumn col = varYcil.get(tempKey);
					//add column to constraint
					col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0*entryChain.getValue().flowTraffic*betaFactor));
					//update HashMap
					varYcil.put(tempKey, col);
				}
			}
		}
		//Latency Constraint - (13)
		for(Map.Entry<Integer, IloRange> entryCstr : this.latencyConstaint.entrySet()){
			for(BaseVertex vrt : g._vertex_list){
				for(BaseVertex tVrt : g.get_adjacent_vertices(vrt)){
					for(int funcIndex=0; funcIndex<modelChains.get(entryCstr.getKey()).funcSeq.size()-1; funcIndex++){
						//create key for column
						VarY tempKey = new VarY(entryCstr.getKey(), funcIndex, vrt, tVrt);
						//get the column
						IloColumn col = varYcil.get(tempKey);
						//add column to constraint
						col = col.and(this.ilpModel.column(entryCstr.getValue(), 1.0*(g.get_edge_length(vrt, tVrt)/InputConstants.SPEED_OF_LIGHT)));
						//update HashMap
						varYcil.put(tempKey, col);
					}
				}
			}
		}
		//ADD OBJECTIVE TO THE PROBLEM
		for(Map.Entry<Integer, TrafficPairChain> entryChain : modelChains.entrySet()){
			for(BaseVertex vrt : g._vertex_list){
				for(BaseVertex tVrt : g.get_adjacent_vertices(vrt)){
					for(int funcIndex=0; funcIndex < entryChain.getValue().funcSeq.size()-1; funcIndex++){
						//beta factor
						double betaFactor = entryChain.getValue().funcSeq.get(funcIndex).betaTrafPerc;
						//create key for column
						VarY tempKey = new VarY(entryChain.getKey(), funcIndex, vrt, tVrt);
						//get the column
						IloColumn col = varYcil.get(tempKey);
						//add column to constraint
						col = col.and(this.ilpModel.column(this.bwUsed, 1.0*entryChain.getValue().flowTraffic*betaFactor));
						//update HashMap
						varYcil.put(tempKey, col);
					}					
				}				
			}			
		}		
		//Add y_{il}^{c} variables to the model
		//Include the range for varYcil variables
		for(Map.Entry<VarY, IloColumn> entryVar : varYcil.entrySet()){
			String yName = "Y_SC"+ entryVar.getKey().scId + "_Ind" + entryVar.getKey().funcIndex + "_Ls" +  entryVar.getKey().sVrt.get_id() + "_Ld" + entryVar.getKey().tVrt.get_id();
			this.usedVarY.put(entryVar.getKey(), this.ilpModel.intVar(entryVar.getValue(),0,1,yName));
		}
		//deallocate Y variables
		varYcil.clear();
		System.out.println("######## Variable y has been added to ILP model! ##########");
		
		
		
		
		
		
		//Columns for variables h_v
		varHv = new HashMap<VarH,IloColumn>();
		//Handles for variable h_{v}
		this.usedVarH = new HashMap<VarH,IloNumVar>();
		//VNF Location Constraint - (9)/1
		for(Map.Entry<BaseVertex, IloRange> entryCstr : this.vnfLocationConstraint1.entrySet()){
			//create key for the column
			VarH tempKey =  new VarH(entryCstr.getKey());
			//create column for the variable
			IloColumn col = this.ilpModel.column(entryCstr.getValue(), -1.0*InputConstants.Big_M);
			//keep track of the column
			varHv.put(tempKey, col);
		}
		//VNF Location Constraint - (9)/2
		for(Map.Entry<BaseVertex, IloRange> entryCstr : this.vnfLocationConstraint2.entrySet()){
			//create key for the column
			VarH tempKey =  new VarH(entryCstr.getKey());
			//get the col for the variable
			IloColumn col = varHv.get(tempKey);
			//add column to the constraint
			col = col.and(this.ilpModel.column(entryCstr.getValue(), -1.0));
			//keep track of the column
			varHv.put(tempKey, col);
		}
		//Location Constraint - (10)
		for(BaseVertex nfvNode : nfvNodes){
			//create key for the column
			VarH tempKey =  new VarH(nfvNode);
			//get the col for the variable
			IloColumn col = varHv.get(tempKey);
			//add column to the constraint
			col = col.and(this.ilpModel.column(this.vnfLocationConstraint3, 1.0));
			//keep track of the column
			varHv.put(tempKey, col);
		}
		//Add h_v variables to the model
		//Include the range for varHv variables
		for(Map.Entry<VarH, IloColumn> entryVar : varHv.entrySet()){
			 String hName = "H_Nod" + entryVar.getKey().node.get_id();
		  	 this.usedVarH.put(entryVar.getKey(),this.ilpModel.intVar(entryVar.getValue(), 0, 1, hName));
		}
		//deallocate H variables
		varHv.clear();
		System.out.println("######## Variable h has been added to ILP model! ##########");
	}
	
}
