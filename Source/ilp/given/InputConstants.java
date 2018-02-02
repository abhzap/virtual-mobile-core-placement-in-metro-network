package ilp.given;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputConstants {
	
	
	public static final String SD_PAIRS = "TrafficFlow1.txt";
	public static final Double CONTROL_PLANE_FRACTION = 0.05;
	public static final Integer NUMBER_OF_NODES_USED = 6;
	public static final Map<Integer, Integer> FUNCTION_REPLICA_COUNT = new HashMap<Integer, Integer>(){{
		    put(100,1); 
		    put(101,1);
		    put(102,1);
		    put(103,1);
		    put(104,1);
	    }};
	public static final Integer CPU_CORES_AT_NODE = 2300;//in terms of Gbps //maximum possible CPU cores (2300)
	public static final int BANDWIDTH_PER_LAMBDA = 100;//in Gbps
	public static final List<Integer> ulScIds = Arrays.asList(1,2); //check in ChainSet for these values
	public static final List<Integer> dlScIds = Arrays.asList(3,4); //check in ChainSet for these values
	public static final Double TOTAL_BUSY_HOUR_TONNAGE = 224.0;//in terms of Gbps
	public static final Double UPLOAD_FRACTION = 0.2;
	public static final Double DOWNLOAD_FRACTION = 0.8;	
	public static final Double CORE_CONSUMPTION_PER_NODE = 2.0;//in term of Gbps (2 CPUs per Gbps)
	public static final Double PROCESSING_LATENCY = .132;//.132ms for 1 Gbps	
	public static final int CONTROL_PLANE_LATENCY_NON_GBR = 500;//in ms//for default bearer
	public static final int CONTROL_PLANE_LATENCY_GBR = 250;//in ms//for dedicated bearer
	public static final ArrayList<Integer> AGGREGATION_NODE_LIST = new ArrayList<Integer>(){{
		add(6);
		add(7);
		add(8);
		add(9);
		add(10);
		add(11);
		add(12);
		add(13);
		add(14);
		add(15);
		add(16);		
	}};
	public static final List<Integer> NFV_NODE_LIST = Arrays.asList(1,2,4,5,6,8,10,11,13,16,17,18,19);
	/*1 Non-real-time-application (web, email etc.)
	2 VoIP
	3 Video (Live Streaming)
	4 Video (Progressive Video)
	5 Medial Downloads (FTP, P2P sharing, progressive download)*/
	public static final Map<Integer, Double> APP_LATENCY = new HashMap<Integer,Double>(){{
		put(1,300.0);
		put(2,100.0);
		put(3,150.0);
		put(4,300.0);
		put(5,300.0);
		}};	
	public static final Map<Integer, Double> APP_TRAFF_FRACTION = new HashMap<Integer,Double>(){{
		put(1,.095);
		put(2,.015);
		put(3,.046);
		put(4,.712);
		put(5,.133);
		}};	
	public static final List<Integer> FUNCTION_LIST = Arrays.asList(100,101,102,103,104);    
    public static final double SPEED_OF_LIGHT = 300;//in kilometers/millisecond
	public static final String CHAIN_SET = "ChainSet.txt";
	public static final String NETWORK_FILE_NAME = "metroCore1.txt";
	public static final int k_paths = 40;//atleast 40 paths to satisfy the connections	
	public static final double Big_M = 99999.0;//seems to be the optimal value for M
	public static final String FILE_READ_PATH = "Data/";	
	public static final String START_OF_FILE_DELIMETER = "START OF FILE";
	public static final String END_OF_FILE_DELIMETER = "END OF FILE";
	public static final String START_OF_NODES_DELIMETER = "START OF NODE";
	public static final String START_OF_LINKS_DELIMETER = "START OF LINK";	
	

		

	
	
		
	//to define the permissible negative values	
	/*public static final double RC_EPS = 1.0e-6;      
	public static final List<Double> TRAFFIC = Arrays.asList(1000.0);
	public static final List<Integer> CORE_NUM_LIST = Arrays.asList(4000);
	public static final boolean allPossiblePlcmnts = true;
    public static final int placementCount = 1;
	public static final double SEC_TO_MICROSEC = 1000000;	
	public static final String TRAFFIC_FILE = "traffic.txt";
	public static final String OUTPUT_VAR = "output_var";
	public static final String ILP_FILE_NAME = "14node_1_nfv";//"14node_dc_all_nfv","14node_dc_1_nfv"
	public static final String LaptopUserName = "Abhishek Gupta";
	public static final String PCUserName = "abgupta";
	public static final String ILP_WRITE_PATH = "C:/Users/" + PCUserName + "/Box Sync/Luna - Eclipse/PlaceVNF/FileLP/";	
		
	public static class ServiceDetails{		
		public Double connectionBandwidth;//in Mbps
		public Double totalTrafficPercentage;
		public ArrayList<Integer> setOfIDs;
		
		public ServiceDetails(Double connectionBandwidth, Double totalTrafficPercentage, int [] scIDs){			
			this.connectionBandwidth = connectionBandwidth;
			this.totalTrafficPercentage = totalTrafficPercentage;
			this.setOfIDs = new ArrayList<Integer>();
			for(int scID : scIDs){
				setOfIDs.add(scID);
			}
		}		
	}
	public static final Map<String,ServiceDetails> services = new HashMap<String,ServiceDetails>();
	public static void populateServices(){
		//connection bandwidth in Mbps
		String service = new String("web");
		int scIDs[] = new int[]{0,15,5,4};
		ServiceDetails detServ = new ServiceDetails(0.1,0.182,scIDs);
		services.put(service, detServ);
		
		service = new String("voip");
		scIDs = new int[]{1,16,7,6};
		detServ = new ServiceDetails(.064,.118,scIDs);
		services.put(service, detServ);
		
		service = new String("videostream");
		scIDs = new int[]{2,17,9,8};
		detServ = new ServiceDetails(4.0,0.698,scIDs);
		services.put(service, detServ);
		
		service = new String("cloudgame");
		scIDs = new int[]{3,18,11,10};
		detServ = new ServiceDetails(4.0,0.002,scIDs);
		services.put(service, detServ);
	}*/
}
