package compiler.phase.codegen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import compiler.common.report.CompilerError;
import compiler.data.ast.attr.Attributes;
import compiler.data.codegen.BranchInstr;
import compiler.data.codegen.Instr;
import compiler.data.codegen.code.FullImcVisitor;
import compiler.data.frg.CodeFragment;
import compiler.data.imc.*;

public class EvalCodeGen extends FullImcVisitor {
	
	private final Attributes attrs;
	public static CodeFragment curFrag;
	public Vector<Instr> instructions = new Vector<Instr>();
	public HashSet<Integer> regs = new HashSet<Integer>();
	public HashMap<LABEL, Instr> labelToInstr = new HashMap<LABEL, Instr>();
	public LABEL curLabel = null;
	public HashMap<String, LABEL> strToLabel = new HashMap<String, LABEL>();
	public InterferenceGraph interferenceGraph;
	
	// empty constructor
	public EvalCodeGen(Attributes attrs, CodeFragment curFrag) {
		this.attrs = attrs;
		EvalCodeGen.curFrag = curFrag;
	}
	
	public void visit(BINOP binOp){
		try {
			binOp.expr1.accept(this);
			binOp.expr2.accept(this);
			
			Integer reg1 = attrs.regAttr.get(binOp.expr1);
			Integer reg2 = attrs.regAttr.get(binOp.expr2);
			
			Integer temp = new Integer(TEMP.newTempName());
			regs.add(temp);
			attrs.regAttr.set(binOp, temp);
			
			if (binOp.oper.equals(BINOP.Oper.ADD)) {
				Instr add = new Instr("\tADD %s,%s,%s\n", temp, true, reg1, false, reg2, false, false);
				add.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.SUB)) {
				Instr sub = new Instr("\tSUB %s,%s,%s\n", temp, true, reg1, false, reg2, false, false);
				sub.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.MUL)) {
				Instr mul = new Instr("\tMUL %s,%s,%s\n", temp, true, reg1, false, reg2, false, false);
				mul.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.DIV)) {
				Instr div = new Instr("\tDIV %s,%s,%s\n", temp, true, reg1, false, reg2, false, false);
				div.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.MOD)) {
				Integer temp2 = new Integer(TEMP.newTempName());
				regs.add(temp2);
				Instr div = new Instr("\tDIV %s,%s,%s\n", temp2, true, reg1, false, reg2, false, false);
				div.addToList(this, instructions);
				Integer temp3 = new Integer(TEMP.newTempName());
				regs.add(temp3);
				Instr mul = new Instr("\tMUL %s,%s,%s\n", temp3, true, reg2, false, temp2, false, false);
				mul.addToList(this, instructions);
				Instr sub = new Instr("\tSUB %s,%s,%s\n", temp, true, reg1, false, temp3, false, false);
				sub.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.OR)) {
				Instr or = new Instr("\tOR %s,%s,%s\n", temp, true, reg1, false, reg2, false, false);
				or.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.AND)) {
				Instr and = new Instr("\tAND %s,%s,%s\n", temp, true, reg1, false, reg2, false, false);
				and.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.EQU)) {
				Integer temp2 = new Integer(TEMP.newTempName());
				regs.add(temp2);
				Instr cmp = new Instr("\tCMP %s,%s,%s\n", temp2, true, reg1, false, reg2, false, false);
				cmp.addToList(this, instructions);
				Instr zsz = new Instr("\tZSZ %s,%s,1\n", temp, true, temp2, false, false);
				zsz.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.NEQ)) {
				Integer temp2 = new Integer(TEMP.newTempName());
				regs.add(temp2);
				Instr cmp = new Instr("\tCMP %s,%s,%s\n", temp2, true, reg1, false, reg2, false, false);
				cmp.addToList(this, instructions);
				Instr zsnz = new Instr("\tZSNZ %s,%s,1\n", temp, true, temp2, false, false);
				zsnz.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.LTH)) {
				Integer temp2 = new Integer(TEMP.newTempName());
				regs.add(temp2);
				Instr cmp = new Instr("\tCMP %s,%s,%s\n", temp2, true, reg1, false, reg2, false, false);
				cmp.addToList(this, instructions);
				Instr zsn = new Instr("\tZSN %s,%s,1\n", temp, true, temp2, false, false);
				zsn.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.GTH)) {
				Integer temp2 = new Integer(TEMP.newTempName());
				regs.add(temp2);
				Instr cmp = new Instr("\tCMP %s,%s,%s\n", temp2, true, reg1, false, reg2, false, false);
				cmp.addToList(this, instructions);
				Instr zsp = new Instr("\tZSP %s,%s,1\n", temp, true, temp2, false, false);
				zsp.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.LEQ)) {
				Integer temp2 = new Integer(TEMP.newTempName());
				regs.add(temp2);
				Instr cmp = new Instr("\tCMP %s,%s,%s\n", temp2, true, reg1, false, reg2, false, false);
				cmp.addToList(this, instructions);
				Instr zsnp = new Instr("\tZSNP %s,%s,1\n", temp, true, temp2, false, false);
				zsnp.addToList(this, instructions);
			} else if (binOp.oper.equals(BINOP.Oper.GEQ)) {
				Integer temp2 = new Integer(TEMP.newTempName());
				regs.add(temp2);
				Instr cmp = new Instr("\tCMP %s,%s,%s\n", temp2, true, reg1, false, reg2, false, false);
				cmp.addToList(this, instructions);
				Instr zsnn = new Instr("\tZSNN %s,%s,1\n", temp, true, temp2, false, false);
				zsnn.addToList(this, instructions);
			}
		} catch(Exception e) {
			throw new CompilerError("Error while visiting BINOP during code generation.");
		}
	}

	public void visit(CALL call) {
		try {
			long ptr = 0;
			for (int i = 0; i < call.numArgs(); i++) {
				call.args(i).accept(this);
				
				Integer reg = attrs.regAttr.get(call.args(i));
				Instr sto = new Instr("\tSTO %s,sp," + Long.toString(ptr) + "\n", reg, false, false);
				ptr += call.widths(i);
				sto.addToList(this, instructions);
			}
			Instr pushj = new Instr("\tPUSHJ k," + call.label + "\n", false);
			pushj.addToList(this, instructions);
			Integer temp = new Integer(TEMP.newTempName());
			regs.add(temp);
			Instr ldo = new Instr("\tLDO %s,sp,0\n", temp, true, false);
			ldo.addToList(this, instructions);
			attrs.regAttr.set(call, temp);
		} catch(Exception e) {
			throw new CompilerError("Error while visiting CALL during code generation.");
		}
	}

	public void visit(CJUMP cJump){
		try {
			cJump.cond.accept(this);
			Integer reg1 = attrs.regAttr.get(cJump.cond);
			
			Instr bp = new BranchInstr("\tBP %s," + cJump.posLabel + "\n", reg1, false, false, cJump.posLabel);
			bp.addToList(this, instructions);
		} catch(Exception e) {
			throw new CompilerError("Error while visiting CJUMP during code generation.");
		}
	}

	public void visit(CONST constant){
		try {
			Integer temp = new Integer(TEMP.newTempName());
			regs.add(temp);
			long val = constant.value;
	//		long val = -5L;
			Instr set = new Instr("\tSETL %s," + Long.toString(val & 0xFFFFL) + "\n", temp, true, false);
			attrs.regAttr.set(constant, temp);
			set.addToList(this, instructions);
			if((val & 0xFFFF0000L) != 0) {
				Instr set2 = new Instr("\tINCML %s," + Long.toString((val >> 16) & 0xFFFFL) + "\n", temp, true, false);
				set2.addToList(this, instructions);
			}
			if((val & 0xFFFF00000000L) != 0) {
				Instr set3 = new Instr("\tINCMH %s," + Long.toString((val >> 32) & 0xFFFFL) + "\n", temp, true, false);
				set3.addToList(this, instructions);
			}
			if((val & 0xFFFF000000000000L) != 0) {
				Instr set4 = new Instr("\tINCH %s," + Long.toString((val >> 48) & 0xFFFFL) + "\n", temp, true, false);
				set4.addToList(this, instructions);
			}
		} catch(Exception e) {
			throw new CompilerError("Error while visiting CONST during code generation.");
		}
	}

	public void visit(ESTMT eStmt){
		try {
			eStmt.expr.accept(this);
		} catch(Exception e) {
			throw new CompilerError("Error while visiting ESTMT during code generation.");
		}
	}

	public void visit(JUMP jump){
		try {
			Instr jmp = new BranchInstr("\tJMP " + jump.label + "\n", false, jump.label);
			jmp.addToList(this, instructions);
		} catch(Exception e) {
			throw new CompilerError("Error while visiting JUMP during code generation.");
		}
	}

	public void visit(LABEL label){
		try {
			if(curLabel != null) {
				Instr swym = new Instr("\tSWYM\n", false);
				swym.addToList(this, instructions);
			}
			curLabel = label;
			strToLabel.put(label.label, label);
		} catch(Exception e) {
			throw new CompilerError("Error while visiting LABEL during code generation.");
		}
	}

	public void visit(MEM mem){
		try {
			mem.addr.accept(this);
			
			Integer reg1 = new Integer(TEMP.newTempName());
			regs.add(reg1);
			Integer reg2 = attrs.regAttr.get(mem.addr);
			Instr ldo = new Instr("\tLDO %s,%s,0\n", reg1, true, reg2, false, false);
			attrs.regAttr.set(mem, reg1);
			ldo.addToList(this, instructions);
		} catch(Exception e) {
			throw new CompilerError("Error while visiting MEM during code generation.");
		}
	}

	public void visit(MOVE move){
		try {
			if(move.dst instanceof MEM) {
				// store
				((MEM)move.dst).addr.accept(this);
				move.src.accept(this);
				
				Integer reg1 = attrs.regAttr.get(((MEM)move.dst).addr);
				Integer reg2 = attrs.regAttr.get(move.src);
				
				Instr sto = new Instr("\tSTO %s,%s,0\n", reg2, false, reg1, false, false);
				sto.addToList(this, instructions);
			} else if (move.dst instanceof TEMP){
				move.dst.accept(this);
				move.src.accept(this);
				
				Integer reg1 = attrs.regAttr.get(move.dst);
				Integer reg2 = attrs.regAttr.get(move.src);
				
				Instr add = new Instr("\tADD %s,%s,0\n", reg1, true, reg2, false, true);
				add.addToList(this, instructions);
			}
		} catch(Exception e) {
			throw new CompilerError("Error while visiting MEM during code generation.");
		}
	}

	public void visit(NAME name){
		try {
			Integer temp = new Integer(TEMP.newTempName());
			regs.add(temp);
			Instr lda = new Instr("\tLDA %s," + name.name + "\n", temp, true, false);
			attrs.regAttr.set(name, temp);
			lda.addToList(this, instructions);
		} catch(Exception e) {
			throw new CompilerError("Error while visiting NAME during code generation.");
		}
	}

	public void visit(NOP nop){
		try {
			Integer temp = new Integer(TEMP.newTempName());
			regs.add(temp);
			
			Instr set = new Instr("\tSET %s,0\n", temp, true, false);
			attrs.regAttr.set(nop, temp);
			set.addToList(this, instructions);
		} catch(Exception e) {
			throw new CompilerError("Error while visiting NOP during code generation.");
		}
	}

	public void visit(SEXPR sExpr){
		try {
			sExpr.stmt.accept(this);
			sExpr.expr.accept(this);
			
			attrs.regAttr.set(sExpr, attrs.regAttr.get(sExpr.expr));
		} catch(Exception e) {
			throw new CompilerError("Error while visiting SEXPR during code generation.");
		}
	}

	public void visit(STMTS stmts){
		try {
			for (int i = 0; i < stmts.numStmts(); i++) {
				stmts.stmts(i).accept(this);
			}
		} catch(Exception e) {
			throw new CompilerError("Error while visiting STMTS during code generation.");
		}
	}

	public void visit(TEMP temp){
		try {
			attrs.regAttr.set(temp, temp.name);
			regs.add(temp.name);
		} catch(Exception e) {
			throw new CompilerError("Error while visiting TEMP during code generation.");
		}
	}

	public void visit(UNOP unOp){
		try {
			unOp.expr.accept(this);
			Integer reg = attrs.regAttr.get(unOp.expr);
			
			if(unOp.oper.equals(UNOP.Oper.ADD)) {
				attrs.regAttr.set(unOp, reg);
			} else if(unOp.oper.equals(UNOP.Oper.SUB)) {
				Integer temp = new Integer(TEMP.newTempName());
				regs.add(temp);
				Instr neg = new Instr("\tNEG %s,0,%s\n", temp, true, reg, false, false);
				attrs.regAttr.set(unOp, temp);
				neg.addToList(this, instructions);
			} else if(unOp.oper.equals(UNOP.Oper.NOT)) {
				Integer temp = new Integer(TEMP.newTempName());
				regs.add(temp);
				Instr zsp = new Instr("\tZSNP %s,%s,1\n", temp, true, reg, false, false);
				attrs.regAttr.set(unOp, temp);
				zsp.addToList(this, instructions);
			}
		} catch(Exception e) {
			throw new CompilerError("Error while visiting UNOP during code generation.");
		}
	}
	
//	public void addHeader() {
//		try {
//			curLabel = new LABEL(curFrag.label);
//			Instr swym = new Instr("\tSWYM\n", false);
//			swym.addToList(this, instructions);
//		} catch(Exception e) {
//			throw new CompilerError("Error while visiting adding header during code generation.");
//		}
//	}
	
	public void addFooter() {
		try {
			Instr swym = new Instr("\tSWYM\n", false);
			swym.addToList(this, instructions);
		} catch(Exception e) {
			throw new CompilerError("Error while visiting adding footer during code generation.");
		}
	}
	
}
