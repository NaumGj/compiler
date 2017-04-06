package compiler.phase.seman;

import compiler.common.report.SemanticError;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.typ.*;

/**
 * @author sliva
 */
public class EvalMem extends FullVisitor {
	
	private final Attributes attrs;
	
	public EvalMem(Attributes attrs) {
		this.attrs = attrs;
	}
	
	public void visit(ArrType arrType) {
		arrType.size.accept(this);
		arrType.elemType.accept(this);
	}

	public void visit(AtomExpr atomExpr) {
		this.attrs.memAttr.set(atomExpr, false);
	}

	public void visit(AtomType atomType) {
	}

	public void visit(BinExpr binExpr) {
		binExpr.fstExpr.accept(this);
		binExpr.sndExpr.accept(this);
		if (binExpr.oper.equals(BinExpr.Oper.ARR)) {
			this.attrs.memAttr.set(binExpr, true);
		} else if (binExpr.oper.equals(BinExpr.Oper.ASSIGN)) {
			if(!this.attrs.memAttr.get(binExpr.fstExpr)) {
				throw new SemanticError("Semantic error: expression at " + binExpr.fstExpr.toString() + " must denote an object in memory");
			}
			this.attrs.memAttr.set(binExpr, false);
		} else if (binExpr.oper.equals(BinExpr.Oper.REC)) {
			this.attrs.memAttr.set(binExpr, true);
		} else {
			this.attrs.memAttr.set(binExpr, false);
		}
	}

	public void visit(CastExpr castExpr) {
		castExpr.type.accept(this);
		castExpr.expr.accept(this);
		this.attrs.memAttr.set(castExpr, false);
	}

	public void visit(CompDecl compDecl) {
		compDecl.type.accept(this);
	}

	public void visit(CompName compName) {
		this.attrs.memAttr.set(compName, false);
	}
	
	public void visit(DeclError declError) {
	}

	public void visit(Exprs exprs) {
		for (int e = 0; e < exprs.numExprs(); e++) {
			exprs.expr(e).accept(this);
		}
		this.attrs.memAttr.set(exprs, false);
	}

	public void visit(ExprError exprError) {
		this.attrs.memAttr.set(exprError, false);
	}

	public void visit(ForExpr forExpr) {
		forExpr.var.accept(this);
		forExpr.loBound.accept(this);
		forExpr.hiBound.accept(this);
		forExpr.body.accept(this);
		this.attrs.memAttr.set(forExpr, false);
	}

	public void visit(FunCall funCall) {
		for (int a = 0; a < funCall.numArgs(); a++) {
			funCall.arg(a).accept(this);
		}
		this.attrs.memAttr.set(funCall, false);
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
		this.attrs.memAttr.set(ifExpr, false);
	}

	public void visit(ParDecl parDecl) {
		parDecl.type.accept(this);
	}

	public void visit(Program program) {
		program.expr.accept(this);
		this.attrs.memAttr.set(program, false);
	}

	public void visit(PtrType ptrType) {
		ptrType.baseType.accept(this);
	}

	public void visit(RecType recType) {
		for (int c = 0; c < recType.numComps(); c++) {
			recType.comp(c).accept(this);
		}
	}

	public void visit(TypeDecl typDecl) {
		typDecl.type.accept(this);
	}
	
	public void visit(TypeError typeError) {
	}

	public void visit(TypeName typeName) {
	}

	public void visit(UnExpr unExpr) {
		unExpr.subExpr.accept(this);
		if(unExpr.oper.equals(UnExpr.Oper.VAL)) {
			this.attrs.memAttr.set(unExpr, true);
		} else if(unExpr.oper.equals(UnExpr.Oper.MEM)) {
			if(!this.attrs.memAttr.get(unExpr.subExpr)) {
				throw new SemanticError("Semantic error: expression at " + unExpr.subExpr.toString() + " must denote an object in memory");
			}
			this.attrs.memAttr.set(unExpr, false);
		} else {
			this.attrs.memAttr.set(unExpr, false);
		}
	}

	public void visit(VarDecl varDecl) {
		varDecl.type.accept(this);
	}

	public void visit(VarName varName) {
		this.attrs.memAttr.set(varName, true);
	}

	public void visit(WhereExpr whereExpr) {
		whereExpr.expr.accept(this);
		for (int d = 0; d < whereExpr.numDecls(); d++) {
			whereExpr.decl(d).accept(this);
		}
		this.attrs.memAttr.set(whereExpr, false);
	}

	public void visit(WhileExpr whileExpr) {
		whileExpr.cond.accept(this);
		whileExpr.body.accept(this);
		this.attrs.memAttr.set(whileExpr, false);
	}
}
