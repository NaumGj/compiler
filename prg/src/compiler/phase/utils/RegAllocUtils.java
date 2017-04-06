package compiler.phase.utils;

import java.io.PrintWriter;
import java.util.ArrayList;

import compiler.data.codegen.Instr;
import compiler.data.frg.CodeFragment;
import compiler.data.imc.TEMP;
import compiler.phase.codegen.EvalCodeGen;
import compiler.phase.regalloc.RegAlloc;

public class RegAllocUtils {
	
	public static void addDataSegmentForStdLib(PrintWriter writer) {
		writer.println("chrBuf\tOCTA 0");
		writer.println("\tBYTE 0");
		writer.println("\tLOC (@+7)&-8");
		writer.println("intBuf\tLOC @+21");
		writer.println("\tLOC (@+7)&-8");
		writer.println("prnsd\tOCTA #ACE7");
		
//	chrBuf	OCTA 0
//		BYTE 0
//		LOC (@+7)&-8
//	intBuf	LOC @+21
//		LOC (@+7)&-8
//	prnsd	OCTA #ACE7
	}
	
	public static void addStdLib(PrintWriter writer) {
		writer.println("_printStr\tADD $0,sp,8");
		writer.println("\tLDO io,$0,0");
		writer.println("\tTRAP 0,Fputs,StdOut");
		writer.println("\tPOP 0,0");
		writer.println("");
		writer.println("_printChr\tLDO $0,sp,8");
		writer.println("\tSTB $0,chrBuf");
		writer.println("\tLDA io,chrBuf");
		writer.println("\tTRAP 0,Fputs,StdOut");
		writer.println("\tPOP 0,0");
		writer.println("");
		writer.println("_printInt\tLDO $0,sp,8");
		writer.println("\tCMP $3,$0,0");
		writer.println("\tZSN $3,$3,1");
		writer.println("\tBZ $3,cont");
		writer.println("\tNEG $0,0,$0");
		writer.println("cont\tLDA $1,intBuf");
		writer.println("\tADD $1,$1,20");
		writer.println("loop\tSUB $1,$1,1");
		writer.println("\tDIV $0,$0,10");
		writer.println("\tGET $2,6");
		writer.println("\tADD $2,$2,48");
		writer.println("\tSTB $2,$1,0");
		writer.println("\tCMP $2,$0,0");
		writer.println("\tZSZ $2,$2,1");
		writer.println("\tPBZ $2,loop");
		writer.println("\tBZ $3,cont2");
		writer.println("\tSUB $1,$1,1");
		writer.println("\tSETL $0,45");
		writer.println("\tSTB $0,$1,0");
		writer.println("cont2\tSTO $1,sp,8");
		writer.println("\tGET $0,rJ");
		writer.println("\tPUSHJ k,_printStr");
		writer.println("\tPUT rJ,$0");
		writer.println("\tPOP 0,0");
		writer.println("");
		
		writer.println("_randomInt\tLDO $0,prnsd");
		writer.println("\tSR $1,$0,6");
		writer.println("\tSR $2,$0,5");
		writer.println("\tXOR $1,$1,$2");
		writer.println("\tAND $1,$1,1");
		writer.println("\tSL $2,$0,1");
		writer.println("\tOR $2,$2,$1");
		writer.println("\tAND $0,$2,#7F");
		writer.println("\tSTO $0,prnsd");
		writer.println("\tSTO $0,sp,0");
		writer.println("\tPOP 0,0");
		writer.println("");
		
//		_rand	LDO $0,prnsd
//		SR $1,$0,6
//		SR $2,$0,5
//		XOR $1,$1,$2
//		AND $1,$1,1
//		SL $2,$0,1
//		OR $2,$2,$1
//		AND $0,$2,#7F
//		STO $0,prnsd
//		STO $0,sp,0
//		POP 0,0

//		_printStr	ADD $0,sp,8
//		LDO io,$0,0
//		TRAP 0,Fputs,StdOut
//		POP 0,0
//	_printChr	LDO $0,sp,8
//		STB $0,chrBuf
//		LDA io,chrBuf
//		TRAP 0,Fputs,StdOut
//		POP 0,0
//	_printInt	LDO $0,sp,8
//		CMP $3,$0,0
//		ZSN $3,$3,1
//		BZ $3,cont
//		NEG $0,0,$0
//	cont	LDA $1,intBuf
//		ADD $1,$1,20
//	loop	SUB $1,$1,1
//		DIV $0,$0,10
//		GET $2,6
//		ADD $2,$2,48
//		STB $2,$1,0
//		CMP $2,$0,0
//		ZSZ $2,$2,1
//		PBZ $2,loop
//		BZ $3,cont2
//		SUB $1,$1,1
//		SETL $0,45
//		STB $0,$1,0
//	cont2	STO $1,sp,8
//		GET $0,rJ
//		PUSHJ k,_printStr
//		PUT rJ,$0
//		POP 0,0
//	_rand	LDO $0,prnsd
//		SR $1,$0,6
//		SR $2,$0,5
//		XOR $1,$1,$2
//		AND $1,$1,1
//		SL $2,$0,1
//		OR $2,$2,$1
//		AND $0,$2,#7F
//		STO $0,prnsd
//		STO $0,sp,0
//		POP 0,0
	}
	
