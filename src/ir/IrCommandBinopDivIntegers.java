/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;

import java.util.ArrayList;
import java.util.List;

import mips.*;
public class IrCommandBinopDivIntegers extends IrCommand {
    public Temp dst;
    public Temp rs;
    public Temp rt;

    public IrCommandBinopDivIntegers(Temp dst, Temp rs, Temp rt) {
        this.dst = dst;
        this.rs = rs;
        this.rt = rt;
    }

	public void mipsMe(java.util.Map<Temp, String> regMap)
	{
		String RegArg1 = regMap.get(this.rs); 
		String RegArg2 = regMap.get(this.rt);
		String RegRes = regMap.get(this.dst);  
		MipsGenerator.getInstance().div(RegRes,RegArg1,RegArg2);
	}
    
    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<>();
        temps.add(this.rs);
        temps.add(this.rt);
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> temps = new ArrayList<>();
        temps.add(this.dst);
        return temps;
    }
}
