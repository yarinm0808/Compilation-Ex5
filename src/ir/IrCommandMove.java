package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mips.*;
import temp.Temp;

public class IrCommandMove extends IrCommand {
    public String Reg;
    public Temp DstReg;
    public IrCommandMove(String Reg, Temp DstReg){
        this.Reg = Reg;
        this.DstReg = DstReg;
    }


    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        String RegIdx = regMap.get(this.DstReg);
        MipsGenerator.getInstance().Move(RegIdx, this.Reg);
    }
    
    @Override
    public List<Temp> GetUsedTemps() {
        return new ArrayList<Temp>();
    }

    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        temps.add(this.DstReg);
        return temps;
    }
    
}
