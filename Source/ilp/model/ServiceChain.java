package ilp.model;

import java.util.ArrayList;

public class ServiceChain {
	public int chainIndex;
	public ArrayList<FunctionToInstance> chainSeq;
	public int chainSize;
	
	//create a new service chain
	public ServiceChain(int chainIndex, ArrayList<FunctionToInstance> chainSeq){
		this.chainIndex = chainIndex;
		this.chainSeq = new ArrayList<FunctionToInstance>(chainSeq);		
		this.chainSize = chainSeq.size();
	}
	
	public ArrayList<FunctionToInstance> getChainSeq(){
		return this.chainSeq;
	}
	
	public int getChainIndex(){
		return this.chainIndex;
	}	
	
}
