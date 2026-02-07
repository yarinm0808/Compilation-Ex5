package ir;


/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;

import java.util.ArrayList;
import java.util.List;

import mips.*;

public class IrCommand_Check_Division_By_Zero extends IrCommand{
    Temp t;
    public IrCommand_Check_Division_By_Zero(Temp t){
        this.t = t;
    }

    @Override
    public void mipsMe(java.util.Map<Temp, String> regMap) {
        String strT = regMap.get(this.t);
        if (strT != null) {
            MipsGenerator.getInstance().check_division_by_zero(strT);
        }
    }
    
    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<>();
        temps.add(this.t);
        return temps;
    }
    @Override
    public List<Temp> GetDefTemps() {
        return new ArrayList<Temp>();
    }
}
