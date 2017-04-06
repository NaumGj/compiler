package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;
import compiler.data.codegen.code.ImcVisitor;

/**
 * NOP represents no operation.
 * 
 * @author sliva
 */
public class NOP extends IMCExpr {

	public NOP() {
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "NOP");
		logger.endElement();
	}
	
	@Override
	public SEXPR linCode() {
		return new SEXPR(new STMTS(new Vector<IMCStmt>()), new NOP());
	}

	@Override
	public void accept(ImcVisitor visitor) {
		visitor.visit(this);
	}
}
