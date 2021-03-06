package compiler;

import java.io.IOException;

import compiler.common.report.*;
import compiler.phase.lexan.*;
import compiler.phase.regalloc.RegAlloc;
import compiler.phase.synan.*;
import compiler.phase.abstr.*;
import compiler.phase.codegen.CodeGen;
import compiler.phase.seman.*;
import compiler.phase.frames.*;
import compiler.phase.imcode.*;
import compiler.phase.lincode.*;

/**
 * The compiler's entry point.
 * 
 * @author sliva
 */
public class Main {

	/**
	 * The compiler's entry point: it parses the command line and triggers the
	 * compilation.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String args[]) {
		// OK, start at the very beginning.
		System.out.println("This is PREV compiler (2016):");

		try {
			// Parse the command line.
			Task task = new Task(args);

			// Carry out the compilation up to the specified phase.
			while (true) {

				// ***** Lexical analysis. *****
				if (task.phase.equals("lexan")) {
					LexAn lexAn = new LexAn(task);
					while (lexAn.lexAn().token != Symbol.Token.EOF) {
					}
					lexAn.close();
					break;
				}

				// ***** Syntax analysis. *****
				SynAn synAn = new SynAn(task);
				task.prgAST = synAn.synAn();
				synAn.close();
				if (task.phase.equals("synan"))
					break;

				// ***** Abstract syntax tree. *****
				Abstr abstr = new Abstr(task);
				abstr.close();
				if (task.phase.equals("abstr"))
					break;

				// ***** Semantic analysis. *****
				SemAn seman = new SemAn(task);
				(new EvalValue(task.prgAttrs)).visit(task.prgAST);
				(new EvalDecl(task.prgAttrs)).visit(task.prgAST);
				(new EvalTyp(task.prgAttrs)).visit(task.prgAST);
				(new EvalMem(task.prgAttrs)).visit(task.prgAST);
				seman.close();
				if (task.phase.equals("seman"))
					break;

				if (Report.getNumWarnings() > 0)
					break;

				// Frames and accesses.
				Frames frames = new Frames(task);
				(new EvalFrames(task.prgAttrs)).visit(task.prgAST);
				frames.close();
				if (task.phase.equals("frames"))
					break;
				
				// Intermediate code generation.
				Imcode imcode = new Imcode(task);
				(new EvalImcode(task.prgAttrs, task.fragments)).visit(task.prgAST);
				imcode.close();
				if (task.phase.equals("imcode"))
					break;
				
				// Linearization of the intermediate code.
				if (task.phase.equals("lincode")){
					LinCode linCode = new LinCode(task);
//					(new EvalLinCode(task.fragments)).visit(task.prgAST);
					linCode.close();
				}
				if (task.phase.equals("lincode"))
					break;
				
				// Liveness analysis.
				CodeGen codeGen = new CodeGen(task, task.phase.equals("codegen"));
//				(new EvalCodeGen(task.fragments)).visit(task.prgAST);
				codeGen.close();
				if (task.phase.equals("codegen"))
					break;
				
				// Register allocation.
				RegAlloc regAlloc = new RegAlloc(task);
//				(new EvalCodeGen(task.fragments)).visit(task.prgAST);
				regAlloc.close();
				if (task.phase.equals("regalloc"))
					break;

				break;
			}
		} catch (CompilerError errorReport) {
			// As dead as a dodo. Print error message and signal error.
			System.err.println(":-( " + errorReport.getMessage());
			System.exit(1);
		} catch (IOException errorReport) {
			System.err.println("IOException :-( " + errorReport.getMessage());
			System.exit(1);
		}

		if (Report.getNumWarnings() > 0) {
			// There is still room for improvement.
			Report.warning("Have you seen all warning messages?");
			System.exit(0);
		} else {
			// Let's hope it ever comes this far.
			Report.info("Done.");
			System.exit(0);
		}
	}

}
