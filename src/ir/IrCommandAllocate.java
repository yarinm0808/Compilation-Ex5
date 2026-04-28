package ir;

import java.util.ArrayList;
import java.util.List;
import mips.*;
import temp.Temp;

public class IrCommandAllocate extends IrCommand {
	String varName;

	public IrCommandAllocate(String varName) {
		this.varName = varName;
	}

	@Override
	public void mipsMe(java.util.Map<Temp, String> regMap) {
		// Simply declare the word. No .data or .text switches here!
		MipsGenerator.getInstance().addnotab("global_"+varName + ": .word 0");
	}

	@Override
	public List<Temp> GetUsedTemps() {
		return new ArrayList<Temp>();
	}

	@Override
	public List<Temp> GetDefTemps() {
		return new ArrayList<Temp>();
	}
}