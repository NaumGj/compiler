package compiler.phase.synan;

import java.io.IOException;
import java.util.*;

import org.w3c.dom.*;

import compiler.*;
import compiler.common.logger.*;
import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.phase.*;
import compiler.phase.lexan.*;

/**
 * The syntax analyzer.
 * 
 * @author sliva
 */
public class SynAn extends Phase {

	/** The lexical analyzer. */
	private final LexAn lexAn;

	/**
	 * Constructs a new syntax analyzer.
	 * 
	 * @param lexAn
	 *            The lexical analyzer.
	 */
	public SynAn(Task task) {
		super(task, "synan");
		this.lexAn = new LexAn(task);
		if(task.phase.equals("synan")){
			this.logger.setTransformer(//
					new Transformer() {
						// This transformer produces the
						// left-most derivation.
	
						private String nodeName(Node node) {
							Element element = (Element) node;
							String nodeName = element.getTagName();
							if (nodeName.equals("nont")) {
								return element.getAttribute("name");
							}
							if (nodeName.equals("symbol")) {
								return element.getAttribute("name");
							}
							return null;
						}
	
						private void leftMostDer(Node node) {
							if (((Element) node).getTagName().equals("nont")) {
								String nodeName = nodeName(node);
								NodeList children = node.getChildNodes();
								StringBuffer production = new StringBuffer();
								production.append(nodeName + " -->");
								for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
									Node child = children.item(childIdx);
									String childName = nodeName(child);
									production.append(" " + childName);
								}
								Report.info(production.toString());
								for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
									Node child = children.item(childIdx);
									leftMostDer(child);
								}
							}
						}
	
						public Document transform(Document doc) {
							leftMostDer(doc.getDocumentElement().getFirstChild());
							return doc;
						}
					});
		}
	}

	/**
	 * Terminates syntax analysis. Lexical analyzer is not closed and, if
	 * logging has been requested, this method produces the report by closing
	 * the logger.
	 */
	@Override
	public void close() {
		lexAn.close();
		super.close();
	}

	/** The parser's lookahead buffer. */
	private Symbol laSymbol;

	/**
	 * Reads the next lexical symbol from the source file and stores it in the
	 * lookahead buffer (before that it logs the previous lexical symbol, if
	 * requested); returns the previous symbol.
	 * 
	 * @return The previous symbol (the one that has just been replaced by the
	 *         new symbol).
	 */
	private Symbol nextSymbol() throws IOException {
		Symbol symbol = laSymbol;
		symbol.log(logger);
		laSymbol = lexAn.lexAn();
		return symbol;
	}

	/**
	 * Logs the error token inserted when a missing lexical symbol has been
	 * reported.
	 * 
	 * @return The error token (the symbol in the lookahead buffer is to be used
	 *         later).
	 */
	private Symbol nextSymbolIsError() {
		Symbol error = new Symbol(Symbol.Token.ERROR, "", new Position("", 0, 0));
		error.log(logger);
		return error;
	}

	/**
	 * Starts logging an internal node of the derivation tree.
	 * 
	 * @param nontName
	 *            The name of a nonterminal the internal node represents.
	 */
	private void begLog(String nontName) {
//		System.out.println(nontName);
		if (logger == null)
			return;
		logger.begElement("nont");
		logger.addAttribute("name", nontName);
	}

	/**
	 * Ends logging an internal node of the derivation tree.
	 */
	private void endLog() {
		if (logger == null)
			return;
		logger.endElement();
	}

	/**
	 * The parser.
	 * 
	 * This method performs the syntax analysis of the source file.
	 * @throws IOException 
	 */
	public Program synAn() throws IOException {
		laSymbol = lexAn.lexAn();
		Program prg = parseProgram();
		if (laSymbol.token != Symbol.Token.EOF)
			Report.warning(laSymbol, "Unexpected symbol(s) at the end of file.");
		return prg;
	}

	// All these methods are a part of a recursive descent implementation of an
	// LL(1) parser.

	private Program parseProgram() throws IOException {
		begLog("Program");
		Program prg = null;
		switch (laSymbol.token) {
		case ADD: 
		case SUB:
		case NOT:
		case MEM:
		case OPENING_BRACKET:
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE:
		{
			Expr e = parseExpression();
			prg = new Program(new Position(e), e);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseProgram()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return prg;
	}
	
	private Expr parseExpression() throws IOException {
		begLog("Expression");
		Expr e = null;
		switch (laSymbol.token) {
		case ADD: 
		case SUB:
		case NOT:
		case MEM:
		case OPENING_BRACKET:
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE:
		{
			e = parseAssignmentExpression();
			e = parseExpressionPrime(e);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseExpression()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseExpressionPrime(Expr expr) throws IOException {
		begLog("ExpressionPrime");
		Expr e = expr;
		switch (laSymbol.token) {
		case WHERE: {
			Symbol symWhere = nextSymbol();
			Symbol symEnd;
			LinkedList<Decl> decls = parseDeclarations();
			if (laSymbol.token == Symbol.Token.END) {
				symEnd = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseExpressionPrime()/: missing symbol END, instead saw unexpected symbol " + laSymbol.token + " at " + laSymbol.toString());
			}
			e = new WhereExpr(new Position(expr, symEnd), expr, decls);
			e = parseExpressionPrime(e);
			break;
		}
		case END:
		case COMMA:
		case CLOSING_BRACKET: 
		case CLOSING_PARENTHESIS: 
		case THEN: 
		case ELSE: 
		case COLON: 
		case TYP: 
		case FUN: 
		case VAR: 
		case EOF: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseExpressionPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private LinkedList<Expr> parseExpressions() throws IOException {
		begLog("Expressions");
		LinkedList<Expr> exprs = new LinkedList<Expr>();
//		Exprs e = null;
		switch (laSymbol.token) {
		case ADD: 
		case SUB:
		case NOT:
		case MEM:
		case OPENING_BRACKET:
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE:
		{
			exprs.add(parseExpression());
			exprs = parseExpressionsPrime(exprs);
//			e = new Exprs(new Position("smeni", 0, 0), exprs);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseExpressions()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return exprs;
	}
	
	private LinkedList<Expr> parseExpressionsPrime(LinkedList<Expr> exprs) throws IOException {
		begLog("ExpressionsPrime");
		switch (laSymbol.token) {
		case COMMA: {
			Symbol symComma = nextSymbol();
			exprs.add(parseExpression());
			exprs = parseExpressionsPrime(exprs);
			break;
		}
		case CLOSING_PARENTHESIS: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseExpressionsPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return exprs;
	}
	
	private Expr parseAssignmentExpression() throws IOException {
		begLog("AssignmentExpression");
		Expr e = null;
		switch (laSymbol.token) {
		case ADD: 
		case SUB:
		case NOT:
		case MEM:
		case OPENING_BRACKET:
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE:
		{
			e = parseDisjunctiveExpression();
			e = parseAssignmentExpressionPrime(e);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseAssignmentExpression()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseAssignmentExpressionPrime(Expr expr) throws IOException {
		begLog("AssignmentExpressionPrime");
		Expr e = expr;
		switch (laSymbol.token) {
		case ASSIGN: {
			Symbol symAssign = nextSymbol();
			Expr disExpr = parseDisjunctiveExpression();
			e = new BinExpr(new Position(expr, disExpr), BinExpr.Oper.ASSIGN, expr, disExpr);
			break;
		}
		case WHERE:
		case END:
		case COMMA:
		case CLOSING_BRACKET: 
		case CLOSING_PARENTHESIS: 
		case THEN: 
		case ELSE: 
		case COLON: 
		case TYP: 
		case FUN: 
		case VAR:
		case EOF: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseAssignmentExpressionPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseDisjunctiveExpression() throws IOException {
		begLog("DisjunctiveExpression");
		Expr e = null;
		switch (laSymbol.token) {
		case ADD: 
		case SUB:
		case NOT:
		case MEM:
		case OPENING_BRACKET:
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE:
		{
			e = parseConjunctiveExpression();
			e = parseDisjunctiveExpressionPrime(e);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseDisjunctiveExpression()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseDisjunctiveExpressionPrime(Expr expr) throws IOException {
		begLog("DisjunctiveExpressionPrime");
		Expr e = expr;
		switch (laSymbol.token) {
		case OR: {
			Symbol symOr = nextSymbol();
			Expr conExpr = parseConjunctiveExpression();
			e = new BinExpr(new Position(expr, conExpr), BinExpr.Oper.OR, expr, conExpr);
			e = parseDisjunctiveExpressionPrime(e);
			break;
		}
		case WHERE:
		case END:
		case COMMA:
		case ASSIGN:
		case CLOSING_BRACKET: 
		case CLOSING_PARENTHESIS:
		case THEN: 
		case ELSE: 
		case COLON: 
		case TYP: 
		case FUN: 
		case VAR:
		case EOF: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseDisjunctiveExpressionPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseConjunctiveExpression() throws IOException {
		begLog("ConjunctiveExpression");
		Expr e = null;
		switch (laSymbol.token) {
		case ADD: 
		case SUB:
		case NOT:
		case MEM:
		case OPENING_BRACKET:
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE:
		{
			e = parseRelationalExpression();
			e = parseConjunctiveExpressionPrime(e);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseConjunctiveExpression()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseConjunctiveExpressionPrime(Expr expr) throws IOException {
		begLog("ConjunctiveExpressionPrime");
		Expr e = expr;
		switch (laSymbol.token) {
		case AND: {
			Symbol symAnd = nextSymbol();
			Expr relExpr = parseRelationalExpression();
			e = new BinExpr(new Position(expr, relExpr), BinExpr.Oper.AND, expr, relExpr);
			e = parseConjunctiveExpressionPrime(e);
			break;
		}
		case WHERE:
		case END:
		case COMMA:
		case ASSIGN:
		case OR:
		case CLOSING_BRACKET: 
		case CLOSING_PARENTHESIS: 
		case THEN: 
		case ELSE: 
		case COLON: 
		case TYP: 
		case FUN: 
		case VAR:
		case EOF: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseConjunctiveExpressionPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseRelationalExpression() throws IOException {
		begLog("RelationalExpression");
		Expr e = null;
		switch (laSymbol.token) {
		case ADD: 
		case SUB:
		case NOT:
		case MEM:
		case OPENING_BRACKET:
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE:
		{
			e = parseAdditiveExpression();
			e = parseRelationalExpressionPrime(e);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseRelationalExpression()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseRelationalExpressionPrime(Expr expr) throws IOException {
		begLog("RelationalExpressionPrime");
		Expr e = expr;
		switch (laSymbol.token) {
		case EQU: {
			Symbol symEqu = nextSymbol();
			Expr addExpr = parseAdditiveExpression();
			e = new BinExpr(new Position(expr, addExpr), BinExpr.Oper.EQU, expr, addExpr);
			break;
		}
		case NEQ: {
			Symbol symNeq = nextSymbol();
			Expr addExpr = parseAdditiveExpression();
			e = new BinExpr(new Position(expr, addExpr), BinExpr.Oper.NEQ, expr, addExpr);
			break;
		}
		case LTH: {
			Symbol symLth = nextSymbol();
			Expr addExpr = parseAdditiveExpression();
			e = new BinExpr(new Position(expr, addExpr), BinExpr.Oper.LTH, expr, addExpr);
			break;
		}
		case GTH: {
			Symbol symGth = nextSymbol();
			Expr addExpr = parseAdditiveExpression();
			e = new BinExpr(new Position(expr, addExpr), BinExpr.Oper.GTH, expr, addExpr);
			break;
		}
		case LEQ: {
			Symbol symLeq = nextSymbol();
			Expr addExpr = parseAdditiveExpression();
			e = new BinExpr(new Position(expr, addExpr), BinExpr.Oper.LEQ, expr, addExpr);
			break;
		}
		case GEQ: {
			Symbol symGeq = nextSymbol();
			Expr addExpr = parseAdditiveExpression();
			e = new BinExpr(new Position(expr, addExpr), BinExpr.Oper.GEQ, expr, addExpr);
			break;
		}
		case WHERE:
		case END:
		case COMMA:
		case ASSIGN:
		case OR:
		case AND:
		case CLOSING_BRACKET: 
		case CLOSING_PARENTHESIS: 
		case THEN: 
		case ELSE: 
		case COLON: 
		case TYP: 
		case FUN: 
		case VAR:
		case EOF: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseRelationalExpressionPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseAdditiveExpression() throws IOException {
		begLog("AdditiveExpression");
		Expr e = null;
		switch (laSymbol.token) {
		case ADD: 
		case SUB:
		case NOT:
		case MEM:
		case OPENING_BRACKET:
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE:
		{
			e = parseMultiplicativeExpression();
			e = parseAdditiveExpressionPrime(e);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseAdditiveExpression()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseAdditiveExpressionPrime(Expr expr) throws IOException {
		begLog("AdditiveExpressionPrime");
		Expr e = expr;
		switch (laSymbol.token) {
		case ADD: {
			Symbol symAdd = nextSymbol();
			Expr mulExpr = parseMultiplicativeExpression();
			e = new BinExpr(new Position(expr, mulExpr), BinExpr.Oper.ADD, expr, mulExpr);
			e = parseAdditiveExpressionPrime(e);
			break;
		}
		case SUB: {
			Symbol symSub = nextSymbol();
			Expr mulExpr = parseMultiplicativeExpression();
			e = new BinExpr(new Position(expr, mulExpr), BinExpr.Oper.SUB, expr, mulExpr);
			e = parseAdditiveExpressionPrime(e);
			break;
		}
		case WHERE:
		case END:
		case COMMA:
		case ASSIGN:
		case OR:
		case AND:
		case EQU: 
		case NEQ:
		case LTH:
		case GTH:
		case LEQ:
		case GEQ:
		case CLOSING_BRACKET: 
		case CLOSING_PARENTHESIS: 
		case THEN: 
		case ELSE: 
		case COLON: 
		case TYP: 
		case FUN: 
		case VAR:
		case EOF: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseAdditiveExpressionPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseMultiplicativeExpression() throws IOException {
		begLog("MultiplicativeExpression");
		Expr e = null;
		switch (laSymbol.token) {
		case ADD: 
		case SUB:
		case NOT:
		case MEM:
		case OPENING_BRACKET:
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE:
		{
			e = parsePrefixExpression();
			e = parseMultiplicativeExpressionPrime(e);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseMultiplicativeExpression()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseMultiplicativeExpressionPrime(Expr expr) throws IOException {
		begLog("MultiplicativeExpressionPrime");
		Expr e = expr;
		switch (laSymbol.token) {
		case MUL: {
			Symbol symMul = nextSymbol();
			Expr prefExpr = parsePrefixExpression();
			e = new BinExpr(new Position(expr, prefExpr), BinExpr.Oper.MUL, expr, prefExpr);
			e = parseMultiplicativeExpressionPrime(e);
			break;
		}
		case DIV: {
			Symbol symDiv = nextSymbol();
			Expr prefExpr = parsePrefixExpression();
			e = new BinExpr(new Position(expr, prefExpr), BinExpr.Oper.DIV, expr, prefExpr);
			e = parseMultiplicativeExpressionPrime(e);
			break;
		}
		case MOD: {
			Symbol symMod = nextSymbol();
			Expr prefExpr = parsePrefixExpression();
			e = new BinExpr(new Position(expr, prefExpr), BinExpr.Oper.MOD, expr, prefExpr);
			e = parseMultiplicativeExpressionPrime(e);
			break;
		}
		case WHERE:
		case END:
		case COMMA:
		case ASSIGN:
		case OR:
		case AND:
		case EQU: 
		case NEQ:
		case LTH:
		case GTH:
		case LEQ:
		case GEQ:
		case ADD:
		case SUB:
		case CLOSING_BRACKET: 
		case CLOSING_PARENTHESIS: 
		case THEN: 
		case ELSE: 
		case COLON: 
		case TYP: 
		case FUN: 
		case VAR:
		case EOF: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseMultiplicativeExpressionPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parsePrefixExpression() throws IOException {
		begLog("PrefixExpression");
		Expr e = null;
		switch (laSymbol.token) {
		case ADD: {
			Symbol symAdd = nextSymbol();
			Expr prefExpr = parsePrefixExpression();
			e = new UnExpr(new Position(symAdd, prefExpr), UnExpr.Oper.ADD, prefExpr);
			break;
		}
		case SUB: {
			Symbol symSub = nextSymbol();
			Expr prefExpr = parsePrefixExpression();
			e = new UnExpr(new Position(symSub, prefExpr), UnExpr.Oper.SUB, prefExpr);
			break;
		}
		case NOT: {
			Symbol symNot = nextSymbol();
			Expr prefExpr = parsePrefixExpression();
			e = new UnExpr(new Position(symNot, prefExpr), UnExpr.Oper.NOT, prefExpr);
			break;
		}
		case MEM: {
			Symbol symMem = nextSymbol();
			Expr prefExpr = parsePrefixExpression();
			e = new UnExpr(new Position(symMem, prefExpr), UnExpr.Oper.MEM, prefExpr);
			break;
		}
		case OPENING_BRACKET: {
			Symbol symOpeningBracket = nextSymbol();
			Symbol symClosingBracket;
			Type type = parseType();
			if (laSymbol.token == Symbol.Token.CLOSING_BRACKET) {
				symClosingBracket = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parsePrefixExpression()/: missing closing bracket, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Expr prefExpr = parsePrefixExpression();
			e = new CastExpr(new Position(symOpeningBracket, prefExpr), type, prefExpr);
			break;
		}
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE:
		{
			e = parsePostfixExpression();
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parsePrefixExpression()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parsePostfixExpression() throws IOException {
		begLog("PostfixExpression");
		Expr e = null;
		switch (laSymbol.token) {
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE:
		{
			e = parseAtomicExpression();
			e = parsePostfixExpressionPrime(e);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parsePostfixExpression()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parsePostfixExpressionPrime(Expr expr) throws IOException {
		begLog("PostfixExpressionPrime");
		Expr e = expr;
		switch (laSymbol.token) {
		case OPENING_BRACKET: {
			Symbol symOpeningBracket = nextSymbol();
			Symbol symClosingBracket;
			Expr sndExpr = parseExpression();
			if (laSymbol.token == Symbol.Token.CLOSING_BRACKET) {
				symClosingBracket = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parsePostfixExpressionPrime()/: missing closing bracket, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			e = new BinExpr(new Position(expr, symClosingBracket), BinExpr.Oper.ARR, expr, sndExpr);
			e = parsePostfixExpressionPrime(e);
			break;
		}
		case DOT: {
			Symbol symDot = nextSymbol();
			Symbol symId;
			if (laSymbol.token == Symbol.Token.IDENTIFIER) {
				symId = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parsePostfixExpressionPrime()/: missing identifier, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			e = new BinExpr(new Position(expr, symId), BinExpr.Oper.REC, expr, new CompName(new Position(symId), symId.lexeme));
			e = parsePostfixExpressionPrime(e);
			break;
		}
		case VAL: {
			Symbol symVal = nextSymbol();
			e = new UnExpr(new Position(expr, symVal), UnExpr.Oper.VAL, expr);
			e = parsePostfixExpressionPrime(e);
			break;
		}
		case WHERE:
		case END:
		case COMMA:
		case ASSIGN:
		case OR:
		case AND:
		case EQU: 
		case NEQ:
		case LTH:
		case GTH:
		case LEQ:
		case GEQ:
		case ADD:
		case SUB:
		case MUL:
		case DIV: 
		case MOD:
		case CLOSING_BRACKET: 
		case CLOSING_PARENTHESIS: 
		case THEN: 
		case ELSE: 
		case COLON: 
		case TYP: 
		case FUN: 
		case VAR:
		case EOF: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parsePostfixExpressionPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseAtomicExpression() throws IOException {
		begLog("AtomicExpression");
		Expr e = null;
		switch (laSymbol.token) {
		case IDENTIFIER: {
			Symbol symId = nextSymbol();
			e = parseArgumentsOpt(symId);
			if(e == null) {
				e = new VarName(new Position(symId), symId.lexeme);
			}
			break;
		}
		case CONST_INTEGER: {
			Symbol constInt = nextSymbol();
			e = new AtomExpr(new Position(constInt), AtomExpr.AtomTypes.INTEGER, constInt.lexeme);
			break;
		}
		case CONST_BOOLEAN: {
			Symbol constBool = nextSymbol();
			e = new AtomExpr(new Position(constBool), AtomExpr.AtomTypes.BOOLEAN, constBool.lexeme);
			break;
		}
		case CONST_CHAR: {
			Symbol constChar = nextSymbol();
			e = new AtomExpr(new Position(constChar), AtomExpr.AtomTypes.CHAR, constChar.lexeme);
			break;
		}
		case CONST_STRING: {
			Symbol constStr = nextSymbol();
			e = new AtomExpr(new Position(constStr), AtomExpr.AtomTypes.STRING, constStr.lexeme);
			break;
		}
		case CONST_NULL: {
			Symbol constNull = nextSymbol();
			e = new AtomExpr(new Position(constNull), AtomExpr.AtomTypes.PTR, constNull.lexeme);
			break;
		}
		case CONST_NONE: {
			Symbol constNone = nextSymbol();
			e = new AtomExpr(new Position(constNone), AtomExpr.AtomTypes.VOID, constNone.lexeme);
			break;
		}
		case OPENING_PARENTHESIS: {
			Symbol symOpeningPar = nextSymbol();
			Symbol symClosingPar;
			LinkedList<Expr> exprs = parseExpressions();
			if (laSymbol.token == Symbol.Token.CLOSING_PARENTHESIS) {
				symClosingPar = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseAtomicExpression()/: missing closing parenthesis, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			if (exprs.size() == 1) {
				e = exprs.get(0);
			} else if (exprs.size() > 1){
				e = new Exprs(new Position(symOpeningPar, symClosingPar), exprs);
			}
			break;
		}
		case IF: {
			Symbol symIf = nextSymbol();
			Symbol symThen, symElse, symEnd;
			Expr cond = parseExpression();
			if (laSymbol.token == Symbol.Token.THEN) {
				symThen = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseAtomicExpression()/: missing symbol THEN, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Expr thenExpr = parseExpression();
			if (laSymbol.token == Symbol.Token.ELSE) {
				symElse = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseAtomicExpression()/: missing symbol ELSE, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Expr elseExpr = parseExpression();
			if (laSymbol.token == Symbol.Token.END) {
				symEnd = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseAtomicExpression()/: missing symbol END, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			e = new IfExpr(new Position(symIf, symEnd), cond, thenExpr, elseExpr);
			break;
		}
		case FOR: {
			Symbol symFor = nextSymbol();
			Symbol symId, symAssign, symComma, symColon, symEnd;
			if (laSymbol.token == Symbol.Token.IDENTIFIER) {
				symId = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseAtomicExpression()/: missing identifier, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			VarName var = new VarName(new Position(symId), symId.lexeme);
			if (laSymbol.token == Symbol.Token.ASSIGN) {
				symAssign = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseAtomicExpression()/: missing symbol ASSIGN, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Expr loBound = parseExpression();
			if (laSymbol.token == Symbol.Token.COMMA) {
				symComma = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseAtomicExpression()/: missing symbol ',', instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Expr hiBound = parseExpression();
			if (laSymbol.token == Symbol.Token.COLON) {
				symComma = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseAtomicExpression()/: missing symbol ':', instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Expr body = parseExpression();
			if (laSymbol.token == Symbol.Token.END) {
				symEnd = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseAtomicExpression()/: missing symbol END, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			e = new ForExpr(new Position(symFor, symEnd), var, loBound, hiBound, body);
			break;
		}
		case WHILE:
		{
			Symbol symWhile = nextSymbol();
			Symbol symColon, symEnd;
			Expr cond = parseExpression();
			if (laSymbol.token == Symbol.Token.COLON) {
				symColon = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseAtomicExpression()/: missing symbol ':', instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Expr body = parseExpression();
			if (laSymbol.token == Symbol.Token.END) {
				symEnd = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseAtomicExpression()/: missing symbol END, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			e = new WhileExpr(new Position(symWhile, symEnd), cond, body);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseAtomicExpression()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseArgumentsOpt(Symbol sym) throws IOException {
		begLog("ArgumentsOpt");
		Expr e = null;
		switch (laSymbol.token) {
		case OPENING_PARENTHESIS: {
			Symbol symOpeningPar = nextSymbol();
			e = parseArgumentsOptPrime(sym);
			break;
		}
		case WHERE:
		case END:
		case COMMA:
		case ASSIGN:
		case OR:
		case AND:
		case EQU: 
		case NEQ:
		case LTH:
		case GTH:
		case LEQ:
		case GEQ:
		case ADD:
		case SUB:
		case MUL:
		case DIV:
		case MOD:
		case OPENING_BRACKET:
		case CLOSING_BRACKET:
		case DOT:
		case VAL:
		case CLOSING_PARENTHESIS: 
		case THEN: 
		case ELSE: 
		case COLON: 
		case TYP: 
		case FUN: 
		case VAR:
		case EOF: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseArgumentsOpt()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private Expr parseArgumentsOptPrime(Symbol sym) throws IOException {
		begLog("ArgumentsOptPrime");
		Expr e = null;
		switch (laSymbol.token) {
		case CLOSING_PARENTHESIS: {
			Symbol symClosingPar = nextSymbol();
			e = new FunCall(new Position(sym, symClosingPar), sym.lexeme, new LinkedList<Expr>());
			break;
		}
		case ADD:
		case SUB:
		case NOT:
		case MEM:
		case OPENING_BRACKET:
		case IDENTIFIER:
		case CONST_INTEGER:
		case CONST_BOOLEAN:
		case CONST_CHAR:
		case CONST_STRING:
		case CONST_NULL:
		case CONST_NONE:
		case OPENING_PARENTHESIS:
		case IF:
		case FOR:
		case WHILE: {
			Symbol symClosingPar;
			LinkedList<Expr> exprs = parseExpressions();
			if (laSymbol.token == Symbol.Token.CLOSING_PARENTHESIS) {
				symClosingPar = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseArgumentsOptPrime()/: missing closing parenthesis, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			e = new FunCall(new Position(sym, symClosingPar), sym.lexeme, exprs);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseArgumentsOptPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private LinkedList<Decl> parseDeclarations() throws IOException {
		begLog("Declarations");
		LinkedList<Decl> decls = new LinkedList<Decl>();
		switch (laSymbol.token) {
		case TYP:
		case FUN:
		case VAR: {
			decls.add(parseDeclaration());
			decls = parseDeclarationsPrime(decls);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseDeclarations()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return decls;
	}
	
	private LinkedList<Decl> parseDeclarationsPrime(LinkedList<Decl> decls) throws IOException {
		begLog("DeclarationsPrime");
		switch (laSymbol.token) {
		case TYP:
		case FUN:
		case VAR: {
			decls.add(parseDeclaration());
			decls = parseDeclarationsPrime(decls);
			break;
		}
		case END:
			break;
		default:
			throw new SyntaxError("Syntax error /parseDeclarationsPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return decls;
	}
	
	private Decl parseDeclaration() throws IOException {
		begLog("Declaration");
		Decl d = null;
		switch (laSymbol.token) {
		case TYP: {
			d = parseTypeDeclaration();
			break;
		}
		case FUN: {
			d = parseFunctionDeclaration();
			break;
		}
		case VAR: {
			d = parseVariableDeclaration();
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseDeclaration()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return d;
	}
	
	private TypeDecl parseTypeDeclaration() throws IOException {
		begLog("TypeDeclaration");
		TypeDecl d = null;
		switch (laSymbol.token) {
		case TYP: {
			Symbol symTyp = nextSymbol();
			Symbol symId, symColon;
			if (laSymbol.token == Symbol.Token.IDENTIFIER) {
				symId = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseTypeDeclaration()/: missing identifier, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			if (laSymbol.token == Symbol.Token.COLON) {
				symColon = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseTypeDeclaration()/: missing symbol ':', instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Type type = parseType();
			d = new TypeDecl(new Position(symTyp, type), symId.lexeme, type);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseTypeDeclaration()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return d;
	}
	
	private FunDecl parseFunctionDeclaration() throws IOException {
		begLog("FunctionDeclaration");
		FunDecl d = null;
		switch (laSymbol.token) {
		case FUN: {
			Symbol symFun = nextSymbol();
			Symbol symId, symOpeningPar, symClosingPar, symColon;
			if (laSymbol.token == Symbol.Token.IDENTIFIER) {
				symId = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseFunctionDeclaration()/: missing identifier, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			if (laSymbol.token == Symbol.Token.OPENING_PARENTHESIS) {
				symOpeningPar = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseFunctionDeclaration()/: missing opening parenthesis, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			LinkedList<ParDecl> pars = parseParametersOpt();
			if (laSymbol.token == Symbol.Token.CLOSING_PARENTHESIS) {
				symClosingPar = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseFunctionDeclaration()/: missing closing parenthesis, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			if (laSymbol.token == Symbol.Token.COLON) {
				symColon = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseFunctionDeclaration()/: missing symbol ':', instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Type type = parseType();
			Expr body = parseFunctionBodyOpt();
			if(body == null) {
				d = new FunDecl(new Position(symFun, type), symId.lexeme, pars, type);
			} else {
				d = new FunDef(new Position(symFun, body), symId.lexeme, pars, type, body);
			}
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseFunctionDeclaration()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return d;
	}
	
	private LinkedList<ParDecl> parseParametersOpt() throws IOException {
		begLog("ParametersOpt");
		LinkedList<ParDecl> pars = new LinkedList<ParDecl>();
		switch (laSymbol.token) {
		case IDENTIFIER: {
			pars = parseParameters();
			break;
		}
		case CLOSING_PARENTHESIS: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseParametersOpt()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return pars;
	}
	
	private LinkedList<ParDecl> parseParameters() throws IOException {
		begLog("Parameters");
		LinkedList<ParDecl> pars = new LinkedList<ParDecl>();
		switch (laSymbol.token) {
		case IDENTIFIER: {
			pars.add(parseParameter());
			pars = parseParametersPrime(pars);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseParameters()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return pars;
	}
	
	private LinkedList<ParDecl> parseParametersPrime(LinkedList<ParDecl> pars) throws IOException {
		begLog("ParametersPrime");
		switch (laSymbol.token) {
		case COMMA: {
			Symbol symComma = nextSymbol();
			pars.add(parseParameter());
			pars = parseParametersPrime(pars);
			break;
		}
		case CLOSING_PARENTHESIS: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseParametersPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return pars;
	}
	
	private ParDecl parseParameter() throws IOException {
		begLog("Parameter");
		ParDecl d = null;
		switch (laSymbol.token) {
		case IDENTIFIER: {
			Symbol symId = nextSymbol();
			Symbol symColon;
			if (laSymbol.token == Symbol.Token.COLON) {
				symColon = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseParameter()/: missing symbol ':', instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Type type = parseType();
			d = new ParDecl(new Position(symId, type), symId.lexeme, type);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseParameter()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return d;
	}
	
	private Expr parseFunctionBodyOpt() throws IOException {
		begLog("FunctionBodyOpt");
		Expr e = null;
		switch (laSymbol.token) {
		case ASSIGN: {
			Symbol symAssign = nextSymbol();
			e = parseExpression();
			break;
		}
		case END: 
		case TYP: 
		case FUN: 
		case VAR: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseFunctionBodyOpt()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return e;
	}
	
	private VarDecl parseVariableDeclaration() throws IOException {
		begLog("VariableDeclaration");
		VarDecl d = null;
		switch (laSymbol.token) {
		case VAR: {
			Symbol symVar = nextSymbol();
			Symbol symId;
			if (laSymbol.token == Symbol.Token.IDENTIFIER) {
				symId = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseVariableDeclaration()/: missing identifier, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			if (laSymbol.token == Symbol.Token.COLON) {
				nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseVariableDeclaration()/: missing symbol ':', instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Type type = parseType();
			d = new VarDecl(new Position(symVar, type), symId.lexeme, type);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseVariableDeclaration()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return d;
	}
	
	private Type parseType() throws IOException {
		begLog("Type");
		Type t = null;
		switch (laSymbol.token) {
		case IDENTIFIER: {
			Symbol symId = nextSymbol();
			t = new TypeName(new Position(symId), symId.lexeme);
			break;
		}
		case INTEGER: {
			Symbol symInt = nextSymbol();
			t = new AtomType(new Position(symInt), AtomType.AtomTypes.INTEGER);
			break;
		}
		case BOOLEAN: {
			Symbol symBool = nextSymbol();
			t = new AtomType(new Position(symBool), AtomType.AtomTypes.BOOLEAN);
			break;
		}
		case CHAR: {
			Symbol symChar = nextSymbol();
			t = new AtomType(new Position(symChar), AtomType.AtomTypes.CHAR);
			break;
		}
		case STRING: {
			Symbol symStr = nextSymbol();
			t = new AtomType(new Position(symStr), AtomType.AtomTypes.STRING);
			break;
		}
		case VOID: {
			Symbol symVoid = nextSymbol();
			t = new AtomType(new Position(symVoid), AtomType.AtomTypes.VOID);
			break;
		}
		case ARR: {
			Symbol symArr = nextSymbol();
			Symbol symOpeningBracket, symClosingBracket;
			if (laSymbol.token == Symbol.Token.OPENING_BRACKET) {
				symOpeningBracket = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseType()/: missing opening bracket, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Expr size = parseExpression();
			if (laSymbol.token == Symbol.Token.CLOSING_BRACKET) {
				symClosingBracket = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseType()/: missing closing bracket, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Type elemType = parseType();
			t = new ArrType(new Position(symArr, elemType), size, elemType);
			break;
		}
		case REC: {
			Symbol symRec = nextSymbol();
			Symbol symOpeningBrace, symClosingBrace;
			if (laSymbol.token == Symbol.Token.OPENING_BRACE) {
				symOpeningBrace = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseType()/: missing opening brace, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			LinkedList<CompDecl> comps = parseComponents();
			if (laSymbol.token == Symbol.Token.CLOSING_BRACE) {
				symClosingBrace = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseType()/: missing closing brace, instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			t = new RecType(new Position(symRec, symClosingBrace), comps);
			break;
		}
		case PTR: {
			Symbol symPtr = nextSymbol();
			Type baseType = parseType();
			t = new PtrType(new Position(symPtr, baseType), baseType);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseType()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return t;
	}
	
	private LinkedList<CompDecl> parseComponents() throws IOException {
		begLog("Components");
		LinkedList<CompDecl> comps = new LinkedList<CompDecl>();
		switch (laSymbol.token) {
		case IDENTIFIER: {
			comps.add(parseComponent());
			comps = parseComponentsPrime(comps);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseComponents()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return comps;
	}
	
	private LinkedList<CompDecl> parseComponentsPrime(LinkedList<CompDecl> comps) throws IOException {
		begLog("ComponentsPrime");
		switch (laSymbol.token) {
		case COMMA: {
			Symbol symComma = nextSymbol();
			comps.add(parseComponent());
			comps = parseComponentsPrime(comps);
			break;
		}
		case CLOSING_BRACE: {
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseComponentsPrime()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return comps;
	}
	
	private CompDecl parseComponent() throws IOException {
		begLog("Component");
		CompDecl d = null;
		switch (laSymbol.token) {
		case IDENTIFIER: {
			Symbol symId = nextSymbol();
			Symbol symColon;
			if (laSymbol.token == Symbol.Token.COLON) {
				symColon = nextSymbol();
			} else {
				throw new SyntaxError("Syntax error /parseComponent()/: missing symbol ':', instead saw unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
			}
			Type type = parseType();
			d = new CompDecl(new Position(symId, type), symId.lexeme, type);
			break;
		}
		default:
			throw new SyntaxError("Syntax error /parseComponent()/: unexpected symbol " + laSymbol.token +" at " + laSymbol.toString());
		}
		endLog();
		return d;
	}

}
