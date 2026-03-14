package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mips.MipsGenerator;
import temp.Temp;

public class IrCommandPrologue extends IrCommand{
	
	public int localStackSize;
    public IrCommandPrologue(int localStackSize) {
        this.localStackSize = localStackSize;
    }
    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        MipsGenerator.getInstance().prologue(this.localStackSize);
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
