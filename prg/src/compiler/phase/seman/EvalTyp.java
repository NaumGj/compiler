package compiler.phase.seman;

import java.util.LinkedList;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.typ.*;

/**
 * Type checker.
 * 
 * <p>
 * Type checker checks type of all sentential forms of the program and resolves
 * the component names as this cannot be done earlier, i.e., in
 * {@link compiler.phase.seman.EvalDecl}.
 * </p>
 * 
 * @author sliva
 */
public class EvalTyp extends FullVisitor {

	private final Attributes attrs;
	private int run = 2;
	
	public EvalTyp(Attributes attrs) {
		this.attrs = attrs;
	}
	
	/** The symbol table. */
	private SymbolTable symbolTable = new SymbolTable();
	
	private String lastRecNamespace = "#";

	public void visit(ArrType arrType) {
		try {
			arrType.size.accept(this);
			arrType.elemType.accept(this);
			if(this.attrs.valueAttr.get(arrType.size) > 0) {
				this.attrs.typAttr.set(arrType, new ArrTyp(this.attrs.valueAttr.get(arrType.size), this.attrs.typAttr.get(arrType.elemType)));
			} else {
				throw new SemanticError("Semantic error: invalid size of array at " + arrType.toString());
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid array expression at " + arrType.toString());
		}
	}

	public void visit(AtomExpr atomExpr) {
		try {
			if(atomExpr.type.equals(AtomExpr.AtomTypes.BOOLEAN)) {
				this.attrs.typAttr.set(atomExpr, new BooleanTyp());
			} else if(atomExpr.type.equals(AtomExpr.AtomTypes.INTEGER)) {
				this.attrs.typAttr.set(atomExpr, new IntegerTyp());
			} else if(atomExpr.type.equals(AtomExpr.AtomTypes.CHAR)) {
				this.attrs.typAttr.set(atomExpr, new CharTyp());
			} else if(atomExpr.type.equals(AtomExpr.AtomTypes.STRING)) {
				this.attrs.typAttr.set(atomExpr, new StringTyp());
			} else if(atomExpr.type.equals(AtomExpr.AtomTypes.PTR)) {
				this.attrs.typAttr.set(atomExpr, new PtrTyp(new VoidTyp()));
			} else if(atomExpr.type.equals(AtomExpr.AtomTypes.VOID)) {
				this.attrs.typAttr.set(atomExpr, new VoidTyp());
			} else {
				throw new SemanticError("Semantic error: invalid atom expression at " + atomExpr.toString());
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid atom expression at " + atomExpr.toString());
		}
	}

	public void visit(AtomType atomType) {
		try {
			if(atomType.type.equals(AtomType.AtomTypes.BOOLEAN)) {
				this.attrs.typAttr.set(atomType, new BooleanTyp());
			} else if(atomType.type.equals(AtomType.AtomTypes.INTEGER)) {
				this.attrs.typAttr.set(atomType, new IntegerTyp());
			} else if(atomType.type.equals(AtomType.AtomTypes.CHAR)) {
				this.attrs.typAttr.set(atomType, new CharTyp());
			} else if(atomType.type.equals(AtomType.AtomTypes.STRING)) {
				this.attrs.typAttr.set(atomType, new StringTyp());
			} else if(atomType.type.equals(AtomType.AtomTypes.VOID)) {
				this.attrs.typAttr.set(atomType, new VoidTyp());
			} else {
				throw new SemanticError("Semantic error: invalid atom type at " + atomType.toString());
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid atom type at " + atomType.toString());
		}
	}

	public void visit(BinExpr binExpr) {
		try {
//			if(run == 2) {
				if(binExpr.oper.equals(BinExpr.Oper.ADD) || binExpr.oper.equals(BinExpr.Oper.SUB) || 
						binExpr.oper.equals(BinExpr.Oper.MUL) || binExpr.oper.equals(BinExpr.Oper.DIV) || binExpr.oper.equals(BinExpr.Oper.MOD)) {
					binExpr.fstExpr.accept(this);
					binExpr.sndExpr.accept(this);
					if(!(this.attrs.typAttr.get(binExpr.fstExpr).actualTyp() instanceof IntegerTyp)) {
						throw new SemanticError("Semantic error: expression at " + binExpr.fstExpr.toString() + " must be integer");
					}
					if(!(this.attrs.typAttr.get(binExpr.sndExpr).actualTyp() instanceof IntegerTyp)) {
						throw new SemanticError("Semantic error: expression at " + binExpr.sndExpr.toString() + " must be integer");
					}
					this.attrs.typAttr.set(binExpr, new IntegerTyp());
				} else if(binExpr.oper.equals(BinExpr.Oper.AND) || binExpr.oper.equals(BinExpr.Oper.OR)) {
					binExpr.fstExpr.accept(this);
					binExpr.sndExpr.accept(this);
					if(!(this.attrs.typAttr.get(binExpr.fstExpr).actualTyp() instanceof BooleanTyp)) {
						throw new SemanticError("Semantic error: expression at " + binExpr.fstExpr.toString() + " must be boolean");
					}
					if(!(this.attrs.typAttr.get(binExpr.sndExpr).actualTyp() instanceof BooleanTyp)) {
						throw new SemanticError("Semantic error: expression at " + binExpr.sndExpr.toString() + " must be boolean");
					}
					this.attrs.typAttr.set(binExpr, new BooleanTyp());
				} else if(binExpr.oper.equals(BinExpr.Oper.EQU) || binExpr.oper.equals(BinExpr.Oper.NEQ) ||
						binExpr.oper.equals(BinExpr.Oper.LTH) || binExpr.oper.equals(BinExpr.Oper.LEQ) || 
						binExpr.oper.equals(BinExpr.Oper.GTH) || binExpr.oper.equals(BinExpr.Oper.GEQ)) {
					binExpr.fstExpr.accept(this);
					binExpr.sndExpr.accept(this);
					if(Typ.equiv(this.attrs.typAttr.get(binExpr.fstExpr), this.attrs.typAttr.get(binExpr.sndExpr))) {
						if(this.attrs.typAttr.get(binExpr.fstExpr).actualTyp() instanceof BooleanTyp ||
								this.attrs.typAttr.get(binExpr.fstExpr).actualTyp() instanceof IntegerTyp ||
								this.attrs.typAttr.get(binExpr.fstExpr).actualTyp() instanceof CharTyp ||
								this.attrs.typAttr.get(binExpr.fstExpr).actualTyp() instanceof PtrTyp) {
							this.attrs.typAttr.set(binExpr, new BooleanTyp());
						} else {
							throw new SemanticError("Semantic error: expressions at " + binExpr.fstExpr.toString() + " and " + binExpr.sndExpr.toString() + "must be of boolean, integer, char or pointer type");
						}
					} else {
						throw new SemanticError("Semantic error: expressions at " + binExpr.fstExpr.toString() + " and " + binExpr.sndExpr.toString() + "must be of the same type");
					}
				} else if (binExpr.oper.equals(BinExpr.Oper.ASSIGN)) {
					binExpr.fstExpr.accept(this);
					binExpr.sndExpr.accept(this);
					Typ fstTyp = this.attrs.typAttr.get(binExpr.fstExpr);
					Typ sndTyp = this.attrs.typAttr.get(binExpr.sndExpr);
					if(!((fstTyp.actualTyp() instanceof AssignableTyp) && (sndTyp.actualTyp() instanceof AssignableTyp))) {
						throw new SemanticError("Semantic error: expressions at " + binExpr + " must be assignable (boolean, integer, char, string or pointer)");
					}
					if(!Typ.equiv(fstTyp, sndTyp)) {
						throw new SemanticError("Semantic error: expressions at " + binExpr.fstExpr + " and " + binExpr.sndExpr + " have different types");
					}
					this.attrs.typAttr.set(binExpr, new VoidTyp());
				} else if (binExpr.oper.equals(BinExpr.Oper.REC)) {
					binExpr.fstExpr.accept(this);
					if(!(this.attrs.typAttr.get(binExpr.fstExpr).actualTyp() instanceof RecTyp)) {
						throw new SemanticError("Semantic error: expression at " + binExpr.fstExpr.toString() + " must be a record");
					}
					RecTyp recTyp = (RecTyp)this.attrs.typAttr.get(binExpr.fstExpr).actualTyp();
					this.lastRecNamespace = recTyp.nameSpace;
					binExpr.sndExpr.accept(this);
					this.attrs.typAttr.set(binExpr, this.attrs.typAttr.get(binExpr.sndExpr));
				} else if (binExpr.oper.equals(BinExpr.Oper.ARR)) {
					binExpr.fstExpr.accept(this);
					binExpr.sndExpr.accept(this);
					ArrTyp arrTyp = (ArrTyp)this.attrs.typAttr.get(binExpr.fstExpr).actualTyp();
					if(!(arrTyp instanceof ArrTyp)) {
						throw new SemanticError("Semantic error: expression at " + binExpr.fstExpr.toString() + " must be an array");
					}
					if(!(this.attrs.typAttr.get(binExpr.sndExpr).actualTyp() instanceof IntegerTyp)) {
						throw new SemanticError("Semantic error: expression at " + binExpr.sndExpr.toString() + " must be integer");
					}
//					if(this.attrs.valueAttr.get(binExpr.sndExpr) > arrTyp.size) {
//						throw new SemanticError("Semantic error: array index out of bounds at " + binExpr);
//					}
					this.attrs.typAttr.set(binExpr, arrTyp.elemTyp);
				}
//			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new SemanticError("Semantic error: invalid binary expression at " + binExpr.toString());
		}
	}

	public void visit(CastExpr castExpr) {
		try{
			if(run == 2) {
				castExpr.type.accept(this);
				castExpr.expr.accept(this);
//				System.out.println(this.attrs.typAttr.get(castExpr.type));
				if(!(this.attrs.typAttr.get(castExpr.type).actualTyp() instanceof PtrTyp)) {
					throw new SemanticError("Semantic error: expression at " + castExpr.type.toString() + " must be a pointer");
				}
				if(!(this.attrs.typAttr.get(castExpr.expr).actualTyp() instanceof PtrTyp)) {
					throw new SemanticError("Semantic error: expression at " + castExpr.expr.toString() + " must be a pointer to void");
				}
				PtrTyp ptrTyp = (PtrTyp)this.attrs.typAttr.get(castExpr.expr).actualTyp();
				if(!(ptrTyp.baseTyp.actualTyp() instanceof VoidTyp)) {
					throw new SemanticError("Semantic error: expression at " + castExpr.expr.toString() + " must be a pointer to void");
				}
				this.attrs.typAttr.set(castExpr, this.attrs.typAttr.get(castExpr.type));
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid cast expression at " + castExpr.toString());
		}
	}

	public void visit(CompDecl compDecl) {
		try {
			compDecl.type.accept(this);
			try {
				symbolTable.insDecl(symbolTable.getTopNamespace(), compDecl.name, compDecl);
			} catch (CannotInsNameDecl e) {
				throw new SemanticError("Semantic error: cannot insert component declaration at " + compDecl.toString());
			}
			this.attrs.typAttr.set(compDecl, this.attrs.typAttr.get(compDecl.type));
		} catch(Exception e) {
			throw new SemanticError("Semantic error: invalid component declaration at " + compDecl.toString());
		}
	}

	public void visit(CompName compName) {
		try {
			this.attrs.declAttr.set(compName, symbolTable.fndDecl(lastRecNamespace, compName.name()));
			this.attrs.typAttr.set(compName, this.attrs.typAttr.get(symbolTable.fndDecl(lastRecNamespace, compName.name())));
		} catch (CannotFndNameDecl e) {
			throw new SemanticError("Semantic error: cannot find component declaration at " + compName.toString());
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid component name at " + compName.toString());
		}
	}
	
	public void visit(DeclError declError) {
	}

	public void visit(Exprs exprs) {
		try{
			for (int e = 0; e < exprs.numExprs(); e++) {
				exprs.expr(e).accept(this);
				if(this.attrs.typAttr.get(exprs.expr(e)) == null) {
					throw new SemanticError("Semantic error: one of the expressions, at " + exprs.expr(e).toString() + ", has undefined type");
				}
			}
			this.attrs.typAttr.set(exprs, this.attrs.typAttr.get(exprs.expr(exprs.numExprs() - 1)));
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid expressions at " + exprs.toString());
		}
	}

	public void visit(ExprError exprError) {
	}

	public void visit(ForExpr forExpr) {
		try {
			if(run == 2) {
				forExpr.var.accept(this);
				forExpr.loBound.accept(this);
				forExpr.hiBound.accept(this);
				forExpr.body.accept(this);
				if(!(this.attrs.typAttr.get(forExpr.var).actualTyp() instanceof IntegerTyp)) {
					throw new SemanticError("Semantic error: variable name in for expression at " + forExpr.toString() + " is not an integer");
				}
				if(!(this.attrs.typAttr.get(forExpr.loBound).actualTyp() instanceof IntegerTyp)) {
					throw new SemanticError("Semantic error: low bound in for expression at " + forExpr.toString() + " is not an integer");
				}
				if(!(this.attrs.typAttr.get(forExpr.hiBound).actualTyp() instanceof IntegerTyp)) {
					throw new SemanticError("Semantic error: high bound in for expression at " + forExpr.toString() + " is not an integer");
				}
				if(this.attrs.typAttr.get(forExpr.body) == null) {
					throw new SemanticError("Semantic error: body in for expression at " + forExpr.toString() + " is undefined (null)");
				}
				this.attrs.typAttr.set(forExpr, new VoidTyp());
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid for expression at " + forExpr.toString());
		}
	}

	public void visit(FunCall funCall) {
		try {
			if (run == 2) {
				FunDecl funDecl = null;
				try{
					funDecl = (FunDecl)this.attrs.declAttr.get(funCall);
				} catch (Exception e) {
					throw new SemanticError("Semantic error: can't find function declaration at " + funCall.toString());
				}
				FunTyp funTyp = (FunTyp)this.attrs.typAttr.get(funDecl).actualTyp();
				for (int a = 0; a < funCall.numArgs(); a++) {
					funCall.arg(a).accept(this);
					if(!this.attrs.typAttr.get(funCall.arg(a)).isStructEquivTo(funTyp.parTyp(a))) {
						throw new SemanticError("Semantic error: argument type doesn't match with the function declaration / definition, at " + funCall.arg(a).toString());
					}
				}
				this.attrs.typAttr.set(funCall, funTyp.resultTyp);
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid function call at " + funCall.toString());
		}
	}

	public void visit(FunDecl funDecl) {
		try {
			if (run == 1) {
				LinkedList<Typ> parTyps = new LinkedList<Typ>();
				for (int p = 0; p < funDecl.numPars(); p++) {
					funDecl.par(p).accept(this);
					parTyps.add(this.attrs.typAttr.get(funDecl.par(p)));
				}
				funDecl.type.accept(this);
				FunTyp funTyp = new FunTyp(parTyps, this.attrs.typAttr.get(funDecl.type));
				this.attrs.typAttr.set(funDecl, funTyp);
			} else if (run == 2) {
				Typ typ = this.attrs.typAttr.get(funDecl.type).actualTyp();
				if(!(typ instanceof ReturnableTyp)) {
					throw new SemanticError("Semantic error: invalid function return type at " + funDecl.toString());
				}
				for (int p = 0; p < funDecl.numPars(); p++) {
					funDecl.par(p).accept(this);
				}
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid function declaration at " + funDecl.toString());
		}
	}

	public void visit(FunDef funDef) {
		try {
			if(run == 1) {
				LinkedList<Typ> parTyps = new LinkedList<Typ>();
				for (int p = 0; p < funDef.numPars(); p++) {
					funDef.par(p).accept(this);
					parTyps.add(this.attrs.typAttr.get(funDef.par(p)));
				}
				funDef.type.accept(this);
				FunTyp funTyp = new FunTyp(parTyps, this.attrs.typAttr.get(funDef.type));
				this.attrs.typAttr.set(funDef, funTyp);
			} else if (run == 2) {
				Typ typ = this.attrs.typAttr.get(funDef.type).actualTyp();
				if(!(typ instanceof ReturnableTyp)) {
					throw new SemanticError("Semantic error: invalid function return type at " + funDef.toString());
				}
				for (int p = 0; p < funDef.numPars(); p++) {
					funDef.par(p).accept(this);
				}
				funDef.body.accept(this);
				FunTyp funTyp = (FunTyp)this.attrs.typAttr.get(funDef).actualTyp();
				if (!funTyp.resultTyp.isStructEquivTo(this.attrs.typAttr.get(funDef.body))) {
					throw new SemanticError("Semantic error: function body returns different type than function return type, at " + funDef.toString());
				}
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid function definition at " + funDef.toString());
		}
	}

	public void visit(IfExpr ifExpr) {
		try{
			if(run == 2){
				ifExpr.cond.accept(this);
				ifExpr.thenExpr.accept(this);
				ifExpr.elseExpr.accept(this);
				if(!(this.attrs.typAttr.get(ifExpr.cond).actualTyp() instanceof BooleanTyp)) {
					throw new SemanticError("Semantic error: condition in if expression at " + ifExpr.toString() + " is not a boolean");
				}
				if(this.attrs.typAttr.get(ifExpr.thenExpr) == null) {
					throw new SemanticError("Semantic error: then body in if expression at " + ifExpr.toString() + " is undefined (null)");
				}
				if(this.attrs.typAttr.get(ifExpr.elseExpr) == null) {
					throw new SemanticError("Semantic error: else body in if expression at " + ifExpr.toString() + " is undefined (null)");
				}
				this.attrs.typAttr.set(ifExpr, new VoidTyp());
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid if expression at " + ifExpr.toString());
		}
	}

	public void visit(ParDecl parDecl) {
		try {
			if(run == 1) {
				parDecl.type.accept(this);
				this.attrs.typAttr.set(parDecl, this.attrs.typAttr.get(parDecl.type));
			} else if (run == 2) {
				Typ parTyp = this.attrs.typAttr.get(parDecl.type).actualTyp();
				if(!(parTyp instanceof PassableTyp)) {
					throw new SemanticError("Semantic error: invalid parameter type at " + parDecl.toString());
				}
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid parameter declaration at " + parDecl.toString());
		}
	}

	public void visit(Program program) {
		try{
			program.expr.accept(this);
			this.attrs.typAttr.set(program, this.attrs.typAttr.get(program.expr));
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid program at " + program.toString());
		}
	}

	public void visit(PtrType ptrType) {
		try{
			ptrType.baseType.accept(this);
			this.attrs.typAttr.set(ptrType, new PtrTyp(this.attrs.typAttr.get(ptrType.baseType)));
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid pointer type at " + ptrType.toString());
		}
	}

	public void visit(RecType recType) {
		try {
			String nameSpace = symbolTable.newNamespace(recType.toString());
			symbolTable.enterNamespace(nameSpace);
			LinkedList<Typ> compTyps = new LinkedList<Typ>();
			for (int c = 0; c < recType.numComps(); c++) {
				recType.comp(c).accept(this);
				compTyps.add(this.attrs.typAttr.get(recType.comp(c)));
			}
			this.attrs.typAttr.set(recType, new RecTyp(nameSpace, compTyps));
			symbolTable.leaveNamespace();
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid record type at " + recType.toString());
		}
	}

	public void visit(TypeDecl typDecl) {
		try {
			if(run == 0) {
				TypName tName = new TypName(typDecl.name);
				this.attrs.typAttr.set(typDecl, tName);
			} else if (run == 1) {
				typDecl.type.accept(this);
				TypName tName = (TypName)this.attrs.typAttr.get(typDecl);
				tName.setType(this.attrs.typAttr.get(typDecl.type));
			} else if (run == 2) {
				TypName tName = (TypName)this.attrs.typAttr.get(typDecl);
				if(tName.isCircular()) {
//					Report.warning("Warning: type at " + typDecl.toString() + " is circular");
					throw new SemanticError("Semantic error: circular type at " + typDecl.toString());
				}
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid type declaration at " + typDecl.toString());
		}
	}
	
	public void visit(TypeError typeError) {
	}

	public void visit(TypeName typeName) {
		try{
			if(run >= 1) {
				try{
					this.attrs.declAttr.get(typeName);
				} catch (Exception e) {
					throw new SemanticError("Semantic error: can't find type declaration at " + typeName.toString());
				}
				this.attrs.typAttr.set(typeName, this.attrs.typAttr.get(this.attrs.declAttr.get(typeName)));
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid type name at " + typeName.toString());
		}
	}

	public void visit(UnExpr unExpr) {
		try {
//			if(run == 2) {
				unExpr.subExpr.accept(this);
				if(unExpr.oper.equals(UnExpr.Oper.NOT)) {
					if(this.attrs.typAttr.get(unExpr.subExpr).actualTyp() instanceof BooleanTyp) {
						this.attrs.typAttr.set(unExpr, new BooleanTyp());
					} else {
						throw new SemanticError("Semantic error: expression at " + unExpr.subExpr.toString() + " must be boolean");
					}
				} else if(unExpr.oper.equals(UnExpr.Oper.ADD) || unExpr.oper.equals(UnExpr.Oper.SUB)) {
					if(this.attrs.typAttr.get(unExpr.subExpr).actualTyp() instanceof IntegerTyp) {
						this.attrs.typAttr.set(unExpr, new IntegerTyp());
					} else {
						throw new SemanticError("Semantic error: expression at " + unExpr.subExpr.toString() + " must be integer");
					}
				} else if(unExpr.oper.equals(UnExpr.Oper.MEM)) {
					this.attrs.typAttr.set(unExpr, new PtrTyp(this.attrs.typAttr.get(unExpr.subExpr)));
				} else if(unExpr.oper.equals(UnExpr.Oper.VAL)) {
					if(this.attrs.typAttr.get(unExpr.subExpr).actualTyp() instanceof PtrTyp) {
						PtrTyp ptrTyp = (PtrTyp)this.attrs.typAttr.get(unExpr.subExpr).actualTyp();
						this.attrs.typAttr.set(unExpr, ptrTyp.baseTyp);
					} else {
						throw new SemanticError("Semantic error: expression at " + unExpr.subExpr.toString() + " must be pointer");
					}
				}
//			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid unary expression at " + unExpr.toString());
		}
	}

	public void visit(VarDecl varDecl) {
		try {
			if(run == 1) {
				varDecl.type.accept(this);
				this.attrs.typAttr.set(varDecl, this.attrs.typAttr.get(varDecl.type));
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid variable declaration at " + varDecl.toString());
		}
	}

	public void visit(VarName varName) {
		try {
			if(run == 2) {
				try {
					this.attrs.declAttr.get(varName);
				} catch (Exception e) {
					throw new SemanticError("Semantic error: can't find variable declaration at " + varName.toString());
				}
				this.attrs.typAttr.set(varName, this.attrs.typAttr.get(this.attrs.declAttr.get(varName)));
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid variable name at " + varName.toString());
		}
	}

	public void visit(WhereExpr whereExpr) {
		try {
			for (int i = 0; i < 3; i++) {
				run = i;
				for (int d = 0; d < whereExpr.numDecls(); d++) {
					whereExpr.decl(d).accept(this);
				}
			}
			whereExpr.expr.accept(this);
			for (int d = 0; d < whereExpr.numDecls(); d++) {
				if(this.attrs.typAttr.get(whereExpr.decl(d)) == null) {
					throw new SemanticError("Semantic error: declaration in where expression at " + whereExpr.toString() + " is undefined (null)");
				}
			}
			this.attrs.typAttr.set(whereExpr, this.attrs.typAttr.get(whereExpr.expr));
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid where expression at " + whereExpr.toString());
		}
	}

	public void visit(WhileExpr whileExpr) {
		try {
			if(run == 2) {
				whileExpr.cond.accept(this);
				whileExpr.body.accept(this);
				if(!(this.attrs.typAttr.get(whileExpr.cond).actualTyp() instanceof BooleanTyp)) {
					throw new SemanticError("Semantic error: condition in while expression at " + whileExpr.toString() + " is not a boolean");
				}
				if(this.attrs.typAttr.get(whileExpr.body) == null) {
					throw new SemanticError("Semantic error: body in while expression at " + whileExpr.toString() + " is undefined (null)");
				}
				this.attrs.typAttr.set(whileExpr, new VoidTyp());
			}
		} catch (Exception e) {
			throw new SemanticError("Semantic error: invalid while expression at " + whileExpr.toString());
		}
	}

}
