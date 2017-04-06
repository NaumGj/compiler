package compiler.phase.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class InterferenceGraph {

	public HashMap<Integer, HashSet<Integer>> graph;
	public EvalCodeGen codeGen;
	
	public InterferenceGraph(EvalCodeGen codeGen) {
		this.codeGen = codeGen;
		
		graph = new HashMap<Integer, HashSet<Integer>>();
		for(Integer temp : codeGen.regs) {
			addRegister(temp);
		}
	}
	
	public void addEdge(Integer t1, Integer t2) {
//		addRegister(t1);
//		addRegister(t2);
		connect(t1, t2);
	}
	
	public void removeReg(Integer reg) {
		ArrayList<Integer> nodesToDisconnect = new ArrayList<Integer>(graph.get(reg));
		disconnect(reg, nodesToDisconnect);
		
		graph.remove(reg);
	}
	
	public boolean isInInterferences(Integer reg1, Integer reg2) {
		if(graph.get(reg1) == null) {
			return false;
		}
		if(graph.get(reg1).contains(reg2)) {
			return true;
		}
		return false;
	}
	
	 public void addRegister(Integer reg) {
		 if(graph.containsKey(reg)) {
			 return;
		 }
		 graph.put(reg, new HashSet<Integer>());
	 }
	   
	   
	 public void connect(Integer reg1, Integer reg2) {
		 this.graph.get(reg1).add(reg2);
		 this.graph.get(reg2).add(reg1);
	 }
	 
	 public void disconnect(Integer reg1, ArrayList<Integer> regs) {
		 for(Integer reg2 : regs) {
			 this.graph.get(reg1).remove(reg2);
			 this.graph.get(reg2).remove(reg1);
		 }
	 }
	 
	 public void print() {
		System.out.println("%INTERFERENCES:");
		for (Integer reg : graph.keySet()) {
			for(Integer interferent : graph.get(reg)) {
				if(reg < interferent) {
					System.out.println("(" + reg + ", " + interferent + ")");
				}
			}
		}
	 }
	 
	 @Override
	 public InterferenceGraph clone() {
		InterferenceGraph interferenceGraph = new InterferenceGraph(this.codeGen);
		interferenceGraph.graph = new HashMap<Integer, HashSet<Integer>>();
		for(Integer key : this.graph.keySet()) {
			interferenceGraph.addRegister(key);
		}
		for(Integer key : this.graph.keySet()) {
			for(Integer interferent : this.graph.get(key)) {
				interferenceGraph.addEdge(key, interferent);
			}
		}
//		interferenceGraph.graph = (HashMap<Integer, HashSet<Integer>>) this.graph.clone();
		return interferenceGraph;
	 }
}
