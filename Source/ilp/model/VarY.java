package ilp.model;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class VarY {
		public int scId;
		public int funcIndex; // function identifier //whether first or last VNF
		public BaseVertex sVrt; // source vertex
		public BaseVertex tVrt; // target vertex
	    
	    public VarY(int scId, int funcIndex, BaseVertex sVrt, BaseVertex tVrt ){
	    	this.scId = scId;
	    	this.funcIndex = funcIndex;	    	
	    	this.sVrt = sVrt;
	    	this.tVrt = tVrt;    	
	    }
	    
	    @Override
		public boolean equals(Object obj)
		{
			boolean result = false;
			if(obj == null || obj.getClass() != getClass()){
				result = false;
			} else {
				VarY o = (VarY) obj;
				if( (this.sVrt.get_id()==o.sVrt.get_id()) && (this.tVrt.get_id()==o.tVrt.get_id()) && (this.funcIndex==o.funcIndex) && (this.scId==o.scId)  ){
					result = true;
				}
			}
		    return result;
		}	

		@Override
		public int hashCode()
		{
		    return this.sVrt.hashCode() + this.tVrt.hashCode() + this.scId + this.funcIndex;
		}
	    
	    public boolean checkEquality(int scId, int funcIndex, BaseVertex sVrt, BaseVertex tVrt){
	    	if( (this.scId == scId) && (this.funcIndex==funcIndex) && (this.sVrt.get_id()==sVrt.get_id()) && (this.tVrt.get_id()==tVrt.get_id()) ){
	    		return true;
	    	}else{
	    		return false;
	    	}
	    }
}
