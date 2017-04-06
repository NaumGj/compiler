package compiler.phase.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import compiler.data.codegen.BranchInstr;
import compiler.data.codegen.Instr;
import compiler.data.imc.LABEL;
import compiler.phase.codegen.EvalCodeGen;
import compiler.phase.codegen.InterferenceGraph;

public class CodeGenUtils {

	public static void calcInstrSuccessors(EvalCodeGen codeGen) {
		Vector<Instr> instructions = codeGen.instructions;
		for (int i = 0; i < instructions.size(); i++) {
			Instr instr = instructions.get(i);
			if(instr instanceof BranchInstr) {
				BranchInstr bInstr = (BranchInstr)instr;
				LABEL label = (LABEL)codeGen.strToLabel.get(bInstr.jumpLabel);
				instr.succ.add(codeGen.labelToInstr.get(label));
				if(!bInstr.unconditionalJump) {
					if(i < (instructions.size() - 1)) {
						instr.succ.add(instructions.get(i+1));
					}
				}
			} else {
				if(i < (instructions.size() - 1)) {
					instr.succ.add(instructions.get(i+1));
				}
			}
		}
	}
	
	public static void calcInOut(Vector<Instr> instructions) {
		boolean hasChanged = true;
//		int numIter = 0;
		while(hasChanged) {
			hasChanged = false;
			for (int i = instructions.size()-1; i >= 0; i--) {

				Instr instr = instructions.get(i);
				
				Set<Integer> newOut = new HashSet<Integer>();
				for(Instr succ : instr.succ) {
					newOut.addAll(succ.in);
				}
				if(!newOut.equals(instr.out)) {
					hasChanged = true;
				}
				instr.out = newOut;
				
				Set<Integer> newIn = new HashSet<Integer>(instr.out);
				newIn.removeAll(instr.def);
				newIn.addAll(instr.use);
				if(!newIn.equals(instr.in)) {
					hasChanged = true;
				}
				instr.in = newIn;
			}
//			numIter++;
//			System.out.println(numIter);
		}
	}
	
	public static void calcInterference(EvalCodeGen codeGen) {
		codeGen.interferenceGraph = new InterferenceGraph(codeGen);
		Vector<Instr> instructions = codeGen.instructions;
		for (int i = 0; i < instructions.size(); i++) {
			Instr instr = instructions.get(i);
			
			ArrayList<Integer> ins = new ArrayList<Integer>(instr.in);
			for (int j = 0; j < ins.size(); j++) {
				Integer t1 = ins.get(j);
				for (int k = j+1; k < ins.size(); k++) {
					Integer t2 = ins.get(k);
					if(!codeGen.interferenceGraph.isInInterferences(t1, t2)) {
//						if(instr.isMove) {
//							if(!((instr.def.contains(t1) && instr.use.contains(t2)) || (instr.def.contains(t2) && instr.use.contains(t1)))) {
//								codeGen.interferenceGraph.addEdge(new InterferenceEdge(t1, t2));
//							}
//						} else {
							codeGen.interferenceGraph.addEdge(t1, t2);
//						}
					}
				}
			}
			
			ArrayList<Integer> outs = new ArrayList<Integer>(instr.out);
			for (int j = 0; j < outs.size(); j++) {
				Integer t1 = outs.get(j);
				for (int k = j+1; k < outs.size(); k++) {
					Integer t2 = outs.get(k);
					if(!codeGen.interferenceGraph.isInInterferences(t1, t2)) {
//						if(instr.isMove) {
//							if(!((instr.def.contains(t1) && instr.use.contains(t2)) || (instr.def.contains(t2) && instr.use.contains(t1)))) {
//								codeGen.interferenceGraph.addEdge(new InterferenceEdge(t1, t2));
//							}
//						} else {
							codeGen.interferenceGraph.addEdge(t1, t2);
//						}
					}
				}
			}
		}
	}
}
