package compiler.phase.regalloc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;

import compiler.Task;
import compiler.common.report.CompilerError;
import compiler.data.ast.attr.Attributes;
import compiler.data.codegen.Instr;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.ConstFragment;
import compiler.data.frg.DataFragment;
import compiler.data.frg.Fragment;
import compiler.phase.Phase;
import compiler.phase.codegen.EvalCodeGen;
import compiler.phase.codegen.InterferenceGraph;
import compiler.phase.utils.CodeGenUtils;
import compiler.phase.utils.PrintUtils;
import compiler.phase.utils.RegAllocUtils;

public class RegAlloc extends Phase {

	private Task task;
	private final Attributes attrs;
	public Stack<ColoredReg> tempsStack = new Stack<ColoredReg>();
	public HashMap<Integer, Integer> regToColor = new HashMap<Integer, Integer>();
	private int numRegs;
	private HashSet<Integer> spills = new HashSet<Integer>();
	public static CodeFragment curCodeFragment = null;

	/**
	 * Constructs the phase that performs register allocation.
	 * 
	 * @param task
	 *            The parameters and internal data of the compilation process.
	 */
	public RegAlloc(Task task) {
		super(task, "regalloc");

		this.task = task;
		this.attrs = task.prgAttrs;
		
		numRegs = task.numRegs;
		allocateRegs();
	}

	/**
	 * Terminates allocation of registers. If logging has been
	 * requested, this method produces the report by closing the logger.
	 */
	@Override
	public void close() {
		super.close();
	}
	
