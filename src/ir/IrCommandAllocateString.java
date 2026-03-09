package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import temp.Temp;
import mips.*;

public class IrCommandAllocateString extends IrCommand{
    public String name;
    public String strVal;

    public IrCommandAllocateString(String name, String strVal){
        this.name = name;
        this.strVal = strVal;
    }

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        System.out.println("[DEBUG] Executing MIPS Allocation for String: " + this.name);
        MipsGenerator.getInstance().allocateString(this.name, this.strVal);
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
