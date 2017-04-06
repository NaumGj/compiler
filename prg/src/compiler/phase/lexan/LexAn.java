package compiler.phase.lexan;

import java.io.*;

import compiler.*;
import compiler.common.report.*;
import compiler.phase.*;

/**
 * The lexical analyzer.
 * 
 * @author sliva
 */
public class LexAn extends Phase {

	/** The source file. */
	private BufferedReader srcFile;
	/** Source file name. */
	private String srcName;
	/** Begin and end position of the current symbol. */
	private Position symPosition;

	/**
	 * Constructs a new lexical analyzer.
	 * 
	 * Opens the source file and prepares the buffer. If logging is requested,
	 * sets up the logger.
	 * 
	 * @param task.srcFName
	 *            The name of the source file name.
	 */
	public LexAn(Task task) {
		super(task, "lexan");

		srcName = this.task.srcFName;
		symPosition = new Position(srcName, 1, 1);
		// Open the source file.
		try {
			srcFile = new BufferedReader(new FileReader(this.task.srcFName));
		} catch (FileNotFoundException ex) {
			throw new CompilerError("Source file '" + this.task.srcFName + "' not found.");
		}
	}

	/**
	 * Terminates lexical analysis. Closes the source file and, if logging has
	 * been requested, this method produces the report by closing the logger.
	 */
	@Override
	public void close() {
		// Close the source file.
		if (srcFile != null) {
			try {
				srcFile.close();
			} catch (IOException ex) {
				Report.warning("Source file '" + task.srcFName + "' cannot be closed.");
			}
		}
		super.close();
	}

	/**
	 * Returns the next lexical symbol from the source file.
	 * 
	 * @return The next lexical symbol.
	 * @throws IOException 
	 */
	public Symbol lexAn() throws IOException {
		int next = srcFile.read();
		
//		System.out.println(next);
		return readNormalMode(next);
	}
	
