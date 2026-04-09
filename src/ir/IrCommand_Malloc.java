package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import temp.Temp;
import mips.*;

public class IrCommand_Malloc extends IrCommand{
    public Temp addressTemp;
    public int size;

    public IrCommand_Malloc(Temp addressTemp, int size){
        this.addressTemp = addressTemp;
        this.size = size;
    }

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        String addrReg = regMap.get(this.addressTemp);
        MipsGenerator.getInstance().malloc(addrReg, this.size);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        return new ArrayList<Temp>();
    }

    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        if (this.addressTemp != null) temps.add(this.addressTemp);
        return temps;
    }
    
}
