package compiler.data.codegen.code;

import compiler.data.imc.*;

public class FullImcVisitor implements ImcVisitor {
	
	public void visit(BINOP binOp){
		binOp.expr1.accept(this);
		binOp.expr2.accept(this);
	}

	public void visit(CALL call){
		for (int i = 0; i < call.numArgs(); i++) {
			call.args(i).accept(this);
		}
	}

	public void visit(CJUMP cJump){
		cJump.cond.accept(this);
	}

	public void visit(CONST constant){
	}

	public void visit(ESTMT eStmt){
		eStmt.expr.accept(this);
	}

	public void visit(JUMP jump){
	}

	public void visit(LABEL label){
	}

	public void visit(MEM mem){
		mem.addr.accept(this);
	}

	public void visit(MOVE move){
		move.dst.accept(this);
		move.src.accept(this);
	}

	public void visit(NAME name){
	}

	public void visit(NOP nop){
	}

	public void visit(SEXPR sExpr){
		sExpr.stmt.accept(this);
		sExpr.expr.accept(this);
	}

	public void visit(STMTS stmts){
		for (int i = 0; i < stmts.numStmts(); i++) {
			stmts.stmts(i).accept(this);
		}
	}

	public void visit(TEMP temp){
	}

	public void visit(UNOP unOp){
		unOp.expr.accept(this);
	}

}
