package ir;

import java.util.ArrayList;
import java.util.List;

import mips.*;
import temp.Temp;

public class IrCommand_Check_Array_Bounds extends IrCommand{
    public Temp ArrayPointer;
    public Temp index;

    public IrCommand_Check_Array_Bounds(Temp ArrayPointer, Temp index){
        this.ArrayPointer = ArrayPointer;
        this.index = index;
    }

    @Override
    public void mipsMe(java.util.Map<Temp, String> regMap) {
        // 1. Convert virtual Temps to physical register strings (e.g., "$t0")
        String arrReg = regMap.get(this.ArrayPointer);
        String idxReg = regMap.get(this.index);

        // 2. Pass the strings to the generator
        MipsGenerator.getInstance().check_array_bounds(arrReg, idxReg);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<>();
        temps.add(this.ArrayPointer);
        temps.add(this.index);
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps() {
        return new ArrayList<Temp>();
    }
    
}
