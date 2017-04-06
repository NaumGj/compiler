package compiler.phase.seman;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;

/**
 * Declaration resolver.
 * 
 * <p>
 * Declaration resolver maps each AST node denoting a
 * {@link compiler.data.ast.Declarable} name to the declaration where
 * this name is declared. In other words, it links each use of each name to a
 * declaration of that name.
 * </p>
 * 
 * @author sliva
 */
public class EvalDecl extends FullVisitor {

	private final Attributes attrs;
	private boolean prototype = false;
	
	public EvalDecl(Attributes attrs) {
		this.attrs = attrs;
	}

	/** The symbol table. */
	private SymbolTable symbolTable = new SymbolTable();
	
	public void visit(ArrType arrType) {
		arrType.size.accept(this);
		arrType.elemType.accept(this);
	}

	public void visit(AtomExpr atomExpr) {
	}

	public void visit(AtomType atomType) {
	}

	public void visit(BinExpr binExpr) {
		binExpr.fstExpr.accept(this);
		binExpr.sndExpr.accept(this);
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
		try {
			this.attrs.declAttr.set(funCall, symbolTable.fndDecl(funCall.name()));
		} catch (CannotFndNameDecl e) {
			throw new SemanticError("Cannot find function declaration at " + funCall.toString());
		}
		for (int a = 0; a < funCall.numArgs(); a++)
			funCall.arg(a).accept(this);
	}

	public void visit(FunDecl funDecl) {
		if(prototype) {
			try {
				symbolTable.insDecl(funDecl.name, funDecl);
			} catch (CannotInsNameDecl e1) {
				throw new SemanticError("Cannot insert function declaration at " + funDecl.toString());
			}
		} else {
			funDecl.type.accept(this);
			symbolTable.enterScope();
			
			for (int p = 0; p < funDecl.numPars(); p++){
				funDecl.par(p).accept(this);
			}
			
			symbolTable.leaveScope();
		}
	}

	public void visit(FunDef funDef) {
		if(prototype) {
			try {
				symbolTable.insDecl(funDef.name, funDef);
			} catch (CannotInsNameDecl e1) {
				throw new SemanticError("Cannot insert function declaration at " + funDef.toString());
			}
		} else {
			funDef.type.accept(this);
			symbolTable.enterScope();
				
			for (int p = 0; p < funDef.numPars(); p++){
				funDef.par(p).accept(this);
			}
			funDef.body.accept(this);
				
			symbolTable.leaveScope();
		}
	}

	public void visit(IfExpr ifExpr) {
		ifExpr.cond.accept(this);
		ifExpr.thenExpr.accept(this);
		ifExpr.elseExpr.accept(this);
	}

	public void visit(ParDecl parDecl) {
		parDecl.type.accept(this);
		
		try {
			symbolTable.insDecl(parDecl.name, parDecl);
		} catch (CannotInsNameDecl e) {
			throw new SemanticError("Cannot insert parameter declaration at " + parDecl.toString());
		}
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
		if(prototype) {
			try {
				symbolTable.insDecl(typDecl.name, typDecl);
			} catch (CannotInsNameDecl e) {
				throw new SemanticError("Cannot insert type declaration at " + typDecl.toString());
			}
		} else {
			typDecl.type.accept(this);
		}
	}
	
	public void visit(TypeError typeError) {
	}

	public void visit(TypeName typeName) {
		try {
			this.attrs.declAttr.set(typeName, symbolTable.fndDecl(typeName.name()));
		} catch (CannotFndNameDecl e) {
			throw new SemanticError("Cannot find type declaration at " + typeName.toString());
		}
	}

	public void visit(UnExpr unExpr) {
		unExpr.subExpr.accept(this);
	}

	public void visit(VarDecl varDecl) {
		if(prototype) {
			try {
				symbolTable.insDecl(varDecl.name, varDecl);
			} catch (CannotInsNameDecl e) {
				throw new SemanticError("Cannot insert variable declaration at " + varDecl.toString());
			}
		} else {
			varDecl.type.accept(this);
		}
	}

	public void visit(VarName varName) {
		try {
			this.attrs.declAttr.set(varName, symbolTable.fndDecl(varName.name()));
		} catch (CannotFndNameDecl e) {
			throw new SemanticError("Cannot find variable declaration at " + varName.toString());
		}
	}

	public void visit(WhereExpr whereExpr) {
		symbolTable.enterScope();
		
		prototype = true;
		for (int i = 0; i < 2; i++) {
			for (int d = 0; d < whereExpr.numDecls(); d++) {
				whereExpr.decl(d).accept(this);
			}
			prototype = false;
		}
		whereExpr.expr.accept(this);
		
		symbolTable.leaveScope();
	}

	public void visit(WhileExpr whileExpr) {
		whileExpr.cond.accept(this);
		whileExpr.body.accept(this);
	}
}
