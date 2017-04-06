package compiler.common.report;

/**
 * Throws a syntax error.
 */
@SuppressWarnings("serial")
public class SemanticError extends CompilerError {

	/**
	 * Syntax error.
	 * 
	 * @param message Error message.
	 */
	public SemanticError(String message) {
		super(message);
	}

}
