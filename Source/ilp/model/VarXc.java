package ilp.model;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class VarXc {
	   public int scID;
	   public BaseVertex v;
	   public int funcIndex;
	   public int funcInst;
	   
	   public VarXc(int scID, BaseVertex v, int funcIndex, int funcInst){
		   this.scID = scID;
		   this.v = v; 
		   this.funcIndex = funcIndex;
		   this.funcInst = funcInst;
	   }
	   
	   @Override
	   public boolean equals(Object obj)
	   {
			boolean result = false;
			if(obj == null || obj.getClass() != getClass()){
				result = false;
			} else {
				VarXc o = (VarXc) obj;
				if( (this.scID==o.scID) && (this.funcIndex==o.funcIndex) && (this.funcInst==o.funcInst) && (this.v.get_id()==o.v.get_id()) ){
					result = true;
				}
			}
		    return result;
	    }	
		
		@Override
		public int hashCode()
		{
		    return this.v.hashCode() + this.funcIndex + this.funcInst + this.scID;
		}
}
