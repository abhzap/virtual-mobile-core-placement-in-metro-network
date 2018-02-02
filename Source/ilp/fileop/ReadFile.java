package ilp.fileop;

import ilp.given.InputConstants;
import ilp.model.FunctionToInstance;
import ilp.model.NodePair;
import ilp.model.ServiceChain;
import ilp.model.TrafficPairChain;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.asu.emit.qyan.alg.model.Path;



public class ReadFile {
	
	//read the Set of Service Chains
		//stream reading functions
		public static Map<Integer,ServiceChain> readChainSet(InputStream fileStream){		
			Map<Integer,ServiceChain> ChainSet = new HashMap<Integer,ServiceChain>();	
			int lineNum = 0;
			BufferedReader br = null;		
			String line_data;
			boolean read_vars = false;
		
					try{
						br = new BufferedReader(new InputStreamReader(fileStream));							
						//skip the comments in the file
						do{
							line_data = br.readLine();
							lineNum++;						
						}while(line_data != null
								&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
						//Error while reading file
						if (line_data == null) {
							System.err.println("ERROR: Incorrect file syntax at line number:"
											+ lineNum);
							
						}
						//Check that the end of file has not reached        	
						while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
							read_vars = true;							
							if(read_vars){
							    //index separated from VNF sequence
								String[] parts = line_data.split("\t");	
								//serviceChainId
								int scID = Integer.parseInt(parts[0]);
								//store the sequence of VNFs and their corresponding instances
							    ArrayList<FunctionToInstance> temp = new ArrayList<FunctionToInstance>();
							    for(int partIndex=1; partIndex<parts.length; partIndex++){
							    	String[] fIdinstId = parts[partIndex].split(",");
							    	FunctionToInstance objTemp = new FunctionToInstance( Integer.parseInt(fIdinstId[0]) , Integer.parseInt(fIdinstId[1]) );
							    	temp.add(objTemp);
							    }
							    System.out.println();
							    //add VNFs							    
								ChainSet.put(scID , new ServiceChain (scID, temp ) );						   							    
							}								
						}
						
						/*for(ServiceChain c : checkSet){
							for(Integer VNF : c.getChainSeq()){
								System.out.print(VNF + ", ");
							}
							System.out.println();
						}*/
						//close the buffered writer from writing the file
						br.close();				
					}catch(Exception exp){
						System.err.println("Error in reading the file");				
					}
			System.out.println("Set of service chains have been read");
			return ChainSet;		
		}
		
		//read the given s-d pairs and add the traffic flows to them
		//stream reading function
		public static ArrayList<TrafficPairChain> readSDPairs(InputStream fileStream, Map<NodePair, List<Path>> sdpaths){
			ArrayList<TrafficPairChain> sdTraffic = new ArrayList<TrafficPairChain>();
			int lineNum = 0;
			BufferedReader br = null;		
			String line_data;
			boolean read_vars = false;
		
					try{
						br = new BufferedReader(new InputStreamReader(fileStream));	
						//skip the comments in the file
						do{
							line_data = br.readLine();
							lineNum++;						
						}while(line_data != null
								&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
						//Error while reading file
						if (line_data == null) {
							System.err.println("ERROR: Incorrect file syntax at line number:"
											+ lineNum);
							
						}
						//Check that the end of file has not reached        	
						//Check that the end of file has not reached        	
						while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
							read_vars = true;							
							if(read_vars){
							    //value separated from variable
								String[] parts = line_data.split("\t");	
								int src_index = Integer.valueOf(parts[0]);							
								int dest_index = Integer.valueOf(parts[1]);	
								int controlScId = Integer.valueOf(parts[2]);
								int dataScId = Integer.valueOf(parts[3]);
								int appId = Integer.valueOf(parts[4]);
								int flowTraffic = Integer.valueOf(parts[5]);
								TrafficPairChain temp = null;
								for( Map.Entry<NodePair,List<Path>> entry : sdpaths.entrySet()){
									if(entry.getKey().v1.get_id()==src_index && entry.getKey().v2.get_id()==dest_index){
										temp = new TrafficPairChain(entry.getKey().v1, entry.getKey().v2, controlScId, dataScId, appId, flowTraffic);									
									}
								}											
								//add the TrafficNodes object to the list
								sdTraffic.add(temp);
							}
						}		
						//close the buffered writer from writing the file
						br.close();				
					}catch(Exception exp){
						System.err.println("Error in reading the file");				
					}
			System.out.println("Source Destination pairs have been read");
			return sdTraffic;
		}

}
