package compiler.common.report;

/**
 * Throws a syntax error.
 */
@SuppressWarnings("serial")
public class SyntaxError extends CompilerError {

	/**
	 * Syntax error.
	 * 
	 * @param message Error message.
	 */
	public SyntaxError(String message) {
		super(message);
	}

}
