package ilp.postprocess;

import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilp.model.NodePair;
import ilp.model.TrafficPairChain;
import ilp.model.VarXc;
import ilp.model.VarY;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class WriteResults {
	
	//find locations of VNFs and traffic load on each location
	public static void calculateTrafficLoadPerNode(BufferedWriter bw, IloCplex ilpProblem, Map<VarXc, IloNumVar> varXc, 
			Map<Integer,TrafficPairChain> modelChains) throws Exception{
		//Initialize VNF and BW Load scenario
		Map<Integer, Double> loadPerNode = new HashMap<Integer, Double>();
		//iterate through the X variables
		for(Map.Entry<VarXc, IloNumVar> entry : varXc.entrySet()){
			//check if non-zero
			if(ilpProblem.getValue(entry.getValue()) != 0){
				int scId = entry.getKey().scID;
				int funcIndex = entry.getKey().funcIndex;
				int fId = modelChains.get(scId).funcSeq.get(funcIndex).functionID;
				int vrtId = entry.getKey().v.get_id();				
				double traffic = modelChains.get(scId).flowTraffic;
				int srcVrt = modelChains.get(scId).v1.get_id();
				int destVrt = modelChains.get(scId).v2.get_id();			
				//check if not source and destination
				if( (fId!=srcVrt) && (fId!=destVrt) ){
					//add to map
					if(loadPerNode.get(vrtId) != null){
						double trafficAtNode = loadPerNode.get(vrtId) + traffic*modelChains.get(scId).funcSeq.get(funcIndex-1).betaTrafPerc;
						loadPerNode.put(vrtId, trafficAtNode);
					}else{
						loadPerNode.put(vrtId, traffic*modelChains.get(scId).funcSeq.get(funcIndex-1).betaTrafPerc);
					}
				}				
			}
		}
		//write to file
		String loadOnNodes = "( ";
		for(Map.Entry<Integer, Double> entry : loadPerNode.entrySet()){
			loadOnNodes += Integer.toString(entry.getKey()) + ":" + Double.toString(entry.getValue()) + " ; ";
			System.out.println("(" + entry.getKey() + ":" + entry.getValue() + ")");
		}
		loadOnNodes += " )";
		bw.write("\t\t" + loadOnNodes);
	}
	
	//find link with maximum bandwidth consumption
	public static void calculateMaxLoadedLink(BufferedWriter bw, IloCplex ilpProblem, Map<VarY, IloNumVar> varY, 
			Map<Integer,TrafficPairChain> modelChains, int itCnt, int repCnt) throws Exception{
		//sort HashMap based on keys
		TreeMap<Double, ArrayList<NodePair>> sortLinksByBw = new TreeMap<Double, ArrayList<NodePair>>();
		//Bandwidth used per link
		Map<NodePair, Double> bwUsedPerLink = new HashMap<NodePair, Double>();
		//check if non-zero
		for(Map.Entry<VarY,IloNumVar> entry : varY.entrySet()){
			if( ilpProblem.getValue(entry.getValue()) != 0 ){
				int scId = entry.getKey().scId;
				int funcIndex = entry.getKey().funcIndex;
				double traffic = modelChains.get(scId).flowTraffic*modelChains.get(scId).funcSeq.get(funcIndex).betaTrafPerc;
				NodePair np = new NodePair(entry.getKey().sVrt,entry.getKey().tVrt);
				if(bwUsedPerLink.get(np) != null){
					bwUsedPerLink.put(np, bwUsedPerLink.get(np) + traffic);
				}else{
					bwUsedPerLink.put(np, traffic);
				}
			}
		}
		//populate the TreeMap
		for(Map.Entry<NodePair, Double> entrySrt : bwUsedPerLink.entrySet()){
			if( sortLinksByBw.get(entrySrt.getValue()) != null){
				ArrayList<NodePair> npList = sortLinksByBw.get(entrySrt.getValue());
				npList.add(entrySrt.getKey());
				sortLinksByBw.put(entrySrt.getValue(), npList);
			}else{
				ArrayList<NodePair> npList= new ArrayList<NodePair>();
				npList.add(entrySrt.getKey());
				sortLinksByBw.put(entrySrt.getValue(), npList);
			}
		}
		//write to file the heaviest link
		bw.write("\t" + sortLinksByBw.lastKey() + ":");
		for(NodePair np : sortLinksByBw.get( sortLinksByBw.lastKey() )){
			bw.write("(" + np.v1.get_id() + "," + np.v2.get_id() + ");");
		}
		//write all link values to a file
		String fileName = "AllLinkBws_I" + itCnt + "_R" + repCnt + ".txt";
		BufferedWriter bwNxt = new BufferedWriter(new FileWriter(fileName));
		for(Map.Entry<Double, ArrayList<NodePair>> entry : sortLinksByBw.entrySet()){
			bwNxt.write( Double.toString(entry.getKey()) + ":\t");
			for(NodePair np : entry.getValue()){
				bwNxt.write("(" + np.v1.get_id() + "," + np.v2.get_id() +  ")");
			}
			bwNxt.write("\n");
		}
		bwNxt.close();
	}

}
