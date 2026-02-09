package ir;

import java.util.*;

import temp.Temp;
import mips.*;

public class IrCommandJumpToRa extends IrCommand{
	@Override
	public void mipsMe(Map<Temp, String> tempToReg) {
		MipsGenerator.getInstance().JumpReturn();
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
