package compiler.data.codegen;

public class BranchInstr extends Instr {

	public String jumpLabel;
	public boolean unconditionalJump;
	
	public BranchInstr(String str, boolean isMove, String jumpLabel) {
		super(str, isMove);
		this.jumpLabel = jumpLabel;
		this.unconditionalJump = true;
	}
	
	public BranchInstr(String str, Integer t1, boolean isDef1, boolean isMove, String jumpLabel) {
		super(str, t1, isDef1, isMove);
		this.jumpLabel = jumpLabel;
		this.unconditionalJump = false;
	}

}
