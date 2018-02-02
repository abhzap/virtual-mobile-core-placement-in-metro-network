package ilp.model;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Cstr6 {
	public int chainIndex;
	public BaseVertex v;
	public int funcIndex;
	
	public Cstr6(int chainIndex, BaseVertex v, int funcIndex){
		this.chainIndex = chainIndex;
		this.v = v;
		this.funcIndex = funcIndex;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			Cstr6 o = (Cstr6) obj;
			if( (this.chainIndex==o.chainIndex) && (this.v.get_id()==o.v.get_id())   
					&& (this.funcIndex==o.funcIndex) ){
				result = true;
			}
		}
	    return result;
	}
	@Override
	public int hashCode()
	{
	    return this.v.hashCode() + this.chainIndex + this.funcIndex;
	}
}
