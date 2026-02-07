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

public class IrCommandBinopAddIntegers extends IrCommand
{
	public Temp t1;
	public Temp t2;
	public Temp dst;
	
	public IrCommandBinopAddIntegers(Temp dst, Temp t1, Temp t2)
	{
		this.dst = dst;
		this.t1 = t1;
		this.t2 = t2;
	}
	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe(java.util.Map<Temp, String> regMap)
	{
		String RegArg1 = regMap.get(this.t1); 
		String RegArg2 = regMap.get(this.t2);
		String RegRes = regMap.get(this.dst);  
		MipsGenerator.getInstance().add(RegRes,RegArg1,RegArg2);
	}
	
	@Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        temps.add(this.t1);
		temps.add(this.t2);
        return temps;
    }
    @Override
    public List<Temp> GetDefTemps(){
        List<Temp> temps = new ArrayList<Temp>();
		temps.add(this.dst);
		return temps;
    }
}
