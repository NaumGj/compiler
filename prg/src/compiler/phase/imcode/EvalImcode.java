package compiler.phase.imcode;

import java.util.*;

import compiler.common.report.*;
import compiler.data.acc.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.frg.*;
import compiler.data.frm.*;
import compiler.data.imc.*;
import compiler.data.typ.ArrTyp;

/**
 * Evaluates intermediate code.
 * 
 * @author sliva
 */
public class EvalImcode extends FullVisitor {

	private final Attributes attrs;

	private final HashMap<String, Fragment> fragments;

	private Stack<CodeFragment> codeFragments = new Stack<CodeFragment>();

	public EvalImcode(Attributes attrs, HashMap<String, Fragment> fragments) {
		this.attrs = attrs;
		this.fragments = fragments;
	}

	public void visit(ArrType arrType) {
		arrType.size.accept(this);
		arrType.elemType.accept(this);
	}

	@Override
	public void visit(AtomExpr atomExpr) {
		try {
			switch (atomExpr.type) {
			case INTEGER:
				try {
					long value = Long.parseLong(atomExpr.value);
					attrs.imcAttr.set(atomExpr, new CONST(value));
				} catch (NumberFormatException ex) {
					Report.warning(atomExpr, "Illegal integer constant.");
				}
				break;
			case BOOLEAN:
				if (atomExpr.value.equals("true"))
					attrs.imcAttr.set(atomExpr, new CONST(1));
				if (atomExpr.value.equals("false"))
					attrs.imcAttr.set(atomExpr, new CONST(0));
				break;
			case CHAR:
				if (atomExpr.value.charAt(1) == '\\'){
					if(atomExpr.value.charAt(2) == 't') {
						attrs.imcAttr.set(atomExpr, new CONST(9));
					} else if (atomExpr.value.charAt(2) == 'n') {
						attrs.imcAttr.set(atomExpr, new CONST(10));
					} else {
						attrs.imcAttr.set(atomExpr, new CONST(atomExpr.value.charAt(2)));
					}
				} else if (atomExpr.value.charAt(1) == '\'') {
					attrs.imcAttr.set(atomExpr, new CONST(atomExpr.value.charAt(2)));
				} else {
					attrs.imcAttr.set(atomExpr, new CONST(atomExpr.value.charAt(1)));
				}
				break;
			case STRING:
				String label = LABEL.newLabelName();
				attrs.imcAttr.set(atomExpr, new NAME(label));
				ConstFragment fragment = new ConstFragment(label, atomExpr.value);
				attrs.frgAttr.set(atomExpr, fragment);
				fragments.put(fragment.label, fragment);
				break;
			case PTR:
				attrs.imcAttr.set(atomExpr, new CONST(0));
				break;
			case VOID:
				attrs.imcAttr.set(atomExpr, new NOP());
				break;
			}
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + atomExpr.toString());
		}
	}

	public void visit(AtomType atomType) {
	}

	public void visit(BinExpr binExpr) {
		try {
			binExpr.fstExpr.accept(this);
			binExpr.sndExpr.accept(this);
			IMCExpr fstImcExpr = (IMCExpr) attrs.imcAttr.get(binExpr.fstExpr);
			IMCExpr sndImcExpr = (IMCExpr) attrs.imcAttr.get(binExpr.sndExpr);
			
			if(binExpr.oper.equals(BinExpr.Oper.ADD)) {
				BINOP binOp = new BINOP(BINOP.Oper.ADD, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.SUB)) {
				BINOP binOp = new BINOP(BINOP.Oper.SUB, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.MUL)) {
				BINOP binOp = new BINOP(BINOP.Oper.MUL, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.DIV)) {
				BINOP binOp = new BINOP(BINOP.Oper.DIV, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.MOD)) {
				BINOP binOp = new BINOP(BINOP.Oper.MOD, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.OR)) {
				BINOP binOp = new BINOP(BINOP.Oper.OR, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.AND)) {
				BINOP binOp = new BINOP(BINOP.Oper.AND, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.EQU)) {
				BINOP binOp = new BINOP(BINOP.Oper.EQU, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.NEQ)) {
				BINOP binOp = new BINOP(BINOP.Oper.NEQ, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.LTH)) {
				BINOP binOp = new BINOP(BINOP.Oper.LTH, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.GTH)) {
				BINOP binOp = new BINOP(BINOP.Oper.GTH, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.LEQ)) {
				BINOP binOp = new BINOP(BINOP.Oper.LEQ, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.GEQ)) {
				BINOP binOp = new BINOP(BINOP.Oper.GEQ, fstImcExpr, sndImcExpr);
				attrs.imcAttr.set(binExpr, binOp);
			} else if(binExpr.oper.equals(BinExpr.Oper.ASSIGN)) {
				MOVE move = new MOVE(fstImcExpr, sndImcExpr);
				SEXPR sexpr = new SEXPR(move, new NOP());
				attrs.imcAttr.set(binExpr, sexpr);
			} else if (binExpr.oper.equals(BinExpr.Oper.ARR)) {
				ArrTyp arrTyp = (ArrTyp)attrs.typAttr.get(binExpr.fstExpr).actualTyp();
				MEM fstMemExpr = (MEM) fstImcExpr;
				BINOP mulOp = new BINOP(BINOP.Oper.MUL, sndImcExpr, new CONST(arrTyp.elemTyp.size()));
				BINOP binOp = new BINOP(BINOP.Oper.ADD, fstMemExpr.addr, mulOp);
				MEM mem = new MEM(binOp, arrTyp.elemTyp.size());
				attrs.imcAttr.set(binExpr, mem);
			} else if (binExpr.oper.equals(BinExpr.Oper.REC)) {
				CompDecl compDecl = (CompDecl)attrs.declAttr.get((CompName)binExpr.sndExpr);
				OffsetAccess accSnd = (OffsetAccess) attrs.accAttr.get(compDecl);
				MEM fstMemExpr = (MEM) fstImcExpr;
				MEM mem = new MEM(new BINOP(BINOP.Oper.ADD, fstMemExpr.addr, new CONST(accSnd.offset)), attrs.typAttr.get(binExpr.sndExpr).size());
				attrs.imcAttr.set(binExpr, mem);
			}
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + binExpr.toString());
		}
	}

	public void visit(CastExpr castExpr) {
		try{
			castExpr.type.accept(this);
			castExpr.expr.accept(this);
			
			attrs.imcAttr.set(castExpr, attrs.imcAttr.get(castExpr.expr));
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + castExpr.toString());
		}
	}

	public void visit(CompDecl compDecl) {
		compDecl.type.accept(this);
	}

	public void visit(CompName compName) {
	}
	
	public void visit(DeclError declError) {
	}

	public void visit(Exprs exprs) {
		try {
			Vector<IMCStmt> stmts = new Vector<IMCStmt>();
			
			for (int e = 0; e < exprs.numExprs() - 1; e++) {
				exprs.expr(e).accept(this);
				IMCExpr exprImc = (IMCExpr) attrs.imcAttr.get(exprs.expr(e));
				stmts.add(new ESTMT(exprImc));
			}
			STMTS stmt = new STMTS(stmts);
			
			// give the last expression as expression in SEXPR
			int e = exprs.numExprs() - 1;
			exprs.expr(e).accept(this);
			IMCExpr exprImc = (IMCExpr) attrs.imcAttr.get(exprs.expr(e));
			attrs.imcAttr.set(exprs, new SEXPR(stmt, exprImc));
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + exprs.toString());
		}
	}

	public void visit(ExprError exprError) {
	}

	public void visit(ForExpr forExpr) {
		try {
			forExpr.var.accept(this);
			forExpr.loBound.accept(this);
			forExpr.hiBound.accept(this);
			forExpr.body.accept(this);
			
			IMCExpr varImc = (IMCExpr)attrs.imcAttr.get(forExpr.var);
			IMCExpr loBoundImc = (IMCExpr)attrs.imcAttr.get(forExpr.loBound);
			IMCExpr hiBoundImc = (IMCExpr)attrs.imcAttr.get(forExpr.hiBound);
			IMCExpr bodyImc = (IMCExpr)attrs.imcAttr.get(forExpr.body);
			
			//LABEL entry = new LABEL(LABEL.newLabelName());
			LABEL loop = new LABEL(LABEL.newLabelName());
			LABEL loop2 = new LABEL(LABEL.newLabelName());
			LABEL exit = new LABEL(LABEL.newLabelName());
			Vector<IMCStmt> stmts = new Vector<IMCStmt>();
			stmts.add(new MOVE(varImc, loBoundImc));
			//stmts.add(entry);
			stmts.add(new CJUMP(new BINOP(BINOP.Oper.LEQ, varImc, hiBoundImc), loop.label, exit.label));
			stmts.add(new JUMP(exit.label));
			stmts.add(loop);
			stmts.add(new ESTMT(bodyImc));
			stmts.add(new CJUMP(new BINOP(BINOP.Oper.LTH, varImc, hiBoundImc), loop2.label, exit.label));
			stmts.add(new JUMP(exit.label));
			stmts.add(loop2);
			stmts.add(new MOVE(varImc, new BINOP(BINOP.Oper.ADD, varImc, new CONST(1))));
			stmts.add(new JUMP(loop.label));
			stmts.add(exit);
			
			attrs.imcAttr.set(forExpr, new SEXPR(new STMTS(stmts), new NOP()));
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + forExpr.toString());
		}
	}

	public void visit(FunCall funCall) {
		try {
			Frame frame = attrs.frmAttr.get((FunDecl)attrs.declAttr.get(funCall));
			if (frame == null) {	// it's a call to FunDecl, there is no frame
				frame = new Frame(0, "_" + funCall.name(), 0, 0, 0, 0, 0);
			}
			String label = frame.label;
			Vector<IMCExpr> args = new Vector<IMCExpr>();
			Vector<Long> widths = new Vector<Long>();
			// static link
			int diff = codeFragments.peek().frame.level - frame.level;
	//		System.out.println(funCall.name() + ", " + diff);
			IMCExpr staticLink = null;
			if (diff >= -1) {
				staticLink = new TEMP(codeFragments.peek().FP);
			}
			for (int i = diff; i > -1; i--) {
				staticLink = new MEM(staticLink, (long)8);
			}
			if(staticLink == null) {
				throw new CompilerError("Invalid level of static link (function call) at " + funCall.toString());
			}
			args.add(staticLink);
			widths.add((long)8);
			
			for (int a = 0; a < funCall.numArgs(); a++) {
				funCall.arg(a).accept(this);
				args.add((IMCExpr)attrs.imcAttr.get(funCall.arg(a)));
				widths.add(attrs.typAttr.get(funCall.arg(a)).size());
			}
			
			CALL call = new CALL(label, args, widths);
			attrs.imcAttr.set(funCall, call);
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + funCall.toString());
		}
	}

	public void visit(FunDecl funDecl) {
		for (int p = 0; p < funDecl.numPars(); p++)
			funDecl.par(p).accept(this);
		funDecl.type.accept(this);
	}

	@Override
	public void visit(FunDef funDef) {
		try {
			Frame frame = attrs.frmAttr.get(funDef);
			int FP = TEMP.newTempName();
			int RV = TEMP.newTempName();
			CodeFragment tmpFragment = new CodeFragment(frame, FP, RV, null);
			codeFragments.push(tmpFragment);
	
			for (int p = 0; p < funDef.numPars(); p++) {
				funDef.par(p).accept(this);
			}
			funDef.type.accept(this);
			funDef.body.accept(this);
	
			codeFragments.pop();
			IMCExpr expr = (IMCExpr) attrs.imcAttr.get(funDef.body);
			MOVE move = new MOVE(new TEMP(RV), expr);
			Fragment fragment = new CodeFragment(tmpFragment.frame, tmpFragment.FP, tmpFragment.RV, move);
			attrs.frgAttr.set(funDef, fragment);
			attrs.imcAttr.set(funDef, move);
			fragments.put(fragment.label, fragment);
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + funDef.toString());
		}
	}

	public void visit(IfExpr ifExpr) {
		try {
			ifExpr.cond.accept(this);
			ifExpr.thenExpr.accept(this);
			ifExpr.elseExpr.accept(this);
			
			IMCExpr condExpr = (IMCExpr)attrs.imcAttr.get(ifExpr.cond);
			IMCExpr thenExpr = (IMCExpr)attrs.imcAttr.get(ifExpr.thenExpr);
			IMCExpr elseExpr = (IMCExpr)attrs.imcAttr.get(ifExpr.elseExpr);
			
			LABEL thenL = new LABEL(LABEL.newLabelName());
			LABEL elseL = new LABEL(LABEL.newLabelName());
			LABEL endL = new LABEL(LABEL.newLabelName());
			Vector<IMCStmt> stmts = new Vector<IMCStmt>();
			stmts.add(new CJUMP(condExpr, thenL.label, elseL.label));
			stmts.add(elseL);
			stmts.add(new ESTMT(elseExpr));
			stmts.add(new JUMP(endL.label));
			stmts.add(thenL);
			stmts.add(new ESTMT(thenExpr));
			stmts.add(endL);
			
			attrs.imcAttr.set(ifExpr, new SEXPR(new STMTS(stmts), new NOP()));
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + ifExpr.toString());
		}
