package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import temp.Temp;
import mips.*;

public class IrCommandPrintString extends IrCommand {
    public Temp StrAddrTemp;
    public IrCommandPrintString(Temp StrAddrTemp){
        this.StrAddrTemp = StrAddrTemp;
    }

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        String physReg = regMap.get(this.StrAddrTemp);
        MipsGenerator.getInstance().printString(physReg);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        temps.add(this.StrAddrTemp);
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps() {
        return new ArrayList<Temp>();
    }
    
}
