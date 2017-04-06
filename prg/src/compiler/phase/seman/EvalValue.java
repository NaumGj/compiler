package compiler.phase.seman;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;

/**
 * Computes the value of simple integer constant expressions.
 * 
 * <p>
 * Simple integer constant expressions consists of integer constants and five
 * basic arithmetic operators (<code>ADD</code>, <code>SUB</code>,
 * <code>MUL</code>, <code>DIV</code>, and <code>MOD</code>).
 * </p>
 * 
 * <p>
 * This is needed during type resolving and type checking to compute the correct
 * array types.
 * </p>
 * 
 * @author sliva
 */
public class EvalValue extends FullVisitor {

	private final Attributes attrs;
	private boolean isNegativeUnary = false;
	
	public EvalValue(Attributes attrs) {
		this.attrs = attrs;
	}
	
	public void visit(ArrType arrType) {
		arrType.size.accept(this);
		arrType.elemType.accept(this);
	}

	public void visit(AtomExpr atomExpr) {
		if(atomExpr.type.equals(AtomExpr.AtomTypes.INTEGER)) {
			Long value;
			try {
				value = Long.parseLong((isNegativeUnary ? "-" : "") + atomExpr.value);
			} catch (NumberFormatException e) {
				throw new SemanticError("Semantic error: the integer at position " + atomExpr.toString() + "is not in the valid interval");
			}
			this.attrs.valueAttr.set(atomExpr, Math.abs(value));
		}
	}

	public void visit(AtomType atomType) {
	}

	public void visit(BinExpr binExpr) {
		binExpr.fstExpr.accept(this);
		binExpr.sndExpr.accept(this);
		if(binExpr.oper.equals(BinExpr.Oper.ADD)) {
			try {
				Long fstValue = this.attrs.valueAttr.get(binExpr.fstExpr);
				Long sndValue = this.attrs.valueAttr.get(binExpr.sndExpr);
				Long value = fstValue + sndValue;
				this.attrs.valueAttr.set(binExpr, value);
			} catch (Exception e) {
				return;
			}
		} else if(binExpr.oper.equals(BinExpr.Oper.SUB)) {
			try {
				Long fstValue = this.attrs.valueAttr.get(binExpr.fstExpr);
				Long sndValue = this.attrs.valueAttr.get(binExpr.sndExpr);
				Long value = fstValue - sndValue;
				this.attrs.valueAttr.set(binExpr, value);
			} catch (Exception e) {
				return;
			}
		} else if(binExpr.oper.equals(BinExpr.Oper.MUL)) {
			try {
				Long fstValue = this.attrs.valueAttr.get(binExpr.fstExpr);
				Long sndValue = this.attrs.valueAttr.get(binExpr.sndExpr);
				Long value = fstValue * sndValue;
				this.attrs.valueAttr.set(binExpr, value);
			} catch (Exception e) {
				return;
			}
		} else if(binExpr.oper.equals(BinExpr.Oper.DIV)) {
			try {
				Long fstValue = this.attrs.valueAttr.get(binExpr.fstExpr);
				Long sndValue = this.attrs.valueAttr.get(binExpr.sndExpr);
				Long value = fstValue / sndValue;
				this.attrs.valueAttr.set(binExpr, value);
			} catch (Exception e) {
				return;
			}
		} else if(binExpr.oper.equals(BinExpr.Oper.MOD)) {
			try {
				Long fstValue = this.attrs.valueAttr.get(binExpr.fstExpr);
				Long sndValue = this.attrs.valueAttr.get(binExpr.sndExpr);
				Long value = fstValue % sndValue;
				this.attrs.valueAttr.set(binExpr, value);
			} catch (Exception e) {
				return;
			}
		}
	}

	public void visit(CastExpr castExpr) {
		castExpr.type.accept(this);
		castExpr.expr.accept(this);
	}

	public void visit(CompDecl compDecl) {
		compDecl.type.accept(this);
	}

	public void visit(CompName compName) {
	}
	
	public void visit(DeclError declError) {
	}

	public void visit(Exprs exprs) {
		for (int e = 0; e < exprs.numExprs(); e++)
			exprs.expr(e).accept(this);
	}

	public void visit(ExprError exprError) {
	}

	public void visit(ForExpr forExpr) {
		forExpr.var.accept(this);
		forExpr.loBound.accept(this);
		forExpr.hiBound.accept(this);
		forExpr.body.accept(this);
	}

	public void visit(FunCall funCall) {
		for (int a = 0; a < funCall.numArgs(); a++)
			funCall.arg(a).accept(this);
	}

	public void visit(FunDecl funDecl) {
		for (int p = 0; p < funDecl.numPars(); p++)
			funDecl.par(p).accept(this);
		funDecl.type.accept(this);
	}

	public void visit(FunDef funDef) {
		for (int p = 0; p < funDef.numPars(); p++)
			funDef.par(p).accept(this);
		funDef.type.accept(this);
		funDef.body.accept(this);
	}

	public void visit(IfExpr ifExpr) {
		ifExpr.cond.accept(this);
		ifExpr.thenExpr.accept(this);
		ifExpr.elseExpr.accept(this);
	}

	public void visit(ParDecl parDecl) {
		parDecl.type.accept(this);
	}

	public void visit(Program program) {
		program.expr.accept(this);
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
		isNegativeUnary = false;
		if(unExpr.oper.equals(UnExpr.Oper.SUB)) {
			isNegativeUnary = true;
		}
		unExpr.subExpr.accept(this);
		Long value = null;
		value = this.attrs.valueAttr.get(unExpr.subExpr);
		if(value != null) {
			if(unExpr.oper.equals(UnExpr.Oper.ADD)) {
				try {
					this.attrs.valueAttr.set(unExpr, value);
				} catch (Exception e) {
					return;
				}
			} else if(unExpr.oper.equals(UnExpr.Oper.SUB)) {
				try {
					this.attrs.valueAttr.set(unExpr, -value);
					isNegativeUnary = false;
				} catch (Exception e) {
					return;
				}
			}
		}
	}

	public void visit(VarDecl varDecl) {
		varDecl.type.accept(this);
	}

	public void visit(VarName varName) {
	}

	public void visit(WhereExpr whereExpr) {
		whereExpr.expr.accept(this);
		for (int d = 0; d < whereExpr.numDecls(); d++)
			whereExpr.decl(d).accept(this);
	}

	public void visit(WhileExpr whileExpr) {
		whileExpr.cond.accept(this);
		whileExpr.body.accept(this);
	}

}
