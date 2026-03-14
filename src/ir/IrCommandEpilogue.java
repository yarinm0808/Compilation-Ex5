package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mips.MipsGenerator;
import temp.Temp;

public class IrCommandEpilogue extends IrCommand{
	public int bytesNeeded;
	public IrCommandEpilogue(int bytesNeeded){
		this.bytesNeeded = bytesNeeded;
	}

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        MipsGenerator.getInstance().epilogue(this.bytesNeeded);
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
