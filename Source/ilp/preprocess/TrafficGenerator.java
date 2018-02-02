package ilp.preprocess;

import ilp.given.InputConstants;
import ilp.model.TrafficPairChain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import edu.asu.emit.qyan.alg.model.Graph;

public class TrafficGenerator {
	
	//generate traffic pairs chains
	public static void makePairChains(Graph g,  HashMap<Integer, ArrayList<Integer>> apnGws, ArrayList<TrafficPairChain> listOfTnPrSc, 
			int flowCount, double totalTraffic, int appId, int controlScId, int dataScId){		
		//calculate flow traffic
		double flowTraffic = totalTraffic/flowCount;
		ArrayList<Integer> appGwId = apnGws.get(appId);	
		Random rand = new Random(System.currentTimeMillis());
		//Upload
		if( (dataScId==1) || (dataScId==2) ){
			//iterate through the flows
			int flowIndex=0;
			while( flowIndex < flowCount ){
//				Random rand = new Random(System.currentTimeMillis());
				int srcVrtId = InputConstants.AGGREGATION_NODE_LIST.get(rand.nextInt(InputConstants.AGGREGATION_NODE_LIST.size())); //source vertex
				int dstVrtId = appGwId.get(rand.nextInt(appGwId.size())); //destination vertex
				//make sure that source and destination vertices are different!
				/*while(srcVrtId == dstVrtId){
					srcVrtId = InputConstants.AGGREGATION_NODE_LIST.get(rand.nextInt(InputConstants.AGGREGATION_NODE_LIST.size())); //source vertex
					dstVrtId = appGwId.get(rand.nextInt(appGwId.size())); //destination vertex
				}*/
				TrafficPairChain tnPrSc = new TrafficPairChain(g.get_vertex(srcVrtId),g.get_vertex(dstVrtId),controlScId,dataScId,appId);
				if(listOfTnPrSc.contains(tnPrSc)){
					listOfTnPrSc.get(listOfTnPrSc.indexOf(tnPrSc)).flowTraffic += flowTraffic;
				}else{
					tnPrSc.flowTraffic = flowTraffic;
					listOfTnPrSc.add(tnPrSc);
				}
				flowIndex++;
			}
		}else{//Download
			//iterate through the flows
			int flowIndex=0;
			while( flowIndex < flowCount ){
//				Random rand = new Random(System.currentTimeMillis());
				int srcVrtId = appGwId.get(rand.nextInt(appGwId.size())); 
				int dstVrtId = InputConstants.AGGREGATION_NODE_LIST.get(rand.nextInt(InputConstants.AGGREGATION_NODE_LIST.size()));  
				//make sure that source and destination vertices are different!
				/*while(srcVrtId == dstVrtId){
					srcVrtId = appGwId.get(rand.nextInt(appGwId.size()));
					dstVrtId = InputConstants.AGGREGATION_NODE_LIST.get(rand.nextInt(InputConstants.AGGREGATION_NODE_LIST.size()));
				}*/
				TrafficPairChain tnPrSc = new TrafficPairChain(g.get_vertex(srcVrtId),g.get_vertex(dstVrtId),controlScId,dataScId,appId);
				if(listOfTnPrSc.contains(tnPrSc)){
					listOfTnPrSc.get(listOfTnPrSc.indexOf(tnPrSc)).flowTraffic += flowTraffic;
				}else{
					tnPrSc.flowTraffic = flowTraffic;
					listOfTnPrSc.add(tnPrSc);
				}
				flowIndex++;
			}
		}
	}
	