//		try {
//			ifExpr.cond.accept(this);
//			ifExpr.thenExpr.accept(this);
//			ifExpr.elseExpr.accept(this);
//			
//			IMCExpr condExpr = (IMCExpr)attrs.imcAttr.get(ifExpr.cond);
//			IMCExpr thenExpr = (IMCExpr)attrs.imcAttr.get(ifExpr.thenExpr);
//			IMCExpr elseExpr = (IMCExpr)attrs.imcAttr.get(ifExpr.elseExpr);
//			
//			LABEL thenL = new LABEL(LABEL.newLabelName());
//			LABEL elseL = new LABEL(LABEL.newLabelName());
//			LABEL endL = new LABEL(LABEL.newLabelName());
//			Vector<IMCStmt> stmts = new Vector<IMCStmt>();
//			stmts.add(new CJUMP(condExpr, thenL.label, elseL.label));
//			stmts.add(thenL);
//			stmts.add(new ESTMT(thenExpr));
//			stmts.add(new JUMP(endL.label));
//			stmts.add(elseL);
//			stmts.add(new ESTMT(elseExpr));
//			stmts.add(endL);
//			
//			attrs.imcAttr.set(ifExpr, new SEXPR(new STMTS(stmts), new NOP()));
//		} catch (Exception e) {
//			throw new CompilerError("Error during generation of intermediate code at " + ifExpr.toString());
//		}
	}

	public void visit(ParDecl parDecl) {
		parDecl.type.accept(this);
	}

	public void visit(Program program) {
		try {
			int FP = TEMP.newTempName();
			int RV = TEMP.newTempName();
			CodeFragment tmpFragment = new CodeFragment(attrs.frmAttr.get(program), FP, RV, null);
			codeFragments.push(tmpFragment);
			
			program.expr.accept(this);
			
			codeFragments.pop();
			IMCExpr expr = (IMCExpr)attrs.imcAttr.get(program.expr);
			MOVE move = new MOVE(new TEMP(RV), expr);
			Fragment fragment = new CodeFragment(tmpFragment.frame, tmpFragment.FP, tmpFragment.RV, new ESTMT(expr));
			attrs.frgAttr.set(program, fragment);
			attrs.imcAttr.set(program, move);
			fragments.put(fragment.label, fragment);
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + program.toString());
		}
	}

	public void visit(PtrType ptrType) {
		ptrType.baseType.accept(this);
	}

	public void visit(RecType recType) {
		for (int c = 0; c < recType.numComps(); c++)
			recType.comp(c).accept(this);
	}

	public void visit(TypeDecl typDecl) {
		typDecl.type.accept(this);
	}
	
	public void visit(TypeError typeError) {
	}

	public void visit(TypeName typeName) {
	}

	public void visit(UnExpr unExpr) {
		try {
			unExpr.subExpr.accept(this);
			IMCExpr imcExpr = (IMCExpr) attrs.imcAttr.get(unExpr.subExpr);
			
			if(unExpr.oper.equals(UnExpr.Oper.ADD)) {
				UNOP unop = new UNOP(UNOP.Oper.ADD, imcExpr);
				attrs.imcAttr.set(unExpr, unop);
			} else if(unExpr.oper.equals(UnExpr.Oper.SUB)) {
				UNOP unop = new UNOP(UNOP.Oper.SUB, imcExpr);
				attrs.imcAttr.set(unExpr, unop);
			} else if(unExpr.oper.equals(UnExpr.Oper.NOT)) {
				UNOP unop = new UNOP(UNOP.Oper.NOT, imcExpr);
				attrs.imcAttr.set(unExpr, unop);
			} else if(unExpr.oper.equals(UnExpr.Oper.MEM)) {
				if(imcExpr instanceof MEM) {
					MEM mem = (MEM) imcExpr;
					attrs.imcAttr.set(unExpr, mem.addr);
				} else {
					throw new CompilerError("Expression at " + unExpr.subExpr.toString() + " must have a MEM as top IMCExpr");
				}
			} else if(unExpr.oper.equals(UnExpr.Oper.VAL)) {
				long accSize = 8;
				if(!(imcExpr instanceof MEM)) {
					accSize = attrs.typAttr.get(unExpr.subExpr).size();
				}
				MEM mem = new MEM(imcExpr, accSize);
				attrs.imcAttr.set(unExpr, mem);
			}
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + unExpr.toString());
		}
	}

	public void visit(VarDecl varDecl) {
		try {
			varDecl.type.accept(this);
			
			Access acc = attrs.accAttr.get(varDecl);
			if(acc instanceof StaticAccess) {
				String label = ((StaticAccess) acc).label;
				DataFragment fragment = new DataFragment(label, acc.size);
				attrs.frgAttr.set(varDecl, fragment);
				fragments.put(fragment.label, fragment);
			}
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + varDecl.toString());
		}
	}

	public void visit(VarName varName) {
		try {
			VarDecl varDecl = (VarDecl)attrs.declAttr.get(varName);
			Access acc = attrs.accAttr.get(varDecl);
			if(acc instanceof StaticAccess) {
				MEM mem = new MEM(new NAME(((StaticAccess) acc).label), acc.size);
				attrs.imcAttr.set(varName, mem);
			} else {
				OffsetAccess offAcc = (OffsetAccess) acc;
	//			int frameIndex = codeFragments.size()-1-(codeFragments.peek().frame.level+1-offAcc.level);
	//			int FP = codeFragments.get(frameIndex).FP;
				int varLevel = offAcc.level;
				long varSize = offAcc.size;
				long varOffset = offAcc.offset;
				int diff = codeFragments.peek().frame.level - varLevel;
	//			System.out.println(varDecl.name + ", " + diff);
				IMCExpr variable = null;
				if (diff >= -1) {
					variable = new TEMP(codeFragments.peek().FP);
				}
				for (int i = diff; i > -1; i--) {
					// to make correct size of top MEM
					if (i == 0) {
						variable = new MEM(variable, varSize);
					} else {
						// 8 is size of pointer
						variable = new MEM(variable, 8);
					}
				}
				if(variable == null) {
					throw new CompilerError("Invalid level of variable name at " + varName.toString());
				}
				MEM mem = new MEM(new BINOP(BINOP.Oper.ADD, variable, new CONST(varOffset)), acc.size);
				attrs.imcAttr.set(varName, mem);
			}
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + varName.toString());
		}
	}

	public void visit(WhereExpr whereExpr) {
		try {
			for (int d = 0; d < whereExpr.numDecls(); d++) {
				whereExpr.decl(d).accept(this);
			}
			whereExpr.expr.accept(this);
			
			attrs.imcAttr.set(whereExpr, attrs.imcAttr.get(whereExpr.expr));
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + whereExpr.toString());
		}
	}

	public void visit(WhileExpr whileExpr) {
		try {
			whileExpr.cond.accept(this);
			whileExpr.body.accept(this);
			
			IMCExpr condExpr = (IMCExpr)attrs.imcAttr.get(whileExpr.cond);
			IMCExpr bodyExpr = (IMCExpr)attrs.imcAttr.get(whileExpr.body);
			
			LABEL entry = new LABEL(LABEL.newLabelName());
			LABEL loop = new LABEL(LABEL.newLabelName());
			LABEL exit = new LABEL(LABEL.newLabelName());
			Vector<IMCStmt> stmts = new Vector<IMCStmt>();
			stmts.add(entry);
			stmts.add(new CJUMP(condExpr, loop.label, exit.label));
			stmts.add(new JUMP(exit.label));
			stmts.add(loop);
			stmts.add(new ESTMT(bodyExpr));
			stmts.add(new JUMP(entry.label));
			stmts.add(exit);
			
			attrs.imcAttr.set(whileExpr, new SEXPR(new STMTS(stmts), new NOP()));
		} catch (Exception e) {
			throw new CompilerError("Error during generation of intermediate code at " + whileExpr.toString());
		}
	}
	
}
