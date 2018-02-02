package ilp.model;

public class Cstr4 {
	public int chainIndex;	
	public int funcIndex;
	public int funcInst; 
	
	public Cstr4(int chainIndex, int funcIndex, int funcInst){
		this.chainIndex = chainIndex;
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
			Cstr4 o = (Cstr4) obj;
			if( (this.chainIndex==o.chainIndex) && (this.funcIndex==o.funcIndex) 
					&& (this.funcInst==o.funcInst) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.chainIndex + this.funcIndex + this.funcInst;
	}
}