	//generate traffic pair chains for voice (appId=2) and video (appId=3)
	public static void generateVoiceVideoPairs(Graph g, HashMap<Integer,Integer> uplinkNasEvents, HashMap<Integer,Integer> downlinkNasEvents,
			ArrayList<TrafficPairChain> listOfTnPrSc, HashMap<Integer, ArrayList<Integer>> apnGws){
		//both voice and video need a dedicated bearer ; Voice ; Video
		int [] appIds = new int [2]; appIds[0]=2; appIds[1]=3;
		int controlScId = 8; //ScId for Dedicated Bearer NAS
		//Voice and Video together
		Double totalPercnt = InputConstants.APP_TRAFF_FRACTION.get(2) + InputConstants.APP_TRAFF_FRACTION.get(3);
		
		//iterate through voice and video respectively
		for(int appId : appIds){	
			
			//Upload
			int dataScId = 1;
			Double flowCount = uplinkNasEvents.get(controlScId)*(InputConstants.APP_TRAFF_FRACTION.get(appId)/totalPercnt);			
			double totalTraffic = InputConstants.TOTAL_BUSY_HOUR_TONNAGE*InputConstants.UPLOAD_FRACTION*InputConstants.APP_TRAFF_FRACTION.get(appId);
			//only call for non-zero flow count
			if(flowCount.intValue() == 0){
				System.out.println("AppId: " + appId + " ; Upload Flow Count: " + 1);
				makePairChains(g, apnGws, listOfTnPrSc, 1, totalTraffic, appId, controlScId, dataScId);
			}else{
				System.out.println("AppId: " + appId + " ; Upload Flow Count: " + flowCount.intValue());
				makePairChains(g, apnGws, listOfTnPrSc, flowCount.intValue(), totalTraffic, appId, controlScId, dataScId);
			}
			
			//Download
			dataScId = 3;			
			flowCount = downlinkNasEvents.get(controlScId)*(InputConstants.APP_TRAFF_FRACTION.get(appId)/totalPercnt);			
			totalTraffic = InputConstants.TOTAL_BUSY_HOUR_TONNAGE*InputConstants.DOWNLOAD_FRACTION*InputConstants.APP_TRAFF_FRACTION.get(appId);
			//only call for non-zero flow count
			if(flowCount.intValue() == 0){
				System.out.println("AppId: " + appId + " ; Download Flow Count: " + 1);
				makePairChains(g, apnGws, listOfTnPrSc, 1, totalTraffic, appId, controlScId, dataScId);
			}else{
				System.out.println("AppId: " + appId + " ; Download Flow Count: " + flowCount.intValue());
				makePairChains(g, apnGws, listOfTnPrSc, flowCount.intValue(), totalTraffic, appId, controlScId, dataScId);
			}
		}
	}
	
	//generate traffic pair chains for Internet (appId=1), Video (appId=4) and Media (appId=5)
	public static void generateRemainingPairs(Graph g, int controlScId, HashMap<Integer,Integer> uplinkNasEvents, HashMap<Integer,Integer> downlinkNasEvents,
			ArrayList<TrafficPairChain> listOfTnPrSc, HashMap<Integer, ArrayList<Integer>> apnGws, int uploadFlowCount, int downloadFlowCount){
		//For Non-GBR //Internet, Video, Media downloads
		int [] appIds = new int [3]; appIds[0]=1; appIds[1]=4; appIds[2]=5;
		//Voice and Video together
		Double totalPercnt = InputConstants.APP_TRAFF_FRACTION.get(1) + InputConstants.APP_TRAFF_FRACTION.get(4) + InputConstants.APP_TRAFF_FRACTION.get(5);
	    
		System.out.println("Control ScId : " + controlScId);
		double uploadTrafficFraction = (double) uplinkNasEvents.get(controlScId)/uploadFlowCount;
		System.out.println("\tUpload Traffic Fraction : " + uploadTrafficFraction);
		double downloadTrafficFraction = (double) downlinkNasEvents.get(controlScId)/downloadFlowCount;
		System.out.println("\tDownload Traffic Fraction : " + downloadTrafficFraction);
		
		//iterate through voice and video respectively
		for(int appId : appIds){	
			
			//Upload
			int dataScId = 1;
			if(controlScId==7){
				dataScId = 2;
			}
			Double flowCount = uplinkNasEvents.get(controlScId)*(InputConstants.APP_TRAFF_FRACTION.get(appId)/totalPercnt);			
			double totalTraffic = InputConstants.TOTAL_BUSY_HOUR_TONNAGE*InputConstants.APP_TRAFF_FRACTION.get(appId)*InputConstants.UPLOAD_FRACTION*uploadTrafficFraction;
			//only call for non-zero flow count
			if(flowCount.intValue() == 0){
				System.out.println("\t\tAppId: " + appId + " ; Upload Flow Count: " + 1 + " ; Traffic: " + totalTraffic);
				makePairChains(g, apnGws, listOfTnPrSc, 1, totalTraffic, appId, controlScId, dataScId);
			}else{
				System.out.println("\t\tAppId: " + appId + " ; Upload Flow Count: " + flowCount.intValue() + " ; Traffic: " + totalTraffic);
				makePairChains(g, apnGws, listOfTnPrSc, flowCount.intValue(), totalTraffic, appId, controlScId, dataScId);
			}
			
			//Download
			dataScId = 3;
			if(controlScId==7){
				dataScId = 4;
			}
			flowCount = downlinkNasEvents.get(controlScId)*(InputConstants.APP_TRAFF_FRACTION.get(appId)/totalPercnt);			
			totalTraffic = InputConstants.TOTAL_BUSY_HOUR_TONNAGE*InputConstants.APP_TRAFF_FRACTION.get(appId)*InputConstants.DOWNLOAD_FRACTION*downloadTrafficFraction;
			
			//only call for non-zero flow count
			if(flowCount.intValue() == 0){
				System.out.println("\t\tAppId: " + appId + " ; Download Flow Count: " + 1  + " ; Traffic: " + totalTraffic);
				makePairChains(g, apnGws, listOfTnPrSc, 1, totalTraffic, appId, controlScId, dataScId);
			}else{
				System.out.println("\t\tAppId: " + appId + " ; Download Flow Count: " + flowCount.intValue()  + " ; Traffic: " + totalTraffic);
				makePairChains(g, apnGws, listOfTnPrSc, flowCount.intValue(), totalTraffic, appId, controlScId, dataScId);
			}
		}
	}