	/**
	 * Returns the next lexical symbol from the source file, by looking at the next character
	 * and deciding what kind of symbol the function should read and return.
	 * 
	 * @param next 
	 * 			ASCII representation of the next character in the source file.
	 * @return The next lexical symbol.
	 * @throws IOException
	 */
	private Symbol readNormalMode(int next) throws IOException {
		if(next == -1) {	// EOF
			return log(new Symbol(Symbol.Token.EOF, "", shiftBothPositions(0, 1)));
		}
		if(next == 43) {	// +
			return log(new Symbol(Symbol.Token.ADD, "", shiftBothPositions(0, 1)));
		}
		if(next == 38) {	// &
			return log(new Symbol(Symbol.Token.AND, "", shiftBothPositions(0, 1)));
		}
		if(next == 61) {	// =
			srcFile.mark(1);
			if(srcFile.read() == 61) {	// one more =
				shiftEndPosition(1);
				return log(new Symbol(Symbol.Token.EQU, "", shiftBothPositions(0, 1)));
			}
			srcFile.reset();
			return log(new Symbol(Symbol.Token.ASSIGN, "", shiftBothPositions(0, 1)));
		}
		if(next == 58) {	// :
			return log(new Symbol(Symbol.Token.COLON, "", shiftBothPositions(0, 1)));
		}
		if(next == 44) {	// ,
			return log(new Symbol(Symbol.Token.COMMA, "", shiftBothPositions(0, 1)));
		}
		if(next == 125) {	// }
			return log(new Symbol(Symbol.Token.CLOSING_BRACE, "", shiftBothPositions(0, 1)));
		}
		if(next == 93) {	// ]
			return log(new Symbol(Symbol.Token.CLOSING_BRACKET, "", shiftBothPositions(0, 1)));
		}
		if(next == 41) {	// )
			return log(new Symbol(Symbol.Token.CLOSING_PARENTHESIS, "", shiftBothPositions(0, 1)));
		}
		if(next == 46) {	// .
			return log(new Symbol(Symbol.Token.DOT, "", shiftBothPositions(0, 1)));
		}
		if(next == 47) {	// /
			return log(new Symbol(Symbol.Token.DIV, "", shiftBothPositions(0, 1)));
		}
		if(next == 62) {	// >
			srcFile.mark(1);
			if(srcFile.read() == 61) {	// =
				shiftEndPosition(1);
				return log(new Symbol(Symbol.Token.GEQ, "", shiftBothPositions(0, 1)));
			}
			srcFile.reset();
			return log(new Symbol(Symbol.Token.GTH, "", shiftBothPositions(0, 1)));
		}
		if(next == 60) {	// <
			srcFile.mark(1);
			if(srcFile.read() == 61) {	// =
				shiftEndPosition(1);
				return log(new Symbol(Symbol.Token.LEQ, "", shiftBothPositions(0, 1)));
			}
			srcFile.reset();
			return log(new Symbol(Symbol.Token.LTH, "", shiftBothPositions(0, 1)));
		}
		if(next == 64) {	// @
			return log(new Symbol(Symbol.Token.MEM, "", shiftBothPositions(0, 1)));
		}
		if(next == 37) {	// %
			return log(new Symbol(Symbol.Token.MOD, "", shiftBothPositions(0, 1)));
		}
		if(next == 42) {	// *
			return log(new Symbol(Symbol.Token.MUL, "", shiftBothPositions(0, 1)));
		}
		if(next == 33) {	// !
			srcFile.mark(1);
			if(srcFile.read() == 61) {	// =
				shiftEndPosition(1);
				return log(new Symbol(Symbol.Token.NEQ, "", shiftBothPositions(0, 1)));
			}
			srcFile.reset();
			return log(new Symbol(Symbol.Token.NOT, "", shiftBothPositions(0, 1)));
		}
		if(next == 123) {	// {
			return log(new Symbol(Symbol.Token.OPENING_BRACE, "", shiftBothPositions(0, 1)));
		}
		if(next == 91) {	// [
			return log(new Symbol(Symbol.Token.OPENING_BRACKET, "", shiftBothPositions(0, 1)));
		}
		if(next == 40) {	// (
			return log(new Symbol(Symbol.Token.OPENING_PARENTHESIS, "", shiftBothPositions(0, 1)));
		}
		if(next == 124) {	// |
			return log(new Symbol(Symbol.Token.OR, "", shiftBothPositions(0, 1)));
		}
		if(next == 45) {	// -
			return log(new Symbol(Symbol.Token.SUB, "", shiftBothPositions(0, 1)));
		}
		if(next == 94) {	// ^
			return log(new Symbol(Symbol.Token.VAL, "", shiftBothPositions(0, 1)));
		}
		if(next == 34) {	// "
			return readStringMode();
		}
		if(next == 39) {	// '
			return readCharMode();
		}
		if(next == 35) {	// #
			skipComment();
			return readNormalMode(srcFile.read());
		}
		if(next >= 48 && next <= 57) {	// number
			return readInteger(next);
		}
		if(next == 95 || (next >= 65 && next <= 90) || (next >= 97 && next <= 122)) {
			//underscore or letter -> identifier, keyword, type name, pointer constant or void constant
			return readIdentifier(next);
		}
		if(next == 32) {
			// space
			shiftBothPositions(0, 1);
			return readNormalMode(srcFile.read());
		}
		if(next == 13) {
			// CR
			shiftBothPositions(0, 1);
			return readNormalMode(srcFile.read());
		}
		if(next == 10) {
			// LF
			shiftBothPositions(1, 0);
			return readNormalMode(srcFile.read());
		}
		if(next == 9) {
			// TAB
			shiftBothPositions(0, 8);
			return readNormalMode(srcFile.read());
		}
		
		throw new LexError("Invalid symbol", symPosition);
	}
	
	/**
	 * Shifts the end position of the current symbol.
	 * 
	 * @param columnShift
	 * 			The number of columns the end position should be shifted to the right.
	 */
	private void shiftEndPosition(int columnShift) {
		Position endPos = new Position(srcName, symPosition.getEndLine(), symPosition.getEndColumn() + columnShift);
		symPosition = new Position(symPosition, endPos);
	}
	
