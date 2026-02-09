package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mips.MipsGenerator;
import temp.Temp;

public class IrCommandPrologue extends IrCommand{
    
    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        MipsGenerator.getInstance().prologue();
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
