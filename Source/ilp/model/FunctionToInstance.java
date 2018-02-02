package ilp.model;

public class FunctionToInstance {
	public int functionID;
	public int instanceID;
	public double betaTrafPerc;
	
	public FunctionToInstance(int functionID, int instanceID){
		this.functionID = functionID;
		this.instanceID = instanceID;
		this.betaTrafPerc = 1.0;
	}
	
	public FunctionToInstance(int functionID, int instanceID, double betaTraPerc){
		this.functionID = functionID;
		this.instanceID = instanceID;
		this.betaTrafPerc = betaTraPerc;
	}
	
	public FunctionToInstance(FunctionToInstance fIDinsID){
		this.functionID = fIDinsID.functionID;
		this.instanceID = fIDinsID.instanceID;
		this.betaTrafPerc = fIDinsID.betaTrafPerc;
	}
}