	public static void addMainMethod(PrintWriter writer) {
		writer.println("Main\tSWYM");
		writer.println("\tSETL sp,#0000");
		writer.println("\tINCML sp,#0000");
		writer.println("\tINCMH sp,#0000");
		writer.println("\tINCH sp,#3FFF");
		writer.println("\tADD fp,sp,0");
		writer.println("\tPUSHJ 1,_");
		writer.println("\tTRAP 0,Halt,0");
		writer.println();
		
//		Main      SWYM
//		SETL $254,#0000
//		INCML $254,#0000
//		INCMH $254,#0000
//		INCH $254,#3FFF
//		SET $253,$254
//		PUSHJ 1,_    
//		LDO $0,$253,0
//		TRAP 0,Halt,0
	}
	
	public static void prolog(CodeFragment frag, PrintWriter writer) {
		writer.println(frag.label + "\tADD $0,fp,0");
		writer.println("\tADD fp,sp,0");
		writer.println("\tSETL $1," + frag.frame.getFrameSize());
		writer.println("\tSUB sp,sp,$1");
		writer.println("\tSETL $1," + frag.frame.getOldFPOffset());
		writer.println("\tSUB $1,fp,$1");
		writer.println("\tSTO $0,$1,0");
		writer.println("\tGET $0,rJ");
		writer.println("\tSUB $1,$1,8");
		writer.println("\tSTO $0,$1,0");
		writer.println();
		
//		ADD $0,$253,0         % stari fp shranim v $0
//		ADD $253,$254,0        % fp <- sp
//		SETL $1,40               % $1 <- frame size
//		SUB $254,$254,$1        % od sp odštejem frame size
//		SETL $1,16          % old fp offset
//		SUB $1,$253,$1        % od fp odštejem old fp offset
//		STO $0,$1,0              % na zgornji naslov shrani stari fp
//		GET $0,rJ           % dobi povratni naslov
//		SUB $1,$1,8              % mesto za povratni naslov
//		STO $0,$1,0              % shrani povratni naslov

	}
	public static void epilog(CodeFragment frag, PrintWriter writer) {
		writer.println("\tSETL $1," + frag.frame.getOldFPOffset());
		writer.println("\tSUB $1,fp,$1");
		writer.println("\tLDO $0,$1,0");
		writer.println("\tSUB $1,$1,8");
		writer.println("\tLDO $1,$1,0");
		writer.println("\tPUT rJ,$1");
		writer.println("\tADD sp,fp,0");
		writer.println("\tADD fp,$0,0");
		writer.println("\tPOP 0,0");
		writer.println();
		
//		STO $0,$253,0         % Shrani rv na fp
//		SETL $1,16          % stari fp offset
//		SUB $1,$253,$1        % odštejem fp-ju
//		LDO $0,$1,0              % loadam stari fp
//		SUB $1,$1,8              % return addr
//		LDO $1,$1,0              % loadam return addr
//		PUT rJ,$1           % jo nastavim v rJ
//		ADD $254,$253,0            % sp <- fp
//		ADD $253,$0,0         % fp <- stari fp
//		POP 0,0                % return
	}

