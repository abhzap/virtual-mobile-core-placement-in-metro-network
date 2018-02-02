package ilp.model;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Cstr5 {
	public int chainIndex;
	public BaseVertex v;
	public int funcIndex;
	public int funcInst;
	
	public Cstr5(int chainIndex, BaseVertex v, int funcIndex, int funcInst){
		this.chainIndex = chainIndex;
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
			Cstr5 o = (Cstr5) obj;
			if( (this.chainIndex==o.chainIndex) && (this.v.get_id()==o.v.get_id())   
					&& (this.funcIndex==o.funcIndex) && (this.funcInst==o.funcInst)){
				result = true;
			}
		}
	    return result;
	}
	@Override
	public int hashCode()
	{
	    return this.v.hashCode() + this.chainIndex + this.funcIndex + this.funcInst;
	}
}
