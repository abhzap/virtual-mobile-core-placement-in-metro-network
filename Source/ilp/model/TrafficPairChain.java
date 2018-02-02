package ilp.model;

import java.util.ArrayList;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class TrafficPairChain {
	public int chainIndex;//these chain Index start are all initialized to 0
	public BaseVertex v1;
	public BaseVertex v2;	
	public int controlScId;
	public int dataScId;
	public int appId;
	public double flowTraffic; //expressed in Mbps
	public ArrayList<FunctionToInstance> funcSeq;
	
	
	public TrafficPairChain(BaseVertex v1, BaseVertex v2, int controlScId, int dataScId, int appId){
		this.chainIndex = 0;		
		this.v1 = v1;
		this.v2 = v2;
		this.controlScId = controlScId;
		this.dataScId = dataScId;
		this.appId = appId;
		this.funcSeq = new ArrayList<FunctionToInstance>();
		this.flowTraffic = 0;		
	}
	
	public TrafficPairChain(BaseVertex v1, BaseVertex v2, int controlScId, int dataScId, int appId, double flowTraffic){
		this.chainIndex = 0;
		this.v1 = v1;
		this.v2 = v2;
		this.controlScId = controlScId;
		this.dataScId = dataScId;
		this.appId = appId;
		this.funcSeq = new ArrayList<FunctionToInstance>();
		this.flowTraffic = flowTraffic;		
	}
	
	//since TrafficNodes is used as a key in a HashMap
	@Override
	public boolean equals(Object obj){
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			TrafficPairChain o = (TrafficPairChain) obj;
			if( (this.chainIndex==o.chainIndex) && (this.v1.get_id()==o.v1.get_id()) && (this.v2.get_id()==o.v2.get_id()) && (this.controlScId==o.controlScId)
					&& (this.dataScId==o.dataScId) && (this.appId==o.appId)){
				result = true;
			}
		}
	    return result;		
	}	
	@Override
	public int hashCode(){
		return this.chainIndex + this.v1.hashCode() + this.v2.hashCode() + this.controlScId + this.dataScId + this.appId;			
	}
	
	public boolean equals(NodePair np){
		return (this.v1.get_id()==np.v1.get_id())&&(this.v2.get_id()==np.v2.get_id());
	}
	
	public boolean equals(TrafficPairChain tnSc){
		return (this.v1.get_id()==tnSc.v1.get_id())&&(this.v2.get_id()==tnSc.v2.get_id())&&(this.controlScId==tnSc.controlScId)&&
				(this.dataScId==tnSc.dataScId)&&(this.appId==tnSc.appId);
	}
	
}


