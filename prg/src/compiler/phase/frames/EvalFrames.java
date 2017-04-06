package compiler.phase.frames;

import java.util.*;

import compiler.common.report.CompilerError;
import compiler.data.acc.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.frm.*;

/**
 * Frame and access evaluator.
 * 
 * @author sliva
 */
public class EvalFrames extends FullVisitor {

	private final Attributes attrs;
	private int level = 0;
	private long localOffset = 0;
	private String funPath = "";
	private long funCounter = 0;
	private long maxOutCallSize = 0;
	private Set<String> globalNames = new HashSet<String>();

	public EvalFrames(Attributes attrs) {
		this.attrs = attrs;
	}

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
		long outCallSize = 8;	// 8 for the static link
		for (int a = 0; a < funCall.numArgs(); a++) {
			funCall.arg(a).accept(this);
			outCallSize += this.attrs.typAttr.get(funCall.arg(a)).size();
		}
//		outCallSize += this.attrs.typAttr.get(this.attrs.declAttr.get(funCall).type).size();
		if(outCallSize > this.maxOutCallSize) {
			this.maxOutCallSize = outCallSize;
		}
	}

	public void visit(FunDecl funDecl) {
		for (int p = 0; p < funDecl.numPars(); p++)
			funDecl.par(p).accept(this);
		funDecl.type.accept(this);
	}

	public void visit(FunDef funDef) {
		try {
			funDef.type.accept(this);
			
			// save the old offsets and out call size
			long oldLocalOffset = this.localOffset;
			long oldOutCallSize = this.maxOutCallSize;
			// check do we have to add underscore to function path, i.e. label
			if(funPath.length() == 0) {
				funPath += funDef.name;
			} else {
				funPath += "_" + funDef.name;
			}
			// create the label
			String label = "_" + funDef.name;
			if(level > 0) {
				label = "f" + Long.toString(funCounter) + "___" + funPath;
				funCounter++;
			} else {
				if(globalNames.contains(funDef.name)) {
					throw new CompilerError("Global name " + funDef.name + " defined within " + funDef.toString() + " is already defined");
				}
				globalNames.add(funDef.name);
			}
			
			// increment level
			level++;
			// 8 because of the static link
			this.localOffset = 8;
			for (int p = 0; p < funDef.numPars(); p++) {
				funDef.par(p).accept(this);
				try {
					long size = this.attrs.typAttr.get(funDef.par(p)).size();
					this.attrs.accAttr.set(funDef.par(p), new OffsetAccess(this.level, this.localOffset, size));
					this.localOffset += size;
				} catch (Exception e) {
					throw new CompilerError("Error while setting access to parameter at " + funDef.par(p).toString());
				}
			}
			
			// the input call size is equal to the current offset
			long inpCallSize = localOffset;
//					+ this.attrs.typAttr.get(funDef.type).size();
			// reset the offset and the out call size to 0
			this.localOffset = 0;
			this.maxOutCallSize = 0;
			// accept the body of the function
			funDef.body.accept(this);
			// decrement the level, since the parameters and body are already visited
			level--;
			
			// delete _name from the path
			if(funPath.length() > funDef.name.length()) {
				funPath = funPath.substring(0, funPath.length()-funDef.name.length()-1);
			} else {
				funPath = "";
			}
			
			// create the frame
			Frame frame = new Frame(level, label, inpCallSize, Math.abs(localOffset), 0, 0, this.maxOutCallSize);
			// set the frame attribute
			this.attrs.frmAttr.set(funDef, frame);
			
			// bring back the old values for local offset and out call size
			this.localOffset = oldLocalOffset;
			this.maxOutCallSize = oldOutCallSize;
		} catch (Exception e) {
			throw new CompilerError("Error while preparing frame for function at " + funDef.toString());
		}
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
		try {
			// create the label
			String label = "_";
			
			// reset the offset and the out call size to 0
			this.localOffset = 0;
			this.maxOutCallSize = 0;

			// accept the body of the program node
			program.expr.accept(this);
			
			// create the frame
			Frame frame = new Frame(-1, label, 0, 0, 0, 0, this.maxOutCallSize);
//			System.out.println(this.maxOutCallSize);
			// set the frame attribute
			this.attrs.frmAttr.set(program, frame);
			
		} catch (Exception e) {
			throw new CompilerError("Error while preparing frame for program node at " + program.toString());
		}	
	}

	public void visit(PtrType ptrType) {
		ptrType.baseType.accept(this);
	}

	public void visit(RecType recType) {
		long recOffset = 0;
		for (int c = 0; c < recType.numComps(); c++) {
			recType.comp(c).accept(this);
			long size = this.attrs.typAttr.get(recType.comp(c)).size();
			this.attrs.accAttr.set(recType.comp(c), new OffsetAccess(this.level, recOffset, size));
			recOffset += size;
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
	}

	public void visit(VarDecl varDecl) {
		try {
			varDecl.type.accept(this);
			long size = this.attrs.typAttr.get(varDecl).size();
			if(level == 0) {
				if(globalNames.contains(varDecl.name)) {
					throw new CompilerError("Global name " + varDecl.name + " defined within " + varDecl.toString() + " is already defined");
				}
				globalNames.add(varDecl.name);
				this.attrs.accAttr.set(varDecl, new StaticAccess("_" + varDecl.name, size));
			} else {
				localOffset -= size;
				this.attrs.accAttr.set(varDecl, new OffsetAccess(this.level, localOffset, size));
			}
		} catch (Exception e) {
			throw new CompilerError("Error while setting access to variable at " + varDecl.toString());
		}
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