	@SuppressWarnings("unchecked")
	public void allocateRegs() {
		PrintWriter writer = null;
		String srcName;
		String[] arr = task.srcFName.split("/");
		srcName = arr[arr.length - 1]; 
		arr = srcName.split("\\.");
		String mmsName = arr[arr.length-2] + ".mms";
		try {
			writer = new PrintWriter(mmsName, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			throw new CompilerError("Error while creating .mms file.");
		}
		writer.println("io\tIS $255");
		writer.println("sp\tGREG 0");
		writer.println("fp\tGREG 0");
		writer.println("k\tIS $" + Integer.toString(task.numRegs));
		writer.println("\tLOC Data_Segment");
		for (Fragment fragment : task.fragments.values()) {
			if (fragment instanceof DataFragment) {
				DataFragment frag = (DataFragment) fragment;
//				System.out.println(frag.label);
//				System.out.println(frag.width);
//				if(frag.width == 8) {
//					writer.println(frag.label + "\tOCTA 0");
//				} else if(frag.width == 4) {
//					writer.println(frag.label + "\tTETRA 0");
//				} else if(frag.width == 2) {
//					writer.println(frag.label + "\tWYDE 0");
//				} else if(frag.width == 1) {
//					writer.println(frag.label + "\tBYTE 0");
//				}
				writer.println(frag.label + "\tLOC @+" + frag.width);
			} else if(fragment instanceof ConstFragment) {
				ConstFragment frag = (ConstFragment) fragment;
				writer.print(frag.label + "\tBYTE ");
				StringBuilder string = new StringBuilder();
				String[] escapes = frag.string.split("\\\\");
				string.append(escapes[0]);
				for(int i = 1; i < escapes.length; i++) {
					String escape = escapes[i];
					if(escape.charAt(0) == '\\') {
						string.append("\",92,\"");
					} else if(escape.charAt(0) == '\'') {
						string.append("\",34,\"");
					} else if(escape.charAt(0) == '"') {
						string.append("\",39,\"");
					} else if(escape.charAt(0) == 't') {
						string.append("\",9,\"");
					} else if(escape.charAt(0) == 'n') {
						string.append("\",10,\"");
					}
					
					string.append(escape.substring(1));
				}
				string.append(",0");
				writer.println(string.toString().replace("\"\",", "").replace("\"\"", ""));
				writer.println("\tLOC (@+7)&-8");
			}
		}
		RegAllocUtils.addDataSegmentForStdLib(writer);
		writer.println("\tLOC #100");
		RegAllocUtils.addMainMethod(writer);
		for (Fragment fragment : task.fragments.values()) {
			if (fragment instanceof CodeFragment) {
				CodeFragment frag = (CodeFragment) fragment;
				curCodeFragment = frag;
				EvalCodeGen codeGen = attrs.codeGenAttr.get(frag);
				
				boolean areAllColored = false;
				
				while(!areAllColored){
					if(codeGen.interferenceGraph == null) {
						build(codeGen);
					}
					InterferenceGraph interferenceGraph = codeGen.interferenceGraph.clone();
					HashSet<Integer> regs = (HashSet<Integer>) codeGen.regs.clone();
					while(!regs.isEmpty()) {
						simplify(codeGen, regs);
						spill(codeGen, regs);
					}
					areAllColored = select(codeGen, interferenceGraph);
					if(!areAllColored) {
						startOver(codeGen);
						codeGen.interferenceGraph = null;
					}
				}
//				PrintUtils.printWithRealRegs(codeGen, regToColor);
				RegAllocUtils.prolog(frag, writer);
				
				HashMap<Integer, String> realRegsMap = PrintUtils.buildRealRegsMap(codeGen.regs, frag.FP, regToColor);
				PrintUtils.printWithRegs(codeGen.instructions, realRegsMap, writer);
				
				RegAllocUtils.epilog(frag, writer);
			} 
		}
		RegAllocUtils.addStdLib(writer);
		writer.close();
	}
	
	private void clearInstrAttrs(Vector<Instr> instructions) {
		try {
			for(Instr instr : instructions) {
				instr.clearAttrs();
			}
		} catch(Exception e) {
			throw new CompilerError("Error while clearing instructions attributes during register allocation.");
		}
	}
	
	public void build(EvalCodeGen codeGen) {
		try {
			codeGen.interferenceGraph = new InterferenceGraph(codeGen);
			clearInstrAttrs(codeGen.instructions);
			CodeGenUtils.calcInstrSuccessors(codeGen);
			CodeGenUtils.calcInOut(codeGen.instructions);
			CodeGenUtils.calcInterference(codeGen);
//			HashMap<Integer, String> virtRegsMap = PrintUtils.buildVirtRegsMap(codeGen.regs, 2);
//			PrintUtils.printWithRegs(codeGen.instructions, virtRegsMap);
//			codeGen.interferenceGraph.print();
		} catch(Exception e) {
			throw new CompilerError("Error in build phase during register allocation.");
		}
	}
	
	public void simplify(EvalCodeGen codeGen, HashSet<Integer> regs) {
		try {
			HashMap<Integer, HashSet<Integer>> graph = codeGen.interferenceGraph.graph;
			boolean canSimplify = true;
			while(canSimplify) {
				canSimplify = false;
				Integer toRemove = null;
				ArrayList<Integer> regsArr = new ArrayList<Integer>(regs);
				Collections.sort(regsArr);
				for (Integer reg : regsArr) {
//					System.out.println(reg);
//					System.out.println(graph.get(reg));
					if (graph.get(reg).size() < numRegs) {
						toRemove = reg;
//						codeGen.interferenceGraph.removeReg(reg);
						canSimplify = true;
						tempsStack.push(new ColoredReg(reg));
						break;
					}
				}
				if(toRemove != null) {
					codeGen.interferenceGraph.removeReg(toRemove);
					regs.remove(toRemove);
				}
			}
		} catch(Exception e) {
			throw new CompilerError("Error in simplify phase during register allocation.");
		}
	}
	
	public void spill(EvalCodeGen codeGen, HashSet<Integer> regs) {
		try {
			Integer toRemove = null;
			ArrayList<Integer> regsArr = new ArrayList<Integer>(regs);
			Collections.sort(regsArr);
			for(Integer reg : regsArr) {
				toRemove = reg;
				tempsStack.push(new ColoredReg(reg));
				break;
			}
			if(toRemove != null) {
				codeGen.interferenceGraph.removeReg(toRemove);
				regs.remove(toRemove);
			}
		} catch(Exception e) {
			throw new CompilerError("Error in spill phase during register allocation.");
		}
	}
	
	public boolean select(EvalCodeGen codeGen, InterferenceGraph interferenceGraph) {
		try {
			boolean areAllColored = true;
			while(!tempsStack.isEmpty()) {
				ColoredReg reg = tempsStack.pop();
				HashSet<Integer> neighbourColors = new HashSet<Integer>();
				for(Integer neighbour : interferenceGraph.graph.get(reg.reg)) {
					if(regToColor.get(neighbour) != null) {
						neighbourColors.add(regToColor.get(neighbour));
					}
				}
				boolean isColorAssigned = false;
				for(Integer i = 0; i < numRegs; i++) {
					if(!neighbourColors.contains(i)) {
						regToColor.put(reg.reg, i);
						reg.color = i;
						isColorAssigned = true;
						break;
					}
				}
				if(!isColorAssigned) {
					spills.add(reg.reg);
					areAllColored = false;
				}
			}
			return areAllColored;
		} catch(Exception e) {
			throw new CompilerError("Error in select phase during register allocation.");
		}
	}
	
	public void startOver(EvalCodeGen codeGen) {
		try {
			for(int i = 0; i < codeGen.instructions.size(); i++) {
				Instr instr = codeGen.instructions.get(i);
				
//				System.out.println(i);
//				System.out.println(instr.str);
//				System.out.println(codeGen.instructions.size());
				if(instr.numArgs >= 1) {
					if(spills.contains(instr.t1)) {
						if(instr.use.contains(instr.t1)) {
							RegAllocUtils.addSpillInstructionsFstRegLDO(codeGen, instr, i);
						} else if(instr.def.contains(instr.t1)) {
							RegAllocUtils.addSpillInstructionsFstRegSTO(codeGen, instr, i);
						}
					}
				}
				if(instr.numArgs >= 2) {
					if(spills.contains(instr.t2)) {
						if(instr.use.contains(instr.t2)) {
							RegAllocUtils.addSpillInstructionsSndRegLDO(codeGen, instr, i);
						} else if(instr.def.contains(instr.t2)) {
							RegAllocUtils.addSpillInstructionsSndRegSTO(codeGen, instr, i);
						}
					}
				}
				if(instr.numArgs == 3) {
					if(spills.contains(instr.t3)) {
						if(instr.use.contains(instr.t3)) {
							RegAllocUtils.addSpillInstructionsThdRegLDO(codeGen, instr, i);
						} else if(instr.def.contains(instr.t3)) {
							RegAllocUtils.addSpillInstructionsThdRegSTO(codeGen, instr, i);
						}
					}
				}
			}
		} catch(Exception e) {
			throw new CompilerError("Error in start over phase during register allocation.");
		}
	}
	
	
}
