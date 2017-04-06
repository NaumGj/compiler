package compiler.common.report;

/**
 * Throws a compiler error of unspecified kind.
 * 
 * All other kinds of compiler errors are subclasses of this class and its
 * throwables should be caught in method {@link compiler.Main#main(String[]) main}.
 * 
 * @author sliva
 */
@SuppressWarnings("serial")
public class LexError extends CompilerError {

	/**
	 * Lexical error.
	 * 
	 * @param message Error message.
	 */
	public LexError(String message) {
		super(message);
	}
	
	/**
	 * Lexical error.
	 * 
	 * @param message Error message.
	 * @param position Error position.
	 */
	public LexError(String message, Position position) {
		super(message + " at line " + position.getEndLine() + ", column: " + position.getEndColumn());
	}

}
