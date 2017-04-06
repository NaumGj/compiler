package compiler.data.imc;

import compiler.common.logger.*;
import compiler.data.ast.code.Visitor;

/**
 * An instruction of an intermediate code.
 * 
 * @author sliva
 */
public abstract class IMC {
	
	public abstract void toXML(Logger logger);

}
