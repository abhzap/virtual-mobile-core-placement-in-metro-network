package ilp.model;


import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class VarH {

public BaseVertex node;
	
	public VarH(BaseVertex node){
		this.node = node;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			VarH o = (VarH) obj;
			if( this.node.get_id()==o.node.get_id() ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.node.hashCode();
	}
}
