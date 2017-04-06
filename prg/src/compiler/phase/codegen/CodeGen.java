package compiler.phase.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import compiler.Task;
import compiler.common.report.CompilerError;
import compiler.data.codegen.BranchInstr;
import compiler.data.codegen.Instr;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.Fragment;
import compiler.data.imc.LABEL;
import compiler.data.imc.STMTS;
import compiler.data.imc.TEMP;
import compiler.phase.Phase;
import compiler.phase.utils.CodeGenUtils;
import compiler.phase.utils.PrintUtils;

public class CodeGen extends Phase {

	private Task task;
	private boolean print;

	/**
	 * Constructs the phase that performs code generation.
	 * 
	 * @param task
	 *            The parameters and internal data of the compilation process.
	 */
	public CodeGen(Task task, boolean print) {
		super(task, "codegen");

		this.task = task;
		this.print = print;
		codeGeneration();
	}

	/**
	 * Terminates generation of code. If logging has been
	 * requested, this method produces the report by closing the logger.
	 */
	@Override
	public void close() {
		super.close();
	}
	
	private void codeGeneration() {
		for (Fragment fragment : task.fragments.values()) {
			if (fragment instanceof CodeFragment) {
				STMTS stmts = ((CodeFragment) fragment).linCode;
				EvalCodeGen codeGen = new EvalCodeGen(task.prgAttrs, (CodeFragment) fragment);
				task.prgAttrs.codeGenAttr.set((CodeFragment) fragment, codeGen);
//				codeGen.addHeader();
				codeGen.visit(stmts);
				if(!fragment.label.equals("_")) {
					Instr sto = new Instr("\tSTO %s,fp,0\n", ((CodeFragment) fragment).RV, false, false);
					sto.addToList(codeGen, codeGen.instructions);
				}
				codeGen.regs.add(((CodeFragment) fragment).RV);
				codeGen.addFooter();
				
//				Integer t1 = TEMP.newTempName();
//				codeGen.regs.add(t1);
//				Integer t2 = TEMP.newTempName();
//				codeGen.regs.add(t2);
//				Integer t3 = TEMP.newTempName();
//				codeGen.regs.add(t3);
//				Integer t4 = TEMP.newTempName();
//				codeGen.regs.add(t4);
//				Integer t5 = TEMP.newTempName();
//				codeGen.regs.add(t5);
//				Integer t6 = TEMP.newTempName();
//				codeGen.regs.add(t6);
//				Integer t7 = TEMP.newTempName();
//				codeGen.regs.add(t7);
//				Integer t8 = TEMP.newTempName();
//				codeGen.regs.add(t8);
//				
//				codeGen.curLabel = new LABEL("Main");
//				codeGen.instructions.add(new Instr("SET %s,0\n", t1, true, false));
//				codeGen.instructions.add(new Instr("SET %s,1\n", t2, true, false));
//				codeGen.instructions.add(new Instr("SET %s,2\n", t3, true, false));
//				codeGen.instructions.add(new Instr("SET %s,4\n", t4, true, false));
//				codeGen.instructions.add(new Instr("ADD %s,%s,0\n", t5, true, t4, false, false));
////				codeGen.curLabel = new LABEL("L1");
////				codeGen.strToLabel.put("L1", codeGen.curLabel);
//				Instr instr = new Instr("ADD %s,%s,1\n", t6, true, t3, false, false);
////				codeGen.labelToInstr.put(codeGen.curLabel, instr);
//				codeGen.instructions.add(instr);
//				Instr instr2 = new Instr("ADD %s,%s,0\n", t7, true, t2, false, false);
//				codeGen.instructions.add(instr2);
//				Instr instr3 = new Instr("ADD %s,%s,0\n", t8, true, t1, false, false);
//				codeGen.instructions.add(instr3);
//				Instr instr4 = new Instr("ADD %s,%s,0\n", t1, true, t1, false, false);
//				codeGen.instructions.add(instr4);
//				codeGen.instructions.add(new Instr("ADD %s,%s,%s\n", t3, true, t3, false, t2, false, false));
//				codeGen.instructions.add(new Instr("MUL %s,%s,2\n", t1, true, t2, false, false));
//				Instr instr2 = new BranchInstr("BP %s,L1\n", t1, false, false, "L1");
//				codeGen.instructions.add(instr2);
//				codeGen.instructions.add(new Instr("ADD %s,%s,2\n", t4, true, t3, false, false));
				
//				TEMP t1 = new TEMP(3);
//				codeGen.virtRegsMap.put(t1, "T3");
//				TEMP t2 = new TEMP(4);
//				codeGen.virtRegsMap.put(t2, "T4");
//				TEMP t3 = new TEMP(5);
//				codeGen.virtRegsMap.put(t3, "T5");
//				TEMP t4 = new TEMP(6);
//				codeGen.virtRegsMap.put(t4, "T6");
//				
//				codeGen.curLabel = new LABEL("Main");
//				codeGen.instructions.add(new Instr("ADD %s,%s,0\n", t1, true, t2, false, true));
//				codeGen.instructions.add(new Instr("SWYM\n", false));
//				codeGen.instructions.add(new Instr("ADD %s,%s,1\n", t3, true, t2, false, false));
//				codeGen.instructions.add(new Instr("SWYM\n", false));
//				codeGen.instructions.add(new Instr("ADD %s,%s,1\n", t4, true, t1, false, false));
				if(this.print) {
					try {
						HashMap<Integer, String> virtRegsMap = PrintUtils.buildVirtRegsMap(codeGen.regs, ((CodeFragment) fragment).FP);
						PrintUtils.printWithTempRegs(codeGen.instructions, virtRegsMap);
					} catch(Exception e) {
						throw new CompilerError("Error while printing with temp registers.");
					}
				}
				
				try {
					CodeGenUtils.calcInstrSuccessors(codeGen);
//					PrintUtils.printWithSuccessors(codeGen.instructions, virtRegsMap);
				} catch(Exception e) {
					throw new CompilerError("Error while calculating instructions' successors.");
				}
				
				try {
					CodeGenUtils.calcInOut(codeGen.instructions);
//					PrintUtils.printInsAndOuts(codeGen.instructions, virtRegsMap);
				} catch(Exception e) {
					throw new CompilerError("Error while calculating instructions' ins and outs.");
				}

				try {
					CodeGenUtils.calcInterference(codeGen);
				} catch(Exception e) {
					throw new CompilerError("Error while calculating interference graph (in CodeGen).");
				}
				
				if(this.print) {
					try {
						PrintUtils.printInterferences(codeGen);
					} catch(Exception e) {
						throw new CompilerError("Error while printing interference graph (in CodeGen).");
					}
				}
 			}
		}
	}
	
}