	public static void addSpillInstructionsFstRegLDO(EvalCodeGen codeGen, Instr instr, int index) {
		Integer temp = TEMP.newTempName();
		
		Long offset = (long)0;
		if(RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t1) != null) {
			offset = RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t1);
		}
//		System.out.print("OFFSET:");
//		System.out.print(instr.t1);
//		System.out.println(offset);
		ArrayList<Integer> offsetPars = loadOffset(codeGen, instr, index, offset);
		Integer reg = offsetPars.get(0);
		index = offsetPars.get(1);
		
		codeGen.regs.remove(instr.t1);
		codeGen.regs.add(temp);
		
		Instr ldo = new Instr("\tLDO %s,fp,%s\n", temp, true, reg, false, false);
		ldo.addToListAtIndex(codeGen, codeGen.instructions, index);
		instr.changeReg(temp, 1);
	}
	
	public static void addSpillInstructionsSndRegLDO(EvalCodeGen codeGen, Instr instr, int index) {
		Integer temp =  TEMP.newTempName();
		
		Long offset = (long)0;
		if(RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t2) != null) {
			offset = RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t2);
		}
//		System.out.print("OFFSET:");
//		System.out.print(instr.t2);
//		System.out.println(offset);
		ArrayList<Integer> offsetPars = loadOffset(codeGen, instr, index, offset);
		Integer reg = offsetPars.get(0);
		index = offsetPars.get(1);
		
		codeGen.regs.remove(instr.t2);
		codeGen.regs.add(temp);
		Instr ldo = new Instr("\tLDO %s,fp,%s\n", temp, true, reg, false, false);
		ldo.addToListAtIndex(codeGen, codeGen.instructions, index);
		instr.changeReg(temp, 2);
	}
	
	public static void addSpillInstructionsThdRegLDO(EvalCodeGen codeGen, Instr instr, int index) {
		Integer temp =  TEMP.newTempName();
		
		Long offset = (long)0;
		if(RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t3) != null) {
			offset = RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t3);
		}
//		System.out.print("OFFSET:");
//		System.out.print(instr.t3);
//		System.out.println(offset);
		ArrayList<Integer> offsetPars = loadOffset(codeGen, instr, index, offset);
		Integer reg = offsetPars.get(0);
		index = offsetPars.get(1);
		
		codeGen.regs.remove(instr.t3);
		codeGen.regs.add(temp);
		Instr ldo = new Instr("\tLDO %s,fp,%s\n", temp, true, reg, false, false);
		ldo.addToListAtIndex(codeGen, codeGen.instructions, index);
		instr.changeReg(temp, 3);
	}
	
	public static void addSpillInstructionsFstRegSTO(EvalCodeGen codeGen, Instr instr, int index) {
		Integer temp =  TEMP.newTempName();
		
		Long offset = (long)0;
		if(RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t1) != null) {
			offset = RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t1);
		} else {
			RegAlloc.curCodeFragment.frame.tmpVarsSize += 8;
			offset = RegAlloc.curCodeFragment.frame.getTmpVarsOffset();
			RegAlloc.curCodeFragment.frame.tmpVarsToOffset.put(instr.t1, offset);
		}
