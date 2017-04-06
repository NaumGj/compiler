package compiler.phase.regalloc;

public class ColoredReg {
	
	public Integer reg;
	public Integer color;
	
	public ColoredReg(Integer reg) { 
		this.reg = reg;
	}
	
	public ColoredReg(Integer reg, Integer color) { 
		this.reg = reg;
		this.color = color;
	}
	
}