	/**
	 * Shifts the end position of the current symbol if lineShift equals 0, otherwise the function
	 * shifts the end position to one line lower and sets the column of the current symbol to 1.
	 * At the end the function returns the position of the symbol and sets the position to the start
	 * of the next symbol.
	 * 
	 * @param lineShift
	 * 		The number of lines the end position should be shifted.
	 * @param columnShift
	 * 		The number of columns the end position should be shifted to the right.
	 * @return The starting position of the next symbol.
	 */
	private Position shiftBothPositions(int lineShift, int columnShift) {
		// we return the previous position
		Position prevPos = new Position(symPosition);
		// if we shift line, then set the column number to 1
		columnShift = lineShift > 0 ? 1 : symPosition.getEndColumn() + columnShift;
		Position endPos = new Position(srcName, symPosition.getEndLine() + lineShift, columnShift);
		symPosition = new Position(endPos, endPos);
		
		return prevPos;
	}
	
	/**
	 * Skips comments, i.e. all characters that follow a "#" sign, until it founds LF.
	 * If the function finds EOF, it throws an error.
	 * 
	 * @throws IOException
	 */
	private void skipComment() throws IOException {
		int next = srcFile.read();
		shiftBothPositions(0, 1);
		while(next != 10) {
			next = srcFile.read();
			shiftBothPositions(0, 1);
			if(next == -1) {
				throw new LexError("After the comment there must be a LF", symPosition);
			}
			if(next > 127) {
				throw new LexError("Only ASCII characters are allowed", symPosition);
			}
		}
		shiftBothPositions(1, 0);
	}
	
	/**
	 * Reads an identifier, but first checks if it is a boolean constant, pointer constant,
	 * void constant, type name or keyword. 
	 * 
	 * @param firstChar
	 * 			First, already read character of the identifier.
	 * @return The next lexical symbol.
	 * @throws IOException
	 */
	private Symbol readIdentifier(int firstChar) throws IOException {
		String identifier = Character.toString((char)firstChar);
		srcFile.mark(1);
		int next = srcFile.read();
		while(next == 95 || (next >= 65 && next <= 90) || (next >= 97 && next <= 122) || (next >= 48 && next <= 57)) {
			identifier += Character.toString((char)next);
			srcFile.mark(1);
			next = srcFile.read();
		}
		srcFile.reset();
		
		// Shift the end position of the identifier
		shiftEndPosition(identifier.length() - 1);
		
		// Check if identifier is actually a boolean constant
		if("true".equals(identifier) || "false".equals(identifier)) {
			return log(new Symbol(Symbol.Token.CONST_BOOLEAN, identifier, shiftBothPositions(0, 1)));
		}
		
		// Check if identifier is actually a pointer constant -> null
		if("null".equals(identifier)) {
			return log(new Symbol(Symbol.Token.CONST_NULL, identifier, shiftBothPositions(0, 1)));
		}
				
		// Check if identifier is actually a void constant -> none
		if("none".equals(identifier)) {
			return log(new Symbol(Symbol.Token.CONST_NONE, identifier, shiftBothPositions(0, 1)));
		}
		
		// Check if identifier is actually a type name
		if("integer".equals(identifier)) {
			return log(new Symbol(Symbol.Token.INTEGER, "", shiftBothPositions(0, 1)));
		}
		if("boolean".equals(identifier)) {
			return log(new Symbol(Symbol.Token.BOOLEAN, "", shiftBothPositions(0, 1)));
		}
		if("char".equals(identifier)) {
			return log(new Symbol(Symbol.Token.CHAR, "", shiftBothPositions(0, 1)));
		}
		if("string".equals(identifier)) {
			return log(new Symbol(Symbol.Token.STRING, "", shiftBothPositions(0, 1)));
		}
		if("void".equals(identifier)) {
			return log(new Symbol(Symbol.Token.VOID, "", shiftBothPositions(0, 1)));
		}
		
		// Check if identifier is actually a keyword
		if("arr".equals(identifier)) {
			return log(new Symbol(Symbol.Token.ARR, "", shiftBothPositions(0, 1)));
		}
		if("else".equals(identifier)) {
			return log(new Symbol(Symbol.Token.ELSE, "", shiftBothPositions(0, 1)));
		}
		if("end".equals(identifier)) {
			return log(new Symbol(Symbol.Token.END, "", shiftBothPositions(0, 1)));
		}
		if("for".equals(identifier)) {
			return log(new Symbol(Symbol.Token.FOR, "", shiftBothPositions(0, 1)));
		}
		if("fun".equals(identifier)) {
			return log(new Symbol(Symbol.Token.FUN, "", shiftBothPositions(0, 1)));
		}
		if("if".equals(identifier)) {
			return log(new Symbol(Symbol.Token.IF, "", shiftBothPositions(0, 1)));
		}
		if("then".equals(identifier)) {
			return log(new Symbol(Symbol.Token.THEN, "", shiftBothPositions(0, 1)));
		}
		if("ptr".equals(identifier)) {
			return log(new Symbol(Symbol.Token.PTR, "", shiftBothPositions(0, 1)));
		}
		if("rec".equals(identifier)) {
			return log(new Symbol(Symbol.Token.REC, "", shiftBothPositions(0, 1)));
		}
		if("typ".equals(identifier)) {
			return log(new Symbol(Symbol.Token.TYP, "", shiftBothPositions(0, 1)));
		}
		if("var".equals(identifier)) {
			return log(new Symbol(Symbol.Token.VAR, "", shiftBothPositions(0, 1)));
		}
		if("where".equals(identifier)) {
			return log(new Symbol(Symbol.Token.WHERE, "", shiftBothPositions(0, 1)));
		}
		if("while".equals(identifier)) {
			return log(new Symbol(Symbol.Token.WHILE, "", shiftBothPositions(0, 1)));
		}
		
		// Otherwise it is an identifier
		return log(new Symbol(Symbol.Token.IDENTIFIER, identifier, shiftBothPositions(0, 1)));
	}
	