//		System.out.print("OFFSET:");
//		System.out.print(instr.t1);
//		System.out.println(offset);
		ArrayList<Integer> offsetPars = loadOffset(codeGen, instr, index, offset);
		Integer reg = offsetPars.get(0);
		index = offsetPars.get(1);
		
		codeGen.regs.remove(instr.t1);
		codeGen.regs.add(temp);
		Instr sto = new Instr("\tSTO %s,fp,%s\n", temp, false, reg, false, false);
		sto.addToListAtIndex(codeGen, codeGen.instructions, index + 1);
		instr.changeReg(temp, 1);
	}
	
	public static void addSpillInstructionsSndRegSTO(EvalCodeGen codeGen, Instr instr, int index) {
		Integer temp =  TEMP.newTempName();
		
		Long offset = (long)0;
		if(RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t2) != null) {
			offset = RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t2);
		} else {
			RegAlloc.curCodeFragment.frame.tmpVarsSize += 8;
			offset = RegAlloc.curCodeFragment.frame.getTmpVarsOffset();
			RegAlloc.curCodeFragment.frame.tmpVarsToOffset.put(instr.t2, offset);
		}
//		System.out.print("OFFSET:");
//		System.out.print(instr.t2);
//		System.out.println(offset);
		ArrayList<Integer> offsetPars = loadOffset(codeGen, instr, index, offset);
		Integer reg = offsetPars.get(0);
		index = offsetPars.get(1);
		
		codeGen.regs.remove(instr.t2);
		codeGen.regs.add(temp);
		
		Instr sto = new Instr("\tSTO %s,fp,%s\n", temp, false, reg, false, false);
		sto.addToListAtIndex(codeGen, codeGen.instructions, index + 1);
		instr.changeReg(temp, 2);
	}
	
	public static void addSpillInstructionsThdRegSTO(EvalCodeGen codeGen, Instr instr, int index) {
		Integer temp =  TEMP.newTempName();
		
		Long offset = (long)0;
		if(RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t3) != null) {
			offset = RegAlloc.curCodeFragment.frame.tmpVarsToOffset.get(instr.t3);
		} else {
			RegAlloc.curCodeFragment.frame.tmpVarsSize += 8;
			offset = RegAlloc.curCodeFragment.frame.getTmpVarsOffset();
			RegAlloc.curCodeFragment.frame.tmpVarsToOffset.put(instr.t3, offset);
		}
//		System.out.print("OFFSET:");
//		System.out.print(instr.t3);
//		System.out.println(offset);
		ArrayList<Integer> offsetPars = loadOffset(codeGen, instr, index, offset);
		Integer reg = offsetPars.get(0);
		index = offsetPars.get(1);
		
		codeGen.regs.remove(instr.t3);
		codeGen.regs.add(temp);
		Instr sto = new Instr("\tSTO %s,fp,%s\n", temp, false, reg, false, false);
		sto.addToListAtIndex(codeGen, codeGen.instructions, index + 1);
		instr.changeReg(temp, 3);
	}
	
	public static ArrayList<Integer> loadOffset(EvalCodeGen codeGen, Instr instr, int index, long offset) {
		Integer temp = new Integer(TEMP.newTempName());
		codeGen.regs.add(temp);
		Instr set = new Instr("\tSETL %s," + Long.toString(offset & 0xFFFFL) + "\n", temp, true, false);
		set.addToListAtIndex(codeGen, codeGen.instructions, index);
		index++;
		if(instr.label != null) {
			set.label = instr.label;
			instr.label = null;
		}
		if((offset & 0xFFFF0000L) != 0) {
			Instr set2 = new Instr("\tINCML %s," + Long.toString((offset >> 16) & 0xFFFFL) + "\n", temp, true, false);
			set2.addToListAtIndex(codeGen, codeGen.instructions, index);
			index++;
		}
		if((offset & 0xFFFF00000000L) != 0) {
			Instr set3 = new Instr("\tINCMH %s," + Long.toString((offset >> 32) & 0xFFFFL) + "\n", temp, true, false);
			set3.addToListAtIndex(codeGen, codeGen.instructions, index);
			index++;
		}
		if((offset & 0xFFFF000000000000L) != 0) {
			Instr set4 = new Instr("\tINCH %s," + Long.toString((offset >> 48) & 0xFFFFL) + "\n", temp, true, false);
			set4.addToListAtIndex(codeGen, codeGen.instructions, index);
			index++;
		}
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(temp);
		list.add(index);
		return list;
	}
	
}
