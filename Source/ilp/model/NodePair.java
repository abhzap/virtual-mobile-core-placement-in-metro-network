package ilp.model;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class NodePair {
	public BaseVertex v1;
	public BaseVertex v2;	
	
	public NodePair(){
		
	}
	
	public NodePair(BaseVertex v1, BaseVertex v2){
		this.v1 = v1;
		this.v2 = v2;
	}
	
	public boolean equals(NodePair p){
		return (this.v1.get_id()==p.v1.get_id())&&(this.v2.get_id()==p.v2.get_id());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			NodePair o = (NodePair) obj;
			if( (this.v1.get_id()==o.v1.get_id())&&(this.v2.get_id()==o.v2.get_id()) ){
				result = true;
			}
		}
	    return result;
	}	
		
	@Override
	public int hashCode()
	{
	    return this.v1.get_id() + this.v2.get_id();
	}
}