	/**
	 * Reads an integer constant.
	 * 
	 * @param firstDigit
	 * 			First, already read digit.
	 * @return The integer constant as the next lexical symbol.
	 * @throws IOException
	 */
	private Symbol readInteger(int firstDigit) throws IOException {
		String lexeme = Character.toString((char)firstDigit);
		srcFile.mark(1);
		int next = srcFile.read();
		while(next >= 48 && next <= 57) {
//			shiftEndPosition(1);
			lexeme += Character.toString((char)next);
			srcFile.mark(1);
			next = srcFile.read();
		}
		srcFile.reset();
//		try {
//			Long.parseLong(lexeme);
//		} catch (NumberFormatException e) {
//			throw new LexError("The integer is not in the valid interval", symPosition);
//		}
		
		// Shift the end position of the integer constant
		shiftEndPosition(lexeme.length() - 1);
		
		return log(new Symbol(Symbol.Token.CONST_INTEGER, lexeme, shiftBothPositions(0, 1)));
	}
	
	/**
	 * Reads a character constant.
	 * 
	 * @return The character constant as the next lexical symbol.
	 * @throws IOException
	 */
	private Symbol readCharMode() throws IOException {
		int next = srcFile.read();
		shiftEndPosition(1);
		
		if (next == 92) {	// \
			next = srcFile.read();
			if(isClosedChar()) {
				// The character is constant (together with single quotes and the escape character) 
				// consists of 4 characters, because the position pointer is already pointing at 
				// the beginning of the character constant, we shift the end position for 2 places
				shiftEndPosition(2);
				if(next == 92) {	// \
					return log(new Symbol(Symbol.Token.CONST_CHAR, "'\\\\'", shiftBothPositions(0, 1)));
				}
				if(next == 39) {	// '
					return log(new Symbol(Symbol.Token.CONST_CHAR, "'\\''", shiftBothPositions(0, 1)));
				}
				if(next == 34) {	// "
					return log(new Symbol(Symbol.Token.CONST_CHAR, "'\\\"'", shiftBothPositions(0, 1)));
				}
				if(next == 116) {	// t
					return log(new Symbol(Symbol.Token.CONST_CHAR, "'\\t'", shiftBothPositions(0, 1)));
				}
				if(next == 110) {	// n
					return log(new Symbol(Symbol.Token.CONST_CHAR, "'\\n'", shiftBothPositions(0, 1)));
				}
				
				shiftEndPosition(-2);
				throw new LexError("Invalid escape sequence", symPosition);
			}

			throw new LexError("Single quote is not closed");
		} else if (next >= 32 && next <= 126 && next != 92 && next != 39 && next != 34) {	// [32, 126] ASCII representation and not a backslash, single or double quote
			if(isClosedChar()) {
				// The character is constant (together with single quotes) consists of 3 characters, 
				// because the position pointer is already pointing at the beginning of the character 
				// constant, we shift the end position for 1 place
				shiftEndPosition(1);
				return log(new Symbol(Symbol.Token.CONST_CHAR, "'" + Character.toString((char)next) + "'", shiftBothPositions(0, 1)));
			} 
			
			throw new LexError("Single quote is not closed", symPosition);
		}
		
		throw new LexError("Invalid character in character constant", symPosition);
	}
	
