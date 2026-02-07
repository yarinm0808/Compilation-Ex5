package ir;
/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mips.*;

public class IrCommand_Check_Null_Ptr extends IrCommand{
	
    Temp t;
    public IrCommand_Check_Null_Ptr(Temp t){
        this.t = t;
    }
    
    @Override
    public void mipsMe(java.util.Map<Temp, String> regMap) {
        String strT = regMap.get(this.t);
        if (strT != null) {
            MipsGenerator.getInstance().check_null_ptr(strT);
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
