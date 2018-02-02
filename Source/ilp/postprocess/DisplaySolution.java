package ilp.postprocess;

import java.util.Map;

import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilp.model.TrafficPairChain;
import ilp.model.VarH;
import ilp.model.VarX;
import ilp.model.VarXc;
import ilp.model.VarY;

public class DisplaySolution {
	
	public static void showAllNonZeroVariables(IloCplex ilpModel, Map<VarH, IloNumVar> usedVarH, 
			Map<VarX, IloNumVar> usedVarX, Map<VarXc, IloNumVar> usedVarXc, Map<VarY, IloNumVar> usedVarY, Map<Integer,TrafficPairChain> modelChains) throws Exception{			
			System.out.println("\nILP Objective Value: " + ilpModel.getObjValue() + " ; EpGap(Rel): " + ilpModel.getMIPRelativeGap() + "\n");    
			//display H variables
			for(Map.Entry<VarH, IloNumVar> entryH : usedVarH.entrySet()){
				if( ilpModel.getValue(entryH.getValue()) != 0 ){
					System.out.println("H_Nod" + entryH.getKey().node.get_id() + " = " +  ilpModel.getValue(entryH.getValue()));
				}
			}
			//display Xvf variables
			for(Map.Entry<VarX, IloNumVar> entryX : usedVarX.entrySet()){
				if( ilpModel.getValue(entryX.getValue()) != 0 ){
					System.out.println("X_Nod" + entryX.getKey().v.get_id() + "_Vnf" + entryX.getKey().f_id + " = " + ilpModel.getValue(entryX.getValue()) );	
				}
			}
			//display Xcjvi variables //only function placements
			/*for(Map.Entry<VarXc, IloNumVar> entryXc : usedVarXc.entrySet()){
				if( ilpModel.getValue(entryXc.getValue()) != 0 ){
					TrafficPairChain tnPrSc = modelChains.get(entryXc.getKey().scID);
					int fId = tnPrSc.funcSeq.get(entryXc.getKey().funcIndex).functionID;
					//check if not source or destination
					if( (fId!=tnPrSc.v1.get_id()) && (fId!=tnPrSc.v2.get_id()) ){
						System.out.println( "X_SC" + entryXc.getKey().scID + "_Nod" + entryXc.getKey().v.get_id() + "_VnfSQ" + entryXc.getKey().funcIndex  + "_VnfInst" + entryXc.getKey().funcInst + " = " + ilpModel.getValue(entryXc.getValue()) );
					}
				}
			}*/
			//display Ycil variables
			/*for(Map.Entry<VarY, IloNumVar> entryY : usedVarY.entrySet()){
				if( ilpModel.getValue(entryY.getValue()) != 0 ){
					System.out.println("Y_SC"+ entryY.getKey().scId + "_Ind" + entryY.getKey().funcIndex + "_Ls" + 
							entryY.getKey().sVrt.get_id() + "_Ld" + entryY.getKey().tVrt.get_id() + " = " + ilpModel.getValue(entryY.getValue()));
				}
			}*/
	}

}