	/**
	 * Checks if the character constant is properly closed with a single quote (')
	 * 
	 * @return True if the character constant is properly closed with a single quote, otherwise false.
	 * @throws IOException
	 */
	private boolean isClosedChar() throws IOException {
		srcFile.mark(1);
		if(srcFile.read() == 39) {	// '
			return true;
		}
		srcFile.reset();
		return false;
	}
	
	/**
	 * Reads a string constant.
	 * 
	 * @return The string constant as the next lexical symbol.
	 * @throws IOException
	 */
	private Symbol readStringMode() throws IOException {
		String lexeme = "\"";
		shiftEndPosition(1);	// for the quotes
		int next = srcFile.read();
		// The length of the string constant together with double quotes, needed for position shifting
//		int strLen = 1;
		
		while(next != 34) {	// "
			shiftEndPosition(1);
			if (next == 92) {	// \
				next = srcFile.read();
				shiftEndPosition(1);
				if(next == 92) {			// \
					lexeme += "\\\\";
				} else if(next == 39) {		// '
					lexeme += "\\'";
				} else if(next == 34) {		// "
					lexeme += "\\\"";
				} else if(next == 116) {	// t
					lexeme += "\\t";
				} else if(next == 110) {	// n
					lexeme += "\\n";
				} else {
					throw new LexError("Invalid escape sequence", symPosition);
				}
//				strLen += 2;	// increase constant's length by 2 (escape character and the actual character)
			} else if (next >= 32 && next <= 126 && next != 92 && next != 39 && next != 34) {	// [32, 126] ASCII representation
				lexeme += Character.toString((char)next);
//				strLen++;	// increment constant's length
			} else {
				// not a valid char
				throw new LexError("Invalid character in string constant", symPosition);
			}
			next = srcFile.read();
		}
		
		// shift the end position for strLen places
//		shiftEndPosition(strLen);
//		shiftEndPosition(lexeme.length() - 1);
		lexeme += "\"";
		
		return log(new Symbol(Symbol.Token.CONST_STRING, lexeme, shiftBothPositions(0, 1)));
	}

	/**
	 * Prints out the symbol and returns it.
	 * 
	 * This method should be called by the lexical analyzer before it returns a
	 * symbol so that the symbol can be logged (even if logging of lexical
	 * analysis has not been requested).
	 * 
	 * @param symbol
	 *            The symbol to be printed out.
	 * @return The symbol received as an argument.
	 */
	private Symbol log(Symbol symbol) {
		symbol.log(logger);
		return symbol;
	}

}
