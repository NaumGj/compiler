package compiler.phase.utils;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import compiler.data.codegen.Instr;
import compiler.data.imc.TEMP;
import compiler.phase.codegen.EvalCodeGen;

public class PrintUtils {

	public static HashMap<Integer, String> buildVirtRegsMap(HashSet<Integer> regs, int FP) {
		HashMap<Integer, String> nameMap = new HashMap<Integer, String>();
		for(Integer temp : regs) {
			if(temp == FP) {
				nameMap.put(temp, "fp");
			} else {
				nameMap.put(temp, "T" + temp);
			}
		}
		return nameMap;
	}
	
	public static HashMap<Integer, String> buildRealRegsMap(HashSet<Integer> regs, int FP, HashMap<Integer, Integer> regToColor) {
		HashMap<Integer, String> nameMap = new HashMap<Integer, String>();
		for(Integer temp : regs) {
			if(temp == FP) {
				nameMap.put(temp, "fp");
			} else {
				nameMap.put(temp, "$" + regToColor.get(temp));
			}
		}
		return nameMap;
	}
	
//	public static void printInSeparateRegs(EvalCodeGen codeGen) {
//		for (TEMP temp : codeGen.virtRegsMap.keySet()) {
//			codeGen.virtRegsMap.put(temp, codeGen.virtRegsMap.get(temp).replace('T', '$'));
//		}
//		printWithTempRegs(codeGen);
//	}
	
	public static void printWithTempRegs(Vector<Instr> instructions, HashMap<Integer, String> regsMap) {
		for(Instr instr : instructions) {
			System.out.print(instr.print(regsMap));
		}
		System.out.println();
	}
	
	public static void printWithRegs(Vector<Instr> instructions, HashMap<Integer, String> regsMap, PrintWriter writer) {
		for(Instr instr : instructions) {
			writer.print(instr.print(regsMap));
		}
		writer.println();
	}
	
	public static void printWithSuccessors(Vector<Instr> instructions, HashMap<Integer, String> virtRegsMap) {
		for(Instr instr : instructions) {
			System.out.print(instr.print(virtRegsMap));
			System.out.println("SUCC:");
			for(Instr succ: instr.succ) {
				System.out.println(succ.print(virtRegsMap));
			}
		}
	}
	
	public static void printInsAndOuts(Vector<Instr> instructions, HashMap<Integer, String> virtRegsMap) {
		for(Instr instr : instructions) {
			System.out.println(instr.print(virtRegsMap));
			System.out.print("INS:");
			for(Integer temp : instr.in) {
				System.out.print(virtRegsMap.get(temp) + ", ");
			}
			System.out.println();
			System.out.print("OUTS:");
			for(Integer temp : instr.out) {
				System.out.print(virtRegsMap.get(temp) + ", ");
			}
			System.out.println();
		}
	}
	
	public static void printInterferences(EvalCodeGen codeGen) {
		codeGen.interferenceGraph.print();
//		System.out.println("%INTERFERENCES:");
//		for(InterferenceEdge interference : codeGen.interferences) {
//			System.out.println("(" + interference.t1.name + ", " + interference.t2.name + ")");
//		}
	}
	
	public static void printWithRealRegs(EvalCodeGen codeGen, HashMap<Integer, Integer> regToColor) {
		for(Instr instr : codeGen.instructions) {
			Integer color1, color2, color3 = null;
			if(instr.numArgs == 0) {
				System.out.print(instr.printRealRegs(null, null, null));
			} else if(instr.numArgs == 1) {
				color1 = regToColor.get(instr.t1);
				if(color1 == null) {
					color1 = -1;
				}
//				System.out.println(instr.t1);
				System.out.print(instr.printRealRegs(color1, null, null));
			} else if(instr.numArgs == 2) {
				color1 = regToColor.get(instr.t1);
				if(color1 == null) {
					color1 = -1;
				}
				color2 = regToColor.get(instr.t2);
				if(color2 == null) {
					color2 = -1;
				}
//				System.out.println(instr.t1 + ", " + instr.t2);
				System.out.print(instr.printRealRegs(color1, color2, null));
			} else if(instr.numArgs == 3) {
				color1 = regToColor.get(instr.t1);
				if(color1 == null) {
					color1 = -1;
				}
				color2 = regToColor.get(instr.t2);
				if(color2 == null) {
					color2 = -1;
				}
				color3 = regToColor.get(instr.t3);
				if(color3 == null) {
					color3 = -1;
				}
//				System.out.println(instr.t1 + ", " + instr.t2 + ", " + instr.t3);
				System.out.print(instr.printRealRegs(color1, color2, color3));
			}
		}
	}
	
}
//private final Charset UTF8_CHARSET = Charset.forName("UTF-8");
//
//String decodeUTF8(byte[] bytes) {
//    return new String(bytes, UTF8_CHARSET);
//}
//
//byte[] encodeUTF8(String string) {
//    return string.getBytes(UTF8_CHARSET);
//}
