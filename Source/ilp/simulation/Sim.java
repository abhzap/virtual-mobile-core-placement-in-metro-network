package ilp.simulation;

public class Sim {

	public static void main(String args[]) throws Exception{
		
		//run the ILP
		//Test2.runIlp();
		//run the Replica vs BW tests
//		ReplicaVsBw.runIlp();
		//Per VNF Replica Vs. Bw
//		PerReplicaBandwidth.runIlp();
		//Per VNF Replica Vs. BW Vs. ControlPlaneFraction
		PerRepBwPerCt.runIlp();
		//to figure out max link to be used
//		MaxLinkBwVsReplica.runIlp();
	}
	
}
