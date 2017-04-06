package compiler.phase.codegen;

import compiler.data.imc.TEMP;

public class InterferenceEdge {
	
	public TEMP t1;
	public TEMP t2;
	
	public InterferenceEdge(TEMP t1, TEMP t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

}
