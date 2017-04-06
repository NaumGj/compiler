package compiler.data.imc;

import compiler.data.codegen.code.ImcVisitor;

/**
 * An expression.
 * 
 * @author sliva
 */
public abstract class IMCExpr extends IMC {

	public abstract SEXPR linCode();
	
	public abstract void accept(ImcVisitor visitor);

}
