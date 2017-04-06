package compiler.data.imc;

import compiler.data.codegen.code.ImcVisitor;

/**
 * A command.
 * 
 * @author sliva
 */
public abstract class IMCStmt extends IMC {
	
	public abstract STMTS linCode();
	
	public abstract void accept(ImcVisitor visitor);

}
