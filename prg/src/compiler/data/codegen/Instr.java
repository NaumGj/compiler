package compiler.data.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import compiler.data.imc.LABEL;
import compiler.data.imc.TEMP;
import compiler.phase.codegen.EvalCodeGen;
import compiler.phase.codegen.TEMPHashSet;

public class Instr {
	
	public int numArgs;
	public LABEL label = null;
	public Integer t1;
	public Integer t2;
	public Integer t3;
	public String str;
	public boolean isMove;
	
	public Set<Integer> use = new HashSet<Integer>();
	public Set<Integer> def = new HashSet<Integer>();
	
	public Set<Integer> in = new HashSet<Integer>();
	public Set<Integer> out = new HashSet<Integer>();
	
	public ArrayList<Instr> succ = new ArrayList<Instr>();
	
	public Instr(String str, boolean isMove) {
		this.str = str;
		this.isMove = isMove;
		numArgs = 0;
	}
	
	public Instr(String str, Integer t1, boolean isDef1, boolean isMove) {
		this.str = str;
		this.t1 = t1;
		this.isMove = isMove;
		numArgs = 1;
		
		if(isDef1) {
			if(t1 != EvalCodeGen.curFrag.FP) {
				def.add(t1);
			}
		} else {
			if(t1 != EvalCodeGen.curFrag.FP) {
				use.add(t1);
			}
		}
	}
	
	public Instr(String str, Integer t1, boolean isDef1, Integer t2, boolean isDef2, boolean isMove) {
		this.str = str;
		this.t1 = t1;
		this.t2 = t2;
		this.isMove = isMove;
		numArgs = 2;
		
		if(isDef1) {
			if(t1 != EvalCodeGen.curFrag.FP) {
				def.add(t1);
			}
		} else {
			if(t1 != EvalCodeGen.curFrag.FP) {
				use.add(t1);
			}
		}
		if(isDef2) {
			if(t2 != EvalCodeGen.curFrag.FP) {
				def.add(t2);
			}
		} else {
			if(t2 != EvalCodeGen.curFrag.FP) {
				use.add(t2);
			}
		}
	}
	
	public Instr(String str, Integer t1, boolean isDef1, Integer t2, boolean isDef2, Integer t3, boolean isDef3, boolean isMove) {
		this.str = str;
		this.t1 = t1;
		this.t2 = t2;
		this.t3 = t3;
		this.isMove = isMove;
		numArgs = 3;
		
		if(isDef1) {
			if(t1 != EvalCodeGen.curFrag.FP) {
				def.add(t1);
			}
		} else {
			if(t1 != EvalCodeGen.curFrag.FP) {
				use.add(t1);
			}
		}
		if(isDef2) {
			if(t2 != EvalCodeGen.curFrag.FP) {
				def.add(t2);
			}
		} else {
			if(t2 != EvalCodeGen.curFrag.FP) {
				use.add(t2);
			}
		}
		if(isDef3) {
			if(t3 != EvalCodeGen.curFrag.FP) {
				def.add(t3);
			}
		} else {
			if(t3 != EvalCodeGen.curFrag.FP) {
				use.add(t3);
			}
		}
	}
	
	public void addToList(EvalCodeGen fragCodeGen, Vector<Instr> instructions) {
		if(fragCodeGen.curLabel != null) {
			fragCodeGen.labelToInstr.put(fragCodeGen.curLabel, this);
		}
		this.label = fragCodeGen.curLabel;
		fragCodeGen.curLabel = null;
		instructions.add(this);
	}
	
	public void addToListAtIndex(EvalCodeGen fragCodeGen, Vector<Instr> instructions, int index) {
		instructions.add(index, this);
	}
	
	public String print(HashMap<Integer, String> virtRegsMap) {
		return (this.label == null ? "" : this.label.label) + String.format(this.str, virtRegsMap.get(t1), virtRegsMap.get(t2), virtRegsMap.get(t3));
	}
	
	public String printRealRegs(Integer c1, Integer c2, Integer c3) {
		if(c1 == null) {
			return ((this.label == null ? "" : this.label.label) + this.str);
		}
		if(c2 == null) {
			return (this.label == null ? "" : this.label.label) + String.format(this.str, "$" + Integer.toString(c1));
		}
		if(c3 == null) {
			return (this.label == null ? "" : this.label.label) + String.format(this.str, "$" + Integer.toString(c1), "$" + Integer.toString(c2));
		}
		return (this.label == null ? "" : this.label.label) + String.format(this.str, "$" + Integer.toString(c1), "$" + Integer.toString(c2), "$" + Integer.toString(c3));
	}
	
	public void clearAttrs() {
		this.in = new HashSet<Integer>();
		this.out = new HashSet<Integer>();
		
		this.succ = new ArrayList<Instr>();
	}
	
	public void changeReg(Integer t, int index) {
		if (index == 1) {
			if(use.contains(t1)) {
				use.remove(t1);
				use.add(t);
				t1 = t;
			} else if (def.contains(t1)) {
				def.remove(t1);
				def.add(t);
				t1 = t;
			}
		} else if (index == 2) {
			if(use.contains(t2)) {
				use.remove(t2);
				use.add(t);
				t2 = t;
			} else if (def.contains(t2)) {
				def.remove(t2);
				def.add(t);
				t2 = t;
			}
		} else if (index == 3) {
			if(use.contains(t3)) {
				use.remove(t3);
				use.add(t);
				t3 = t;
			} else if (def.contains(t3)) {
				def.remove(t3);
				def.add(t);
				t3 = t;
			}
		}
	}
	
}