	//generate traffic pair chains
	public static ArrayList<TrafficPairChain> generateTrafficPairChains(Graph g, HashMap<Integer,Integer> flowsPerNasEvent,  
			HashMap<Integer,ArrayList<Integer>> apnGws){
		ArrayList<TrafficPairChain> listOfTnPrSc = new ArrayList<TrafficPairChain>();		
		HashMap<Integer,Integer> uplinkNasEvents = new HashMap<Integer,Integer>();
		HashMap<Integer,Integer> downlinkNasEvents = new HashMap<Integer,Integer>();
		//iterate through the NAS events
		for(Map.Entry<Integer, Integer> nasEvent : flowsPerNasEvent.entrySet()){
			//upload
			Double uplinkEvents = nasEvent.getValue()*InputConstants.UPLOAD_FRACTION;
			uplinkNasEvents.put(nasEvent.getKey(), uplinkEvents.intValue());
			System.out.println("Control ScId: " + nasEvent.getKey() + " ; UpLoad: " + uplinkEvents.intValue());
			//download
			Double downlinkEvents = nasEvent.getValue()*InputConstants.DOWNLOAD_FRACTION;
			downlinkNasEvents.put(nasEvent.getKey(), downlinkEvents.intValue());
			System.out.println("Control ScId: " + nasEvent.getKey() + " ; DownLoad: " + downlinkEvents.intValue());
		}	
		
		//allocate traffic for voice and video (live streaming) //Service request
		generateVoiceVideoPairs(g, uplinkNasEvents, downlinkNasEvents, listOfTnPrSc, apnGws);
		//Generate traffic Pair Chains for remaining applications and NAS events
		//NAS ID's //Attach (5), X2-based (6), S1-based (7), Pure Data Plane (0)
		int [] controlScIds = new int [4]; controlScIds[0]=5; controlScIds[1]=6; controlScIds[2]=7; controlScIds[3]=0;
//		int [] controlScIds = new int [4]; controlScIds[0]=0; 
		int uploadFlowCount = 0;
		int downloadFlowCount = 0;
		for(int controlScId : controlScIds){
			uploadFlowCount += uplinkNasEvents.get(controlScId);
			downloadFlowCount += downlinkNasEvents.get(controlScId); 
		}
		System.out.println("Upload Flow Count = " + uploadFlowCount);
		System.out.println("Download Flow Count = " + downloadFlowCount);
		for(int controlScId : controlScIds){
			generateRemainingPairs(g, controlScId, uplinkNasEvents, downlinkNasEvents, listOfTnPrSc, apnGws, uploadFlowCount, downloadFlowCount);
		}
		
		return listOfTnPrSc;
	}
		
	
}
