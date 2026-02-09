package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import temp.*;
import mips.*;

public class IrCommandCall extends IrCommand {
	public String FuncName;

	public IrCommandCall(String FuncName){
		this.FuncName = FuncName;
	}
	@Override
	public List<Temp> GetUsedTemps() {
		return new ArrayList<Temp>();
	}
	
	@Override
	public List<Temp> GetDefTemps() {
		return new ArrayList<Temp>();
	}
	
	@Override
	public void mipsMe(Map<Temp, String> tempToReg) {
		MipsGenerator.getInstance().func_call(this.FuncName);
	}
}
